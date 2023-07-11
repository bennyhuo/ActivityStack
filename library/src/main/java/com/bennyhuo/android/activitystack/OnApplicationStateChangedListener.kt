package com.bennyhuo.android.activitystack

interface OnApplicationStateChangedListener {
    /**
     * application goes background with all activities in CREATED state.
     */
    fun onBackground() {}

    /**
     * application goes foreground with one of the activities above CREATED state.
     */
    fun onForeground() {}

    /**
     * called when application exits abnormally.
     */
    fun onTerminate(throwable: Throwable?) {}
}