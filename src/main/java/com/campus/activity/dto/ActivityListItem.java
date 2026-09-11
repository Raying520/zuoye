package com.campus.activity.dto;

import com.campus.activity.entity.Activity;

/**
 * 活动列表项：活动实体 + 当前报名人数（FR4 列表展示用）
 */
public class ActivityListItem {

    private final Activity activity;
    private final long enrolledCount;

    public ActivityListItem(Activity activity, long enrolledCount) {
        this.activity = activity;
        this.enrolledCount = enrolledCount;
    }

    public Activity getActivity() { return activity; }

    public long getEnrolledCount() { return enrolledCount; }
}
