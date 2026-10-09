package com.campus.activity.service;

import com.campus.activity.dto.UserSession;
import com.campus.activity.entity.Activity;
import com.campus.activity.entity.Enrollment;
import com.campus.activity.repository.ActivityRepository;
import com.campus.activity.repository.EnrollmentRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.EnumSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;


/**
 * 报名业务服务（FR5 报名/取消报名、FR6 名单查看）
 * V2.0 新增：报名审核（FR9）、候补与候补转正（FR10/FR11）、报名截止（FR15）
 */
@Service
public class EnrollmentService {

    /** 一个学生在一项活动中只能存在一条“有效”记录的状态集合 */
    private static final Set<Enrollment.Status> ACTIVE_STATUSES = EnumSet.of(
            Enrollment.Status.PENDING,
            Enrollment.Status.APPROVED,
            Enrollment.Status.WAITING
    );

    private final EnrollmentRepository enrollmentRepository;
    private final ActivityRepository activityRepository;

    public EnrollmentService(EnrollmentRepository enrollmentRepository,
                             ActivityRepository activityRepository) {
        this.enrollmentRepository = enrollmentRepository;
        this.activityRepository = activityRepository;
    }

    /** 学生报名（FR5）：含审核、候补、截止等 V2.0 规则 */
    @Transactional
    public void enroll(Long activityId, UserSession student) {
        if (!student.isStudent()) {
            throw new IllegalArgumentException("只有学生可以报名活动");
        }
        Activity activity = activityRepository.findById(activityId)
                .orElseThrow(() -> new IllegalArgumentException("活动不存在"));

        // BR2：活动状态 = 报名中
        if (activity.getStatus() != Activity.Status.OPEN) {
            throw new IllegalArgumentException("该活动当前不可报名");
        }

        // FR15：报名截止检查
        if (activity.getRegistrationDeadline() != null
                && LocalDateTime.now().isAfter(activity.getRegistrationDeadline())) {
            throw new IllegalArgumentException("报名已截止");
        }

        // 已存在记录的处理（兼容 V1 数据中 status 为 null 的情况）
        Optional<Enrollment> existingOpt = enrollmentRepository.findByActivityIdAndUserId(activityId, student.getId());
        if (existingOpt.isPresent()) {
            Enrollment existing = existingOpt.get();
            Enrollment.Status status = existing.getStatus();
            if (status != null && status == Enrollment.Status.REJECTED) {
                // 被驳回后可重新报名，删除旧记录
                enrollmentRepository.delete(existing);
            } else if (status == null || ACTIVE_STATUSES.contains(status)) {
                throw new IllegalArgumentException("你已报名/候补/审核中该活动");
            }
        }

        Enrollment enrollment = new Enrollment();
        enrollment.setActivityId(activityId);
        enrollment.setUserId(student.getId());

        boolean requireAudit = Boolean.TRUE.equals(activity.getRequireAudit());
        boolean allowWaitlist = Boolean.TRUE.equals(activity.getAllowWaitlist());

        if (requireAudit) {
            // FR9：需审核的活动先进入待审核状态
            long approvedCount = approvedCount(activityId);
            if (approvedCount >= activity.getCapacity()) {
                throw new IllegalArgumentException("活动名额已满");
            }
            enrollment.setStatus(Enrollment.Status.PENDING);
        } else {
            long approvedCount = approvedCount(activityId);
            if (approvedCount < activity.getCapacity()) {
                enrollment.setStatus(Enrollment.Status.APPROVED);
            } else if (allowWaitlist) {
                // FR10/FR11：满员后进入候补队列
                enrollment.setStatus(Enrollment.Status.WAITING);
                enrollment.setWaitingNo(nextWaitingNo(activityId));
            } else {
                throw new IllegalArgumentException("活动名额已满");
            }
        }

        enrollmentRepository.save(enrollment);
    }

    /** 学生取消报名（FR5）：仅活动报名中时可取消，取消后触发候补转正 */
    @Transactional
    public void cancel(Long activityId, UserSession student) {
        if (!student.isStudent()) {
            throw new IllegalArgumentException("只有学生可以取消报名");
        }
        Activity activity = activityRepository.findById(activityId)
                .orElseThrow(() -> new IllegalArgumentException("活动不存在"));
        if (activity.getStatus() != Activity.Status.OPEN) {
            throw new IllegalArgumentException("活动已结束或已取消，无法变更报名");
        }
        Enrollment enrollment = enrollmentRepository.findByActivityIdAndUserId(activityId, student.getId())
                .orElseThrow(() -> new IllegalArgumentException("你尚未报名该活动"));

        Enrollment.Status oldStatus = enrollment.getStatus();
        enrollmentRepository.delete(enrollment);

        if (oldStatus == Enrollment.Status.APPROVED) {
            promoteWaiting(activityId);
        }
    }

