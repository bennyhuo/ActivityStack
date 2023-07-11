package com.bennyhuo.android.activitystack

import android.app.Activity

class ActivityInfo(val activity: Activity) {
    var activityState = ActivityState.CREATED

    override fun toString(): String {
        return "(" + activity.javaClass.simpleName + "@" + activity.taskId +
            ":" + activityState + ')'
    }
}