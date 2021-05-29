[![License: MIT](https://img.shields.io/badge/License-MIT-yellow.svg)](LICENSE)

# ActivityStack

Record activity task stack by observing the lifecycle changes of Activity. You can use this library to check whether your application is in background, or get the current resuming activity.

Listeners provided for easily observing the changes of the status of the Activities and themselves.

# How to use

## Import

I have deployed these modules to maven central, you may add this in your build.gradle:

```
implementation("com.bennyhuo:activity-stack:1.0")
```

### SNAPSHOT

If you want to try the dev version, add this to your build.gradle:

```
repositories {
    maven {
        url "https://oss.sonatype.org/service/local/repositories/snapshots/"
    }
}

dependencies {
    implementation("com.bennyhuo:activity-stack:1.1-SNAPSHOT")
}
```

## Usages

Setup in Application first:

```
TaskManager.setup(this)
```

Add listeners:

```
TaskManager.addOnActivityChangedListener { previousActivity, currentActivity ->
    Log.d("as", "Activity changed: ${previousActivity?.javaClass?.simpleName} -> ${currentActivity?.javaClass?.simpleName}")
}

TaskManager.addOnActivityStateChangedListener(MainActivity::class.java) {
        activity, oldState, newState ->
    Log.d("as", "$activity: $oldState -> $newState")
}
```

If you want to remove those listeners later, you can use the methods like 'removeXXXListener'. Pay attention to SAM conversion, remove a lambda added before is not possible.

Dump the Activity Task stack:

```
TaskManager.getAllTasks().forEach {
    Log.i("as", it.toString())
}
```

Get current Activity:

```
val current = TaskManager.getCurrentActivity() // current resumed activity or null if in the background
```

Test whether current Application is in the foreground:

```
val isForeground = TaskManager.isForeground() // false if running in the background.
```