    /** 教师审核通过（FR9） */
    @Transactional
    public void approve(Long enrollmentId, UserSession teacher) {
        if (!teacher.isTeacher()) {
            throw new IllegalArgumentException("只有教师可以审核报名");
        }
        Enrollment enrollment = enrollmentRepository.findById(enrollmentId)
                .orElseThrow(() -> new IllegalArgumentException("报名记录不存在"));
        Activity activity = verifyTeacherActivity(enrollment, teacher);
        if (enrollment.getStatus() != Enrollment.Status.PENDING) {
            throw new IllegalArgumentException("只能审核待审核的报名");
        }
        if (approvedCount(activity.getId()) >= activity.getCapacity()) {
            throw new IllegalArgumentException("已通过人数已达容量上限");
        }
        enrollment.setStatus(Enrollment.Status.APPROVED);
        enrollmentRepository.save(enrollment);
    }

    /** 教师审核拒绝（FR9） */
    @Transactional
    public void reject(Long enrollmentId, UserSession teacher) {
        if (!teacher.isTeacher()) {
            throw new IllegalArgumentException("只有教师可以审核报名");
        }
        Enrollment enrollment = enrollmentRepository.findById(enrollmentId)
                .orElseThrow(() -> new IllegalArgumentException("报名记录不存在"));
        Activity activity = verifyTeacherActivity(enrollment, teacher);
        if (enrollment.getStatus() != Enrollment.Status.PENDING) {
            throw new IllegalArgumentException("只能审核待审核的报名");
        }
        enrollment.setStatus(Enrollment.Status.REJECTED);
        enrollment.setWaitingNo(null);
        enrollmentRepository.save(enrollment);
    }

    /** 查询某活动已通过人数（实际参与名额占用） */
    public long approvedCount(Long activityId) {
        return enrollmentRepository.countByActivityIdAndStatus(activityId, Enrollment.Status.APPROVED);
    }

    /** 查询某活动候补人数 */
    public long waitingCount(Long activityId) {
        return enrollmentRepository.countByActivityIdAndStatus(activityId, Enrollment.Status.WAITING);
    }

    /** 查询学生是否已报名某活动（BR4） */
    public boolean isEnrolled(Long activityId, Long userId) {
        return enrollmentRepository.existsByActivityIdAndUserIdAndStatusIn(activityId, userId, ACTIVE_STATUSES);
    }

    /** 查询学生是否已候补某活动 */
    public boolean isWaiting(Long activityId, Long userId) {
        return enrollmentRepository.existsByActivityIdAndUserIdAndStatusIn(
                activityId, userId, EnumSet.of(Enrollment.Status.WAITING));
    }

    /** 查询学生的所有报名记录（FR6） */
    public List<Enrollment> findByUser(Long userId) {
        return enrollmentRepository.findByUserIdOrderByCreateTimeDesc(userId);
    }

    /** 查询活动的所有报名记录（FR6） */
    public List<Enrollment> findByActivity(Long activityId) {
        return enrollmentRepository.findByActivityIdOrderByCreateTimeDesc(activityId);
    }

    /** 查询某学生在一项活动中的报名记录（详情页状态可见 FR12/FR14） */
    public Optional<Enrollment> findByActivityAndUser(Long activityId, Long userId) {
        return enrollmentRepository.findByActivityIdAndUserId(activityId, userId);
    }

    /** 候补转正：当产生空位时，将最早候补的记录转为已通过 */
    @Transactional
    public void promoteWaiting(Long activityId) {
        Optional<Enrollment> firstWaiting = enrollmentRepository
                .findFirstByActivityIdAndStatusOrderByCreateTimeAsc(activityId, Enrollment.Status.WAITING);
        if (firstWaiting.isPresent()) {
            Enrollment enrollment = firstWaiting.get();
            enrollment.setStatus(Enrollment.Status.APPROVED);
            enrollment.setWaitingNo(null);
            enrollmentRepository.save(enrollment);
        }
    }

    /** 生成下一个候补序号 */
    private int nextWaitingNo(Long activityId) {
        return enrollmentRepository.findFirstByActivityIdAndStatusOrderByWaitingNoDesc(activityId, Enrollment.Status.WAITING)
                .map(e -> (e.getWaitingNo() == null ? 0 : e.getWaitingNo()) + 1)
                .orElse(1);
    }

    private Activity verifyTeacherActivity(Enrollment enrollment, UserSession teacher) {
        Activity activity = activityRepository.findById(enrollment.getActivityId())
                .orElseThrow(() -> new IllegalArgumentException("活动不存在"));
        if (!activity.getTeacherId().equals(teacher.getId())) {
            throw new IllegalArgumentException("只能审核自己发布的活动");
        }
        return activity;
    }

    /** 按 ID 查询报名记录（控制器跳转用） */
    public Enrollment findByIdHelper(Long id) {
        return enrollmentRepository.findById(id).orElse(null);
    }
}
