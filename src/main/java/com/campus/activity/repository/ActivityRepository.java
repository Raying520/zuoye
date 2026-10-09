package com.campus.activity.repository;

import com.campus.activity.entity.Activity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

/**
 * 活动数据访问接口
 */
public interface ActivityRepository extends JpaRepository<Activity, Long> {

    /** 教师查看自己发布的活动（FR7：我的活动） */
    List<Activity> findByTeacherIdOrderByActivityTimeDesc(Long teacherId);

    /** 学生浏览活动列表：按活动时间倒序（FR4） */
    List<Activity> findAllByOrderByActivityTimeDesc();

    /** 学生浏览活动列表（V3.0：过滤被管理员下架的活动） */
    List<Activity> findByPlatformStatusOrderByActivityTimeDesc(Activity.PlatformStatus platformStatus);

    /** 管理员监管视图：按教师查询（V3.0） */
    List<Activity> findByTeacherIdAndPlatformStatusOrderByActivityTimeDesc(Long teacherId, Activity.PlatformStatus platformStatus);
}
