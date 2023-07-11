package com.bennyhuo.android.activitystack;

import android.app.Activity;

public class ActivityInfo {

    final Activity activity;
    ActivityState activityState = ActivityState.CREATED;

    public ActivityInfo(Activity activity) {
        this.activity = activity;
    }

    public Activity getActivity() {
        return activity;
    }

    public ActivityState getActivityState() {
        return activityState;
    }

    @Override
    public String toString() {
        return "(" + activity.getClass().getSimpleName() + "@" + activity.getTaskId() +
                ":" + activityState + ')';
    }
}
