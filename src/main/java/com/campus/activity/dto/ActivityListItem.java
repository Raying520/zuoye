package com.campus.activity.dto;

import com.campus.activity.entity.Activity;

import java.time.LocalDateTime;

/**
 * 活动列表项：活动实体 + 当前报名人数（FR4 列表展示用）
 * V2.0 区分已通过人数与候补人数
 * V2.1 增加报名截止是否已过的标记
 */
public class ActivityListItem {

    private final Activity activity;
    private final long enrolledCount;  // 已通过人数（实际参与者）
    private final long waitingCount;   // 候补人数
    private final boolean deadlinePassed;

    public ActivityListItem(Activity activity, long enrolledCount, long waitingCount, boolean deadlinePassed) {
        this.activity = activity;
        this.enrolledCount = enrolledCount;
        this.waitingCount = waitingCount;
        this.deadlinePassed = deadlinePassed;
    }

    public Activity getActivity() { return activity; }

    public long getEnrolledCount() { return enrolledCount; }

    public long getWaitingCount() { return waitingCount; }

    public boolean isDeadlinePassed() { return deadlinePassed; }
}
