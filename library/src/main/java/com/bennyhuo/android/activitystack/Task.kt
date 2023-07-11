package com.bennyhuo.android.activitystack

import android.app.Activity
import java.util.Stack

/**
 * Created by benny on 11/14/16.
 */
class Task(val taskId: Int) : Stack<ActivityInfo>() {

    val taskState: ActivityState
        get() {
            var state = ActivityState.CREATED.ordinal
            for (activityInfo in this) {
                state = Math.max(state, activityInfo.activityState.ordinal)
            }
            return ActivityState.values()[state]
        }

    internal fun copy(): Task {
        val task = Task(taskId)
        task.addAll(this)
        return task
    }

    fun finishActivities(condition: (Activity) -> Boolean): Boolean {
        var result = false
        for (activityInfo in this) {
            if (condition(activityInfo.activity)) {
                activityInfo.activity.finish()
                result = true
            }
        }
        return result
    }

    override fun toString(): String {
        return "{" + taskId + ": " + super.toString() + "}"
    }

    companion object {
        internal const val EMPTY_TASK_ID = -1
        val EMPTY_TASK = Task(EMPTY_TASK_ID)
    }
}