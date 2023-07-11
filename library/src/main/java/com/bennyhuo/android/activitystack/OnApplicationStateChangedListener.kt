package com.bennyhuo.android.activitystack;

public interface OnApplicationStateChangedListener {
    /**
     * application goes background with all activities in CREATED state.
     */
    void onBackground();

    /**
     * application goes foreground with one of the activities above CREATED state.
     */
    void onForeground();

    /**
     * called when application exits abnormally.
     */
    void onTerminate(Throwable throwable);
}