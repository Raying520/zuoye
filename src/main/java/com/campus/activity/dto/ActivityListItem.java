package com.campus.activity.dto;

import com.campus.activity.entity.Activity;

/**
 * 活动列表项：活动实体 + 当前报名人数（FR4 列表展示用）
 * V2.0 区分已通过人数与候补人数
 */
public class ActivityListItem {

    private final Activity activity;
    private final long enrolledCount;  // 已通过人数（实际参与者）
    private final long waitingCount;   // 候补人数

    public ActivityListItem(Activity activity, long enrolledCount, long waitingCount) {
        this.activity = activity;
        this.enrolledCount = enrolledCount;
        this.waitingCount = waitingCount;
    }

    public Activity getActivity() { return activity; }

    public long getEnrolledCount() { return enrolledCount; }

    public long getWaitingCount() { return waitingCount; }
}
