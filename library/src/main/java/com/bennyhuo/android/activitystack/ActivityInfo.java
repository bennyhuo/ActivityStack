package com.bennyhuo.android.activitystack;

import android.app.Activity;

public class ActivityInfo {
    public enum ActivityState {
        DESTROYED,
        CREATED,
        STARTED,
        RESUMED
    }

    Activity activity;
    ActivityState activityState = ActivityState.CREATED;

    public ActivityInfo(Activity activity) {
        this.activity = activity;
    }

    @Override
    public String toString() {
        return "(" + activity.getClass().getSimpleName() + "@" + activity.getTaskId() +
                ":" + activityState + ')';
    }
}
