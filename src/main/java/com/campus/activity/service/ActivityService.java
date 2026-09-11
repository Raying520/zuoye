package com.campus.activity.service;

import com.campus.activity.dto.ActivityForm;
import com.campus.activity.dto.ActivityListItem;
import com.campus.activity.dto.UserSession;
import com.campus.activity.entity.Activity;
import com.campus.activity.repository.ActivityRepository;
import com.campus.activity.repository.EnrollmentRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

/**
 * 活动业务服务（FR3、FR4、FR7 的业务规则实现）
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
        activity.setTitle(form.getTitle().trim());
        activity.setDescription(form.getDescription());
        activity.setActivityTime(form.getActivityTime());
        activity.setPlace(form.getPlace().trim());
        activity.setCapacity(form.getCapacity());
        activity.setStatus(Activity.Status.OPEN);
        activity.setTeacherId(teacher.getId());
        return activityRepository.save(activity);
    }

    /** 查询所有活动，按活动时间倒序（FR4） */
    public List<Activity> findAll() {
        return activityRepository.findAllByOrderByActivityTimeDesc();
    }

    /** 查询所有活动并附带当前报名人数（FR4 列表展示） */
    public List<ActivityListItem> findAllWithEnrollmentCount() {
        List<Activity> activities = activityRepository.findAllByOrderByActivityTimeDesc();
        List<ActivityListItem> result = new ArrayList<>(activities.size());
        for (Activity activity : activities) {
            long count = enrollmentRepository.countByActivityId(activity.getId());
            result.add(new ActivityListItem(activity, count));
        }
        return result;
    }

    /** 教师查询自己发布的活动（FR7） */
    public List<Activity> findByTeacher(Long teacherId) {
        return activityRepository.findByTeacherIdOrderByActivityTimeDesc(teacherId);
    }

    /** 按 ID 查询活动 */
    public Activity findById(Long id) {
        return activityRepository.findById(id).orElse(null);
    }
}
