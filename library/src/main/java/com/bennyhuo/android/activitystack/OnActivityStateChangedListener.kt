package com.bennyhuo.android.activitystack

import android.app.Activity

fun interface OnActivityStateChangedListener {
    fun onActivityStateChanged(
        activity: Activity,
        previousState: ActivityState,
        currentState: ActivityState
    )
}