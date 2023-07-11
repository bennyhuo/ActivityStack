package com.bennyhuo.android.activitystack.sample

import android.app.Activity
import android.app.Application
import android.util.Log
import com.bennyhuo.android.activitystack.TaskManager

class App: Application() {
    override fun onCreate() {
        super.onCreate()
        TaskManager.init(this)

        TaskManager.addOnActivityChangedListener { previousActivity, currentActivity ->
            Log.d("as", "Activity changed: ${previousActivity?.javaClass?.simpleName} -> ${currentActivity?.javaClass?.simpleName}")
        }

        TaskManager.addOnActivityStateChangedListener(Activity::class.java) { activity, oldState, newState ->
            Log.d("as", "$activity: $oldState -> $newState")

            TaskManager.allTasks.forEach {
                Log.i("as", it.toString())
            }
        }
    }
}