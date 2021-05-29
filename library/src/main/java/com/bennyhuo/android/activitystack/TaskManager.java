package com.bennyhuo.android.activitystack;

import android.app.Activity;
import android.app.Application;
import android.app.Application.ActivityLifecycleCallbacks;
import android.content.ComponentCallbacks;
import android.content.Intent;
import android.content.res.Configuration;
import android.os.Bundle;

import com.bennyhuo.android.activitystack.ActivityInfo.ActivityState;

import java.lang.Thread.UncaughtExceptionHandler;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map.Entry;

/**
 * Created by benny on 9/17/16.
 */
public class TaskManager {
    public static final String TAG = "TaskManager";

    final static TaskManager INSTANCE = new TaskManager();

    private final HashMap<Integer, Task> tasks = new HashMap<>();
    private Task currentTask = Task.EMPTY_TASK;

    private Application application;

    public interface OnApplicationStateChangedListener {
        int TERMINATED_BY_RECENT_TASKS = 0;
        int TERMINATED_BY_CRASH = 1;
        int TERMINATED_ON_VM_DOWN = 2;

        /**
         * application goes background with all activities in CREATED state.
         */
        void onBackground();

        /**
         * application goes foreground with one of the activities above CREATED state.
         */
        void onForeground();

        /**
         * redirect low memory callback of Application.
         */
        void onLowMemory();

        /**
         * called when application exits abnormally.
         */
        void onTerminate(int reason, Throwable throwable);

        void onActivityChanged(Activity previousActivity, Activity currentActivity);
    }

    public interface OnActivityChangedListener {
        void onActivityChanged(Activity previousActivity, Activity currentActivity);
    }

    public interface OnActivityStateChangedListener {
        void onActivityStateChanged(Activity activity, ActivityState previousState, ActivityState currentState);
    }

    private HashSet<OnApplicationStateChangedListener> onApplicationStateChangedListeners = new HashSet<>();

    private void notifyApplicationStateBackground() {
        HashSet<OnApplicationStateChangedListener> onApplicationStateChangedListeners = (HashSet<OnApplicationStateChangedListener>) this.onApplicationStateChangedListeners.clone();
        for (OnApplicationStateChangedListener onApplicationStateChangedListener : onApplicationStateChangedListeners) {
            onApplicationStateChangedListener.onBackground();
        }
    }

    private void notifyApplicationStateForeground() {
        HashSet<OnApplicationStateChangedListener> onApplicationStateChangedListeners = (HashSet<OnApplicationStateChangedListener>) this.onApplicationStateChangedListeners.clone();
        for (OnApplicationStateChangedListener onApplicationStateChangedListener : onApplicationStateChangedListeners) {
            onApplicationStateChangedListener.onForeground();
        }
    }

    private void notifyLowMemory() {
        HashSet<OnApplicationStateChangedListener> onApplicationStateChangedListeners = (HashSet<OnApplicationStateChangedListener>) this.onApplicationStateChangedListeners.clone();
        for (OnApplicationStateChangedListener onApplicationStateChangedListener : onApplicationStateChangedListeners) {
            onApplicationStateChangedListener.onLowMemory();
        }
    }

    void notifyTerminated(int reason, Throwable throwable) {
        HashSet<OnApplicationStateChangedListener> onApplicationStateChangedListeners = (HashSet<OnApplicationStateChangedListener>) this.onApplicationStateChangedListeners.clone();
        for (OnApplicationStateChangedListener onApplicationStateChangedListener : onApplicationStateChangedListeners) {
            onApplicationStateChangedListener.onTerminate(reason, throwable);
        }
    }

    public static void addOnApplicationStateChangedListener(OnApplicationStateChangedListener onApplicationStateChangedListener) {
        INSTANCE.onApplicationStateChangedListeners.add(onApplicationStateChangedListener);
    }

    public static void removeOnApplicationStateChangedListener(OnApplicationStateChangedListener onApplicationStateChangedListener) {
        INSTANCE.onApplicationStateChangedListeners.remove(onApplicationStateChangedListener);
    }

    private HashSet<OnActivityChangedListener> onActivityChangedListeners = new HashSet<>();

    private void notifyActivityChanged(Activity previousActivity, Activity currentActivity) {
        HashSet<OnApplicationStateChangedListener> onApplicationStateChangedListeners = (HashSet<OnApplicationStateChangedListener>) this.onApplicationStateChangedListeners.clone();
        for (OnApplicationStateChangedListener onApplicationStateChangedListener : onApplicationStateChangedListeners) {
            onApplicationStateChangedListener.onActivityChanged(previousActivity, currentActivity);
        }

        HashSet<OnActivityChangedListener> onActivityChangedListeners = (HashSet<OnActivityChangedListener>) this.onActivityChangedListeners.clone();
        for (OnActivityChangedListener onActivityChangedListener : onActivityChangedListeners) {
            onActivityChangedListener.onActivityChanged(previousActivity, currentActivity);
        }
    }

