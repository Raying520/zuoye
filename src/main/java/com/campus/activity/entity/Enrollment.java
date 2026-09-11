package com.campus.activity.entity;

import jakarta.persistence.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

/**
 * 报名记录实体（对应软件设计 4. 数据设计：报名记录表 enrollment）
 * 联合唯一约束 (activity_id, user_id) 在数据库层面兜底防重复报名（BR4）
 */
@Entity
@Table(name = "enrollment",
       uniqueConstraints = @UniqueConstraint(
           name = "uk_enrollment_activity_user",
           columnNames = {"activity_id", "user_id"}
       ))
@EntityListeners(AuditingEntityListener.class)
public class Enrollment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "activity_id", nullable = false)
    private Long activityId;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @CreatedDate
    @Column(name = "create_time")
    private LocalDateTime createTime;

    // --- getters & setters ---

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Long getActivityId() { return activityId; }
    public void setActivityId(Long activityId) { this.activityId = activityId; }

    public Long getUserId() { return userId; }
    public void setUserId(Long userId) { this.userId = userId; }

    public LocalDateTime getCreateTime() { return createTime; }
    public void setCreateTime(LocalDateTime createTime) { this.createTime = createTime; }
}
