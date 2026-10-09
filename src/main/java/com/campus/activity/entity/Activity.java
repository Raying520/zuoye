package com.campus.activity.entity;

import jakarta.persistence.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

/**
 * 活动实体（对应软件设计 4. 数据设计：活动表 activity）
 */
@Entity
@Table(name = "activity")
@EntityListeners(AuditingEntityListener.class)
public class Activity {

    public enum Status {
        OPEN,       // 报名中
        CLOSED,     // 已结束
        CANCELLED   // 已取消
    }

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 100)
    private String title;

    @Column(length = 2000)
    private String description;

    /** 活动时间 */
    @Column(name = "activity_time", nullable = false)
    private LocalDateTime activityTime;

    /** 活动地点 */
    @Column(nullable = false, length = 200)
    private String place;

    /** 容量上限 */
    @Column(nullable = false)
    private Integer capacity;

    /** 参与条件说明（FR8） */
    @Column(name = "requirements", length = 2000)
    private String requirements;

    /** 报名截止时间（FR15） */
    @Column(name = "registration_deadline")
    private LocalDateTime registrationDeadline;

    /** 是否需要教师审核（FR9） */
    @Column(name = "require_audit", nullable = false)
    private Boolean requireAudit = false;

    /** 是否允许候补（FR10/FR11） */
    @Column(name = "allow_waitlist", nullable = false)
    private Boolean allowWaitlist = false;

    /** 活动状态：OPEN/CLOSED/CANCELLED */
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private Status status = Status.OPEN;

    /** 发布教师 ID */
    @Column(name = "teacher_id", nullable = false)
    private Long teacherId;

    @CreatedDate
    @Column(name = "create_time")
    private LocalDateTime createTime;

    // --- getters & setters ---

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public LocalDateTime getActivityTime() { return activityTime; }
    public void setActivityTime(LocalDateTime activityTime) { this.activityTime = activityTime; }

    public String getPlace() { return place; }
    public void setPlace(String place) { this.place = place; }

    public Integer getCapacity() { return capacity; }
    public void setCapacity(Integer capacity) { this.capacity = capacity; }

    public String getRequirements() { return requirements; }
    public void setRequirements(String requirements) { this.requirements = requirements; }

    public LocalDateTime getRegistrationDeadline() { return registrationDeadline; }
    public void setRegistrationDeadline(LocalDateTime registrationDeadline) { this.registrationDeadline = registrationDeadline; }

    public Boolean getRequireAudit() { return requireAudit; }
    public void setRequireAudit(Boolean requireAudit) { this.requireAudit = requireAudit; }

    public Boolean getAllowWaitlist() { return allowWaitlist; }
    public void setAllowWaitlist(Boolean allowWaitlist) { this.allowWaitlist = allowWaitlist; }

    public Status getStatus() { return status; }
    public void setStatus(Status status) { this.status = status; }

    public Long getTeacherId() { return teacherId; }
    public void setTeacherId(Long teacherId) { this.teacherId = teacherId; }

    public LocalDateTime getCreateTime() { return createTime; }
    public void setCreateTime(LocalDateTime createTime) { this.createTime = createTime; }
}
