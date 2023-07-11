package com.bennyhuo.android.activitystack

import android.app.Activity

/**
 * Created by benny at 2023/7/11 10:45.
 */
inline fun <reified T : Activity> TaskManager.finishActivities() {
    TaskManager.finishActivities { it.javaClass == T::class.java }
}