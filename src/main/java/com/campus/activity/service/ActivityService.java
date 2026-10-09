package com.campus.activity.service;

import com.campus.activity.dto.ActivityForm;
import com.campus.activity.dto.ActivityListItem;
import com.campus.activity.dto.UserSession;
import com.campus.activity.entity.Activity;
import com.campus.activity.entity.Enrollment;
import com.campus.activity.repository.ActivityRepository;
import com.campus.activity.repository.EnrollmentRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * 活动业务服务（FR3、FR4、FR7 的业务规则实现）
 * V2.0 新增：活动编辑（FR13）、报名截止/条件/审核/候补相关字段
 */
@Service
public class ActivityService {

    private final ActivityRepository activityRepository;
    private final EnrollmentRepository enrollmentRepository;

    public ActivityService(ActivityRepository activityRepository,
                           EnrollmentRepository enrollmentRepository) {
        this.activityRepository = activityRepository;
        this.enrollmentRepository = enrollmentRepository;
    }

    /** 教师发布活动（FR3） */
    @Transactional
    public Activity publish(ActivityForm form, UserSession teacher) {
        if (!teacher.isTeacher()) {
            throw new IllegalArgumentException("只有教师可以发布活动");
        }
        Activity activity = new Activity();
        fillFromForm(activity, form);
        activity.setStatus(Activity.Status.OPEN);
        activity.setTeacherId(teacher.getId());
        return activityRepository.save(activity);
    }

    /** 教师编辑已发布的活动（FR13）：仅 OPEN 时可编辑 */
    @Transactional
    public Activity update(Long activityId, ActivityForm form, UserSession teacher) {
        Activity activity = activityRepository.findById(activityId)
                .orElseThrow(() -> new IllegalArgumentException("活动不存在"));
        if (!activity.getTeacherId().equals(teacher.getId())) {
            throw new IllegalArgumentException("只能编辑自己发布的活动");
        }
        if (activity.getStatus() != Activity.Status.OPEN) {
            throw new IllegalArgumentException("活动当前状态不可编辑");
        }
        fillFromForm(activity, form);
        return activityRepository.save(activity);
    }

    private void fillFromForm(Activity activity, ActivityForm form) {
        activity.setTitle(form.getTitle().trim());
        activity.setDescription(form.getDescription());
        activity.setActivityTime(form.getActivityTime());
        activity.setPlace(form.getPlace().trim());
        activity.setCapacity(form.getCapacity());
        activity.setRequirements(form.getRequirements());
        activity.setRegistrationDeadline(form.getRegistrationDeadline());
        activity.setRequireAudit(form.getRequireAudit() != null ? form.getRequireAudit() : false);
        activity.setAllowWaitlist(form.getAllowWaitlist() != null ? form.getAllowWaitlist() : false);
    }

    /** 查询所有正常活动，按活动时间倒序（FR4 + V3.0 BR15） */
    public List<Activity> findAll() {
        return activityRepository.findByPlatformStatusOrderByActivityTimeDesc(Activity.PlatformStatus.NORMAL);
    }

    /** 查询所有正常活动并附带当前报名人数（FR4 列表展示） */
    public List<ActivityListItem> findAllWithEnrollmentCount() {
        List<Activity> activities = activityRepository.findByPlatformStatusOrderByActivityTimeDesc(Activity.PlatformStatus.NORMAL);
        List<ActivityListItem> result = new ArrayList<>(activities.size());
        LocalDateTime now = LocalDateTime.now();
        for (Activity activity : activities) {
            long approved = countApproved(activity.getId());
            long waiting = countWaiting(activity.getId());
            boolean deadlinePassed = isDeadlinePassed(activity, now);
            result.add(new ActivityListItem(activity, approved, waiting, deadlinePassed));
        }
        return result;
    }

    /** 管理员查看全部活动（V3.0 FR18） */
    public List<Activity> findAllForAdmin() {
        return activityRepository.findAllByOrderByActivityTimeDesc();
    }

    /** 管理员设置活动平台状态（V3.0 FR19） */
    @Transactional
    public void setPlatformStatus(Long activityId, Activity.PlatformStatus status) {
        Activity activity = activityRepository.findById(activityId)
                .orElseThrow(() -> new IllegalArgumentException("活动不存在"));
        activity.setPlatformStatus(status);
        activityRepository.save(activity);
    }

    /** 教师查询自己发布的活动（FR7） */
    public List<Activity> findByTeacher(Long teacherId) {
        return activityRepository.findByTeacherIdOrderByActivityTimeDesc(teacherId);
    }

    /** 教师查询自己发布的活动并附带报名人数（FR7 管理列表） */
    public List<ActivityListItem> findByTeacherWithEnrollmentCount(Long teacherId) {
        List<Activity> activities = activityRepository.findByTeacherIdOrderByActivityTimeDesc(teacherId);
        List<ActivityListItem> result = new ArrayList<>(activities.size());
        LocalDateTime now = LocalDateTime.now();
        for (Activity activity : activities) {
            long approved = countApproved(activity.getId());
            long waiting = countWaiting(activity.getId());
            boolean deadlinePassed = isDeadlinePassed(activity, now);
            result.add(new ActivityListItem(activity, approved, waiting, deadlinePassed));
        }
        return result;
    }

    /** 按 ID 查询活动 */
    public Activity findById(Long id) {
        return activityRepository.findById(id).orElse(null);
    }

    /** 教师取消活动（FR7 + BR5） */
    @Transactional
    public void cancel(Long activityId, UserSession teacher) {
        Activity activity = activityRepository.findById(activityId)
                .orElseThrow(() -> new IllegalArgumentException("活动不存在"));
        if (!activity.getTeacherId().equals(teacher.getId())) {
            throw new IllegalArgumentException("只能管理自己发布的活动");
        }
        if (activity.getStatus() != Activity.Status.OPEN) {
            throw new IllegalArgumentException("活动当前状态不可取消");
        }
        activity.setStatus(Activity.Status.CANCELLED);
        activityRepository.save(activity);
    }

    /** 教师结束活动（FR7 + BR5） */
    @Transactional
    public void close(Long activityId, UserSession teacher) {
        Activity activity = activityRepository.findById(activityId)
                .orElseThrow(() -> new IllegalArgumentException("活动不存在"));
        if (!activity.getTeacherId().equals(teacher.getId())) {
            throw new IllegalArgumentException("只能管理自己发布的活动");
        }
        if (activity.getStatus() != Activity.Status.OPEN) {
            throw new IllegalArgumentException("活动当前状态不可结束");
        }
        activity.setStatus(Activity.Status.CLOSED);
        activityRepository.save(activity);
    }

    private long countApproved(Long activityId) {
        return enrollmentRepository.countByActivityIdAndStatus(activityId, Enrollment.Status.APPROVED);
    }

    private long countWaiting(Long activityId) {
        return enrollmentRepository.countByActivityIdAndStatus(activityId, Enrollment.Status.WAITING);
    }

    private boolean isDeadlinePassed(Activity activity, LocalDateTime now) {
        return activity.getRegistrationDeadline() != null && now.isAfter(activity.getRegistrationDeadline());
    }
}
