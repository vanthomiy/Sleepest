package com.example.set_alarm

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.provider.AlarmClock
import android.widget.Button
import android.widget.EditText

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val set_alarm = findViewById<Button>(R.id.set_alarm)
        val alarm_time_hour = findViewById<EditText>(R.id.text_input_hour)
        val alarm_time_minuite = findViewById<EditText>(R.id.text_input_minuit)

        set_alarm.setOnClickListener {
            access_alarm(alarm_time_hour.text.toString().toInt(), alarm_time_minuite.text.toString().toInt())
            /*
            val intent = Intent(AlarmClock.ACTION_SET_ALARM)
            intent.putExtra(AlarmClock.EXTRA_MESSAGE, "Sleepest Wecker")
            intent.putExtra(AlarmClock.EXTRA_HOUR, wakeup_hour)
            intent.putExtra(AlarmClock.EXTRA_MINUTES, wakeup_minuite)
            intent.putExtra(AlarmClock.EXTRA_SKIP_UI, true)
            startActivity(intent)
             */
        }

    }

    fun access_alarm(hour: Int, minuite: Int) {
        val intent = Intent(AlarmClock.ACTION_SET_ALARM)
        intent.putExtra(AlarmClock.EXTRA_MESSAGE, "Sleepest Wecker")
        intent.putExtra(AlarmClock.EXTRA_HOUR, hour)
        intent.putExtra(AlarmClock.EXTRA_MINUTES, minuite)
        intent.putExtra(AlarmClock.EXTRA_SKIP_UI, true)
        startActivity(intent)
    }
}