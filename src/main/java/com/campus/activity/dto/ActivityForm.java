package com.campus.activity.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDateTime;

/**
 * 活动发布表单（FR3）
 */
public class ActivityForm {

    @NotBlank(message = "活动标题不能为空")
    private String title;

    private String description;

    @NotNull(message = "活动时间不能为空")
    @DateTimeFormat(pattern = "yyyy-MM-dd'T'HH:mm")
    private LocalDateTime activityTime;

    @NotBlank(message = "活动地点不能为空")
    private String place;

    @NotNull(message = "容量上限不能为空")
    @Min(value = 1, message = "容量上限至少为 1")
    private Integer capacity;

    private String requirements;

    @NotNull(message = "报名截止时间不能为空")
    @DateTimeFormat(pattern = "yyyy-MM-dd'T'HH:mm")
    private LocalDateTime registrationDeadline;

    private Boolean requireAudit;

    private Boolean allowWaitlist;

    // --- getters & setters ---

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
}
