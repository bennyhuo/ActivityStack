[![License: MIT](https://img.shields.io/badge/License-MIT-yellow.svg)](LICENSE)[![Maven Central](https://maven-badges.herokuapp.com/maven-central/com.bennyhuo/activity-stack/badge.svg)](https://maven-badges.herokuapp.com/maven-central/com.bennyhuo/activity-stack)

# ActivityStack

Record activity task stack by observing the lifecycle changes of Activity. You can use this library to check whether your application is in background, or get the current resuming activity.

Listeners provided for easily observing the changes of the status of the Activities and themselves.

# How to use

## Import

I have deployed these modules to maven central, you may add this in your build.gradle:

```
implementation("com.bennyhuo:activity-stack:1.2.0")
```

## Usages

Setup in Application first:

```
TaskManager.init(this)
```

Add listeners:

```
TaskManager.addOnActivityChangedListener { previousActivity, currentActivity ->
    Log.d("as", "Activity changed: ${previousActivity?.javaClass?.simpleName} -> ${currentActivity?.javaClass?.simpleName}")
}

TaskManager.addOnActivityStateChangedListener(MainActivity::class.java) { activity, oldState, newState ->
    Log.d("as", "$activity: $oldState -> $newState")
}
```

If you want to remove those listeners later, you can use the methods like 'removeXXXListener'. Pay attention to SAM conversion, remove a lambda added before is not possible.

Dump the Activity Task stack:

```
TaskManager.allTasks.forEach {
    Log.i("as", it.toString())
}
```

Get current Activity:

```
val current = TaskManager.currentActivity // current resumed activity or null if in the background
```

Test whether current Application is in the foreground:

```
val isForeground = TaskManager.isForeground // false if running in the background.
```
