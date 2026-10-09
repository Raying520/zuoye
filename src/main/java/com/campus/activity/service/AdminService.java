package com.campus.activity.service;

import com.campus.activity.dto.UserSession;
import com.campus.activity.entity.Activity;
import com.campus.activity.entity.AdminOperationLog;
import com.campus.activity.entity.User;
import com.campus.activity.repository.AdminOperationLogRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * 管理员业务服务（V3.0）
 * 集中处理账号启用/停用、活动平台状态变更、操作日志记录。
 */
@Service
public class AdminService {

    private final UserService userService;
    private final ActivityService activityService;
    private final AdminOperationLogRepository logRepository;

    public AdminService(UserService userService,
                        ActivityService activityService,
                        AdminOperationLogRepository logRepository) {
        this.userService = userService;
        this.activityService = activityService;
        this.logRepository = logRepository;
    }

    /** 停用账号（FR16） */
    @Transactional
    public void disableUser(Long userId, String reason, UserSession admin) {
        userService.setStatus(userId, User.Status.DISABLED);
        log(admin.getId(), "DISABLE_USER", "USER", userId, reason);
    }

    /** 启用账号（FR16） */
    @Transactional
    public void enableUser(Long userId, String reason, UserSession admin) {
        userService.setStatus(userId, User.Status.ENABLED);
        log(admin.getId(), "ENABLE_USER", "USER", userId, reason);
    }

    /** 标记活动待整改（FR19） */
    @Transactional
    public void flagActivity(Long activityId, String reason, UserSession admin) {
        activityService.setPlatformStatus(activityId, Activity.PlatformStatus.FLAGGED);
        log(admin.getId(), "FLAG_ACTIVITY", "ACTIVITY", activityId, reason);
    }

    /** 下架活动（FR19） */
    @Transactional
    public void suspendActivity(Long activityId, String reason, UserSession admin) {
        activityService.setPlatformStatus(activityId, Activity.PlatformStatus.SUSPENDED);
        log(admin.getId(), "SUSPEND_ACTIVITY", "ACTIVITY", activityId, reason);
    }

    /** 恢复活动（FR19） */
    @Transactional
    public void restoreActivity(Long activityId, String reason, UserSession admin) {
        activityService.setPlatformStatus(activityId, Activity.PlatformStatus.NORMAL);
        log(admin.getId(), "RESTORE_ACTIVITY", "ACTIVITY", activityId, reason);
    }

    /** 查询操作日志（FR20） */
    public List<AdminOperationLog> findLogs() {
        return logRepository.findAllByOrderByCreateTimeDesc();
    }

    /** 平台统计概览（FR21） */
    public PlatformStats getStats() {
        return new PlatformStats();
    }

    private void log(Long operatorId, String operationType, String targetType, Long targetId, String reason) {
        AdminOperationLog log = new AdminOperationLog();
        log.setOperatorId(operatorId);
        log.setOperationType(operationType);
        log.setTargetType(targetType);
        log.setTargetId(targetId);
        log.setReason(reason);
        logRepository.save(log);
    }

    /**
     * 平台统计（FR21）
     * 使用内部类承载首页关键指标。
     */
    public static class PlatformStats {
        // 占位：实际数值由调用方通过各 Service 汇总后设置
        public long userCount;
        public long activityCount;
        public long enrollmentCount;
        public long pendingEnrollmentCount;
        public long flaggedActivityCount;
        public long suspendedActivityCount;
    }
}
