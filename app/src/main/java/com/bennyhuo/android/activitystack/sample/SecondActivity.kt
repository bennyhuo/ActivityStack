package com.bennyhuo.android.activitystack.sample

import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import com.bennyhuo.android.activitystack.TaskManager
import kotlinx.android.synthetic.main.activity_second.*

class SecondActivity: AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_second)

        getCurrentActivity.setOnClickListener {
            Log.d("as", "current: ${TaskManager.getCurrentActivity()}")
            Log.d("as", "foreground: ${TaskManager.isForeground()}")
        }

        gotoThird.setOnClickListener {
            startActivity(Intent(this, ThirdActivity::class.java).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK))
        }
    }

}