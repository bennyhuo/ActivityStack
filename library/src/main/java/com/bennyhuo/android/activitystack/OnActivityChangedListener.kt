package com.bennyhuo.android.activitystack

import android.app.Activity

fun interface OnActivityChangedListener {
    fun onActivityChanged(previousActivity: Activity?, currentActivity: Activity?)
}