    public static void addOnActivityChangedListener(OnActivityChangedListener onActivityChangedListener) {
        INSTANCE.onActivityChangedListeners.add(onActivityChangedListener);
    }

    public static void removeOnActivityChangedListener(OnActivityChangedListener onActivityChangedListener) {
        INSTANCE.onActivityChangedListeners.remove(onActivityChangedListener);
    }

    private HashMap<Class<? extends Activity> , HashSet<OnActivityStateChangedListener>> onActivityStateChangedListeners = new HashMap<>();

    private void notifyActivityStateChanged(Activity activity, ActivityState previousState, ActivityState currentState) {
        HashSet<OnActivityStateChangedListener> listeners = this.onActivityStateChangedListeners.get(activity.getClass());
        if (listeners == null) {
            listeners = this.onActivityStateChangedListeners.get(Activity.class);
        }
        if (listeners != null) {
            HashSet<OnActivityStateChangedListener> onActivityStateChangedListeners = (HashSet<OnActivityStateChangedListener>) listeners.clone();
            for (OnActivityStateChangedListener onActivityStateChangedListener : onActivityStateChangedListeners) {
                onActivityStateChangedListener.onActivityStateChanged(activity, previousState, currentState);
            }
        }
    }

    public static void addOnActivityStateChangedListener(Class<? extends Activity> activityClass, OnActivityStateChangedListener onActivityStateChangedListener) {
        HashSet<OnActivityStateChangedListener> listeners = INSTANCE.onActivityStateChangedListeners.get(activityClass);
        if (listeners == null) {
            listeners = new HashSet<>(1);
            INSTANCE.onActivityStateChangedListeners.put(activityClass, listeners);
        }
        listeners.add(onActivityStateChangedListener);
    }

    public static void removeAllOnActivityStateChangedListener(Class<? extends Activity> activityClass) {
        INSTANCE.onActivityStateChangedListeners.remove(activityClass);
    }

    public static void removeOnActivityStateChangedListener(Class<? extends Activity> activityClass, OnActivityStateChangedListener onActivityStateChangedListener) {
        HashSet<OnActivityStateChangedListener> listeners = INSTANCE.onActivityStateChangedListeners.get(activityClass);
        listeners.remove(onActivityStateChangedListener);
        if(listeners.isEmpty()){
            INSTANCE.onActivityStateChangedListeners.remove(activityClass);
        }
    }

    private TaskManager() {

    }

    void onCreate(Application application) {
        if (this.application != null && this.application == application) return;
        this.application = application;
        application.registerActivityLifecycleCallbacks(lifecycleCallbacks);
        application.registerComponentCallbacks(componentCallbacks);
        originalExceptionHandler = Thread.getDefaultUncaughtExceptionHandler();
        Thread.setDefaultUncaughtExceptionHandler(uncaughtExceptionHandler);
    }

    void onDestroy() {
        if (application == null) return;
        application.unregisterActivityLifecycleCallbacks(lifecycleCallbacks);
        application.unregisterComponentCallbacks(componentCallbacks);
        Thread.setDefaultUncaughtExceptionHandler(originalExceptionHandler);
    }

    private ComponentCallbacks componentCallbacks = new ComponentCallbacks() {
        @Override
        public void onConfigurationChanged(Configuration newConfig) {

        }

        @Override
        public void onLowMemory() {
            TaskManager.INSTANCE.notifyLowMemory();
        }
    };

    private ActivityLifecycleCallbacks lifecycleCallbacks = new ActivityLifecycleCallbacks() {
        @Override
        public void onActivityCreated(Activity activity, Bundle savedInstanceState) {
            updateActivityState(activity, ActivityState.CREATED);
        }

        @Override
        public void onActivityStarted(Activity activity) {
            updateActivityState(activity, ActivityState.STARTED);
        }

        @Override
        public void onActivityResumed(Activity activity) {
            updateActivityState(activity, ActivityState.RESUMED);
        }

        @Override
        public void onActivityPaused(Activity activity) {
            updateActivityState(activity, ActivityState.STARTED);
        }

        @Override
        public void onActivityStopped(Activity activity) {
            updateActivityState(activity, ActivityState.CREATED);
        }

        @Override
        public void onActivitySaveInstanceState(Activity activity, Bundle outState) {

        }

        @Override
        public void onActivityDestroyed(Activity activity) {
            updateActivityState(activity, ActivityState.DESTROYED);
        }
    };

