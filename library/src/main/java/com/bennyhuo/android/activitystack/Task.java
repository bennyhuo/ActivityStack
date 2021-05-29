package com.bennyhuo.android.activitystack;


import com.bennyhuo.android.activitystack.ActivityInfo.ActivityState;

import java.util.Stack;

/**
 * Created by benny on 11/14/16.
 */
public class Task extends Stack<ActivityInfo> {
    public static final String TAG = "Task";

    public static final int EMPTY_TASK_ID = -1;
    public static final Task EMPTY_TASK = new Task(EMPTY_TASK_ID);

    private final int taskId;

    public Task(int taskId) {
        this.taskId = taskId;
    }

    public Task copy() {
        Task task = new Task(taskId);
        task.addAll(this);
        return task;
    }

    public int getTaskId() {
        return taskId;
    }

    public ActivityState getTaskState(){
        int state = ActivityState.CREATED.ordinal();
        for (ActivityInfo activityInfo : this) {
            state = Math.max(state, activityInfo.activityState.ordinal());
        }
        return ActivityState.values()[state];
    }

    @Override
    public String toString() {
        return "{" + taskId + ": " + super.toString() + "}";
    }
}
