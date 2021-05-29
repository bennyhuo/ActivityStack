package com.bennyhuo.android.activitystack;

import android.app.Activity;

public interface OnActivityChangedListener {
    void onActivityChanged(Activity previousActivity, Activity currentActivity);
}