    private Thread.UncaughtExceptionHandler originalExceptionHandler;

    private final Thread.UncaughtExceptionHandler uncaughtExceptionHandler = new UncaughtExceptionHandler() {
        @Override
        public void uncaughtException(Thread thread, Throwable ex) {
            notifyTerminated(OnApplicationStateChangedListener.TERMINATED_BY_CRASH, ex);
            onDestroy();
            if (originalExceptionHandler != null) {
                originalExceptionHandler.uncaughtException(thread, ex);
            }
        }
    };

    private void updateActivityState(Activity activity, ActivityState activityState) {
        ActivityState previousState = ActivityState.DESTROYED;

        Activity previousActivity = getCurrentActivity();

        Task task = tasks.get(activity.getTaskId());
        if (task == null) {
            task = new Task(activity.getTaskId());
            tasks.put(activity.getTaskId(), task);
        }
        boolean found = false;
        for (ActivityInfo activityInfo : task) {
            if (activityInfo.activity == activity) {
                previousState = activityInfo.activityState;
                activityInfo.activityState = activityState;
                if (activityState == ActivityState.DESTROYED) {
                    task.remove(activityInfo);
                    if (task.isEmpty()) {
                        tasks.remove(task.getTaskId());
                    }
                }
                found = true;
                break;
            }
        }
        if (activityState == ActivityState.CREATED && !found) {
            // previousState == DESTROYED
            task.push(new ActivityInfo(activity));
        }
        Task currentTask = Task.EMPTY_TASK;
        for (Entry<Integer, Task> taskEntry : tasks.entrySet()) {
            Task entryValue = taskEntry.getValue();
            if (entryValue.isEmpty()) continue;
            int stateIndex = entryValue.getTaskState().ordinal();
            /* >benny: [16-11-14 10:03] task invisible */
            if (stateIndex < ActivityState.STARTED.ordinal()) continue;
            if (currentTask == Task.EMPTY_TASK) currentTask = entryValue;
            /* >benny: [16-11-14 10:04] only one task can be visible, so state cannot be both started or resumed  */
            if (stateIndex > currentTask.getTaskState().ordinal()) currentTask = entryValue;
        }
        setCurrentTask(currentTask);

        Activity currentActivity = getCurrentActivity();

        if(previousActivity != currentActivity) {
            notifyActivityChanged(previousActivity, currentActivity);
        }

        notifyActivityStateChanged(activity, previousState, activityState);
    }

    private void setCurrentTask(Task currentTask) {
        if (this.currentTask != currentTask) {
            this.currentTask = currentTask;
            if (this.currentTask.isEmpty()) {
                notifyApplicationStateBackground();
            } else {
                notifyApplicationStateForeground();
            }
        }
    }

    public static boolean isForeground() {
        if (INSTANCE.currentTask.isEmpty()) return false;
        ActivityState activityState = INSTANCE.currentTask.getTaskState();
        return activityState == ActivityState.STARTED || activityState == ActivityState.RESUMED;
    }

    public static Activity getCurrentActivity() {
        if (INSTANCE.currentTask.isEmpty()) return null;
        return INSTANCE.currentTask.peek().activity;
    }

    public static Activity getGroundActivity() {
        if (INSTANCE.currentTask.size() > 1) {
            return INSTANCE.currentTask.get(INSTANCE.currentTask.size() - 2).activity;
        } else if (INSTANCE.currentTask.size() == 1) {
            return INSTANCE.currentTask.peek().activity;
        } else {
            return null;
        }
    }

    public static void recreateActivities() {
        Activity currentActivity = getCurrentActivity();
        for (Entry<Integer, Task> entry : INSTANCE.tasks.entrySet()) {
            for (ActivityInfo activityInfo : entry.getValue()) {
                if (activityInfo.activity != currentActivity)
                    activityInfo.activity.recreate();
            }
        }

        if (currentActivity != null) {
            Intent intent = currentActivity.getIntent();
            if (intent == null) {
                intent = new Intent(currentActivity, currentActivity.getClass());
            }
            currentActivity.finish();
            currentActivity.startActivity(intent);
            currentActivity.overridePendingTransition(0, 0);
        }
    }

    public static List<Task> getAllTasks() {
        List<Task> snapshot = new ArrayList<>();
        for (Task task : INSTANCE.tasks.values()) {
            snapshot.add(task.copy());
        }
        return snapshot;
    }

    public static void setup(Application application) {
        TaskManager.INSTANCE.onCreate(application);
    }

}
