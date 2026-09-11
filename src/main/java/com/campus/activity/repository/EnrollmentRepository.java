package com.campus.activity.repository;

import com.campus.activity.entity.Enrollment;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

/**
 * 报名记录数据访问接口
 */
public interface EnrollmentRepository extends JpaRepository<Enrollment, Long> {

    /** 查询某活动的报名人数（BR3 容量校验） */
    long countByActivityId(Long activityId);

    /** 查询学生是否已报名某活动（BR4 重复报名校验） */
    boolean existsByActivityIdAndUserId(Long activityId, Long userId);

    /** 查询某活动某学生的报名记录（取消报名用） */
    Optional<Enrollment> findByActivityIdAndUserId(Long activityId, Long userId);

    /** 查询某学生的所有报名记录（FR6：我的报名） */
    List<Enrollment> findByUserIdOrderByCreateTimeDesc(Long userId);

    /** 查询某活动的所有报名记录（FR6：教师查看名单） */
    List<Enrollment> findByActivityIdOrderByCreateTimeDesc(Long activityId);
}
