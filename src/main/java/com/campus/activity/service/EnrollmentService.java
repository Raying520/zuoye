package com.campus.activity.service;

import com.campus.activity.dto.UserSession;
import com.campus.activity.entity.Activity;
import com.campus.activity.entity.Enrollment;
import com.campus.activity.repository.ActivityRepository;
import com.campus.activity.repository.EnrollmentRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * 报名业务服务（FR5 报名/取消报名、FR6 名单查看）
 * 核心规则：BR2 状态校验、BR3 容量校验、BR4 重复报名校验
 */
@Service
public class EnrollmentService {

    private final EnrollmentRepository enrollmentRepository;
    private final ActivityRepository activityRepository;

    public EnrollmentService(EnrollmentRepository enrollmentRepository,
                             ActivityRepository activityRepository) {
        this.enrollmentRepository = enrollmentRepository;
        this.activityRepository = activityRepository;
    }

    /** 学生报名（FR5）：三重校验后写入记录 */
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

        // BR3：报名人数 < 容量上限
        long enrolledCount = enrollmentRepository.countByActivityId(activityId);
        if (enrolledCount >= activity.getCapacity()) {
            throw new IllegalArgumentException("活动名额已满");
        }

        // BR4：不可重复报名（数据库唯一约束兜底）
        if (enrollmentRepository.existsByActivityIdAndUserId(activityId, student.getId())) {
            throw new IllegalArgumentException("你已经报名过该活动");
        }

        Enrollment enrollment = new Enrollment();
        enrollment.setActivityId(activityId);
        enrollment.setUserId(student.getId());
        enrollmentRepository.save(enrollment);
    }

    /** 学生取消报名（FR5）：仅活动报名中时可取消 */
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
        enrollmentRepository.delete(enrollment);
    }

    /** 查询某活动当前报名人数（BR3） */
    public long countByActivity(Long activityId) {
        return enrollmentRepository.countByActivityId(activityId);
    }

    /** 查询学生是否已报名某活动（BR4） */
    public boolean isEnrolled(Long activityId, Long userId) {
        return enrollmentRepository.existsByActivityIdAndUserId(activityId, userId);
    }

    /** 查询学生的所有报名记录（FR6） */
    public List<Enrollment> findByUser(Long userId) {
        return enrollmentRepository.findByUserIdOrderByCreateTimeDesc(userId);
    }

    /** 查询活动的所有报名记录（FR6） */
    public List<Enrollment> findByActivity(Long activityId) {
        return enrollmentRepository.findByActivityIdOrderByCreateTimeDesc(activityId);
    }
}
