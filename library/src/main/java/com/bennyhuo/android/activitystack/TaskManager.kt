@file:Suppress("UNCHECKED_CAST")
package com.bennyhuo.android.activitystack

import android.app.Activity
import android.app.Application
import android.app.Application.ActivityLifecycleCallbacks
import android.content.Intent
import android.os.Bundle


/**
 * Created by benny on 9/17/16.
 */
object TaskManager {
    private val tasks = HashMap<Int, Task>()
    private var currentTask = Task.EMPTY_TASK

    val allTasks: List<Task>
        get() {
            val snapshot: MutableList<Task> = ArrayList()
            for (task in tasks.values) {
                snapshot.add(task.copy())
            }
            return snapshot
        }

    private lateinit var application: Application

    val isForeground: Boolean
        get() {
            if (currentTask.isEmpty()) return false
            val activityState = currentTask.taskState
            return activityState === ActivityState.STARTED || activityState === ActivityState.RESUMED
        }

    val currentActivity: Activity?
        get() = currentTask.lastOrNull()?.activity

    val groundActivity: Activity?
        get() = currentTask.getOrNull(currentTask.size - 2)?.activity

    private val onApplicationStateChangedListeners = HashSet<OnApplicationStateChangedListener>()

    private fun notifyApplicationStateBackground() {
        onApplicationStateChangedListeners.toTypedArray().forEach { listener ->
            listener.onBackground()
        }
    }

    private fun notifyApplicationStateForeground() {
        onApplicationStateChangedListeners.toTypedArray().forEach { listener ->
            listener.onForeground()
        }
    }

    fun notifyTerminated(throwable: Throwable?) {
        onApplicationStateChangedListeners.toTypedArray().forEach { listener ->
            listener.onTerminate(throwable)
        }
    }

    private val onActivityChangedListeners = HashSet<OnActivityChangedListener>()

    private fun notifyActivityChanged(previousActivity: Activity?, currentActivity: Activity?) {
        onActivityChangedListeners.toTypedArray().forEach { listener ->
            listener.onActivityChanged(previousActivity, currentActivity)
        }
    }

    private val onActivityStateChangedListeners =
        HashMap<Class<out Activity>, HashSet<OnActivityStateChangedListener>>()

    private fun notifyActivityStateChanged(
        activity: Activity,
        previousState: ActivityState,
        currentState: ActivityState
    ) {
        var activityClass: Class<*>? = activity.javaClass
        while (activityClass != null) {
            val listeners = onActivityStateChangedListeners[activityClass]
            if (listeners != null) {
                listeners.toTypedArray().forEach { listener ->
                    listener.onActivityStateChanged(activity, previousState, currentState)
                }
                return
            }

            if (activityClass == Activity::class.java) {
                break
            }
            activityClass = activityClass.superclass
        }
    }

    fun init(application: Application) {
        if (this::application.isInitialized) return
        this.application = application
        application.registerActivityLifecycleCallbacks(lifecycleCallbacks)
        originalExceptionHandler = Thread.getDefaultUncaughtExceptionHandler()
        Thread.setDefaultUncaughtExceptionHandler(uncaughtExceptionHandler)
    }

    fun release() {
        if (!this::application.isInitialized) return
        application.unregisterActivityLifecycleCallbacks(lifecycleCallbacks)
        Thread.setDefaultUncaughtExceptionHandler(originalExceptionHandler)
    }

    private val lifecycleCallbacks = object : ActivityLifecycleCallbacks {
            override fun onActivityCreated(activity: Activity, savedInstanceState: Bundle?) {
                updateActivityState(activity, ActivityState.CREATED)
            }

            override fun onActivityStarted(activity: Activity) {
                updateActivityState(activity, ActivityState.STARTED)
            }

            override fun onActivityResumed(activity: Activity) {
                updateActivityState(activity, ActivityState.RESUMED)
            }

            override fun onActivityPaused(activity: Activity) {
                updateActivityState(activity, ActivityState.STARTED)
            }

            override fun onActivityStopped(activity: Activity) {
                updateActivityState(activity, ActivityState.CREATED)
            }

            override fun onActivitySaveInstanceState(activity: Activity, outState: Bundle) {}
            override fun onActivityDestroyed(activity: Activity) {
                updateActivityState(activity, ActivityState.DESTROYED)
            }
        }
    
    private var originalExceptionHandler: Thread.UncaughtExceptionHandler? = null
    
    private val uncaughtExceptionHandler = Thread.UncaughtExceptionHandler { thread, ex ->
        notifyTerminated(ex)
        release()
        originalExceptionHandler?.uncaughtException(thread, ex)
    }

