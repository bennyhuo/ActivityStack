package com.bennyhuo.android.activitystack.sample

import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import com.bennyhuo.android.activitystack.TaskManager
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity: AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        getCurrentActivity.setOnClickListener {
            Log.d("as", "current: ${TaskManager.getCurrentActivity()}")
            Log.d("as", "foreground: ${TaskManager.isForeground()}")
        }

        gotoSecond.setOnClickListener {
            startActivity(Intent(this, SecondActivity::class.java))
        }
    }

}