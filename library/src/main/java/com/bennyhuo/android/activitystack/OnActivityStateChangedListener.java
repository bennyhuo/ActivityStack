package com.bennyhuo.android.activitystack;

import android.app.Activity;

public interface OnActivityStateChangedListener {
    void onActivityStateChanged(Activity activity, ActivityState previousState, ActivityState currentState);
}