    private fun updateActivityState(activity: Activity, activityState: ActivityState) {
        var previousState = ActivityState.DESTROYED
        val previousActivity = currentActivity
        val taskId = activity.taskId
        var task: Task? = null
        if (taskId == Task.EMPTY_TASK_ID) {
            if (activityState !== ActivityState.DESTROYED) {
                // only handle destroy state for activities when task id is -1.  
                return
            }
            LOOP@ for (value in tasks.values) {
                for (activityInfo in value) {
                    if (activityInfo.activity === activity) {
                        task = value
                        break@LOOP
                    }
                }
            }
            if (task == null) {
                // task not found, this activity is not record in any task, just return.
                return
            }
        } else {
            task = tasks[taskId]
        }
        if (task == null) {
            task = Task(taskId)
            tasks[taskId] = task
        }
        var found = false
        for (activityInfo in task) {
            if (activityInfo.activity === activity) {
                previousState = activityInfo.activityState
                activityInfo.activityState = activityState
                if (activityState === ActivityState.DESTROYED) {
                    task.remove(activityInfo)
                    if (task.isEmpty()) {
                        tasks.remove(task.taskId)
                    }
                }
                found = true
                break
            }
        }
        if (activityState === ActivityState.CREATED && !found) {
            // previousState == DESTROYED
            task.push(ActivityInfo(activity))
        }
        var currentTask = Task.EMPTY_TASK
        for (entryValue in tasks.values) {
            if (entryValue.isEmpty()) continue
            val stateIndex = entryValue.taskState.ordinal
            /* task invisible */
            if (stateIndex < ActivityState.STARTED.ordinal) continue
            if (currentTask === Task.EMPTY_TASK) currentTask = entryValue
            /* only one task can be visible, so state cannot be both started or resumed */
            if (stateIndex > currentTask.taskState.ordinal) currentTask = entryValue
        }
        
        setCurrentTask(currentTask)
        val currentActivity = this.currentActivity
        if (previousActivity !== currentActivity) {
            notifyActivityChanged(previousActivity, currentActivity)
        }
        notifyActivityStateChanged(activity, previousState, activityState)
    }

    private fun setCurrentTask(currentTask: Task) {
        if (this.currentTask !== currentTask) {
            this.currentTask = currentTask
            if (this.currentTask.isEmpty()) {
                notifyApplicationStateBackground()
            } else {
                notifyApplicationStateForeground()
            }
        }
    }

    fun addOnApplicationStateChangedListener(listener: OnApplicationStateChangedListener) {
        onApplicationStateChangedListeners.add(listener)
    }

    fun removeOnApplicationStateChangedListener(listener: OnApplicationStateChangedListener) {
        onApplicationStateChangedListeners.remove(listener)
    }

    fun addOnActivityChangedListener(listener: OnActivityChangedListener) {
        onActivityChangedListeners.add(listener)
    }

    fun removeOnActivityChangedListener(listener: OnActivityChangedListener) {
        onActivityChangedListeners.remove(listener)
    }

    fun addOnActivityStateChangedListener(
        activityClass: Class<out Activity>,
        listener: OnActivityStateChangedListener
    ) {
        var listeners = onActivityStateChangedListeners[activityClass]
        if (listeners == null) {
            listeners = HashSet(1)
            onActivityStateChangedListeners[activityClass] = listeners
        }
        listeners.add(listener)
    }

    fun removeAllOnActivityStateChangedListener(activityClass: Class<out Activity>) {
        onActivityStateChangedListeners.remove(activityClass)
    }

    fun removeOnActivityStateChangedListener(
        activityClass: Class<out Activity>,
        listener: OnActivityStateChangedListener
    ) {
        val listeners = onActivityStateChangedListeners[activityClass]!!
        listeners.remove(listener)
        if (listeners.isEmpty()) {
            onActivityStateChangedListeners.remove(activityClass)
        }
    }

    fun finishActivities(condition: (Activity) -> Boolean): Boolean {
        var result = false
        for (value in tasks.values) {
            // avoid short circuit
            result = value.finishActivities(condition) or result
        }
        return result
    }

    fun finishActivities(activityClass: Class<out Activity?>): Boolean {
        return finishActivities { activity: Activity -> activity.javaClass == activityClass }
    }

    inline fun <reified T : Activity> finishActivities() {
        finishActivities(T::class.java)
    }

    fun recreateActivities() {
        val currentActivity = this.currentActivity
        tasks.values.forEach {task -> 
            task.forEach { activityInfo ->
                if (activityInfo.activity !== currentActivity) activityInfo.activity.recreate()
            }
        }

        if (currentActivity != null) {
            val intent = currentActivity.intent ?: Intent(currentActivity, currentActivity.javaClass)
            currentActivity.finish()
            currentActivity.startActivity(intent)
            currentActivity.overridePendingTransition(0, 0)
        }
    }
}