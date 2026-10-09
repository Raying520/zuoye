package com.campus.activity.repository;

import com.campus.activity.entity.AdminOperationLog;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

/**
 * 管理员操作日志数据访问接口（V3.0 FR20）
 */
public interface AdminOperationLogRepository extends JpaRepository<AdminOperationLog, Long> {

    List<AdminOperationLog> findAllByOrderByCreateTimeDesc();
}
