package com.bennyhuo.android.activitystack.sample

import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import com.bennyhuo.android.activitystack.TaskManager
import com.bennyhuo.android.activitystack.finishActivities
import kotlinx.android.synthetic.main.activity_third.*

class ThirdActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_third)

        getCurrentActivity.setOnClickListener {
            Log.d("as", "current: ${TaskManager.currentActivity}")
            Log.d("as", "foreground: ${TaskManager.isForeground}")
        }

        gotoMain.setOnClickListener {
            startActivity(
                Intent(
                    this,
                    MainActivity::class.java
                ).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK).addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
            )
        }

        finishOtherActivities.setOnClickListener {
//            TaskManager.finishActivities {
//                Log.d("debugger", "finishActivities $it")
//                it.javaClass == MainActivity::class.java || it.javaClass == SecondActivity::class.java 
//            }
            
            TaskManager.finishActivities<MainActivity>()
        }

    }

}