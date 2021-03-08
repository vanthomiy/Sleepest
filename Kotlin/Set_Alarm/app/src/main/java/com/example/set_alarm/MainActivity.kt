package com.example.set_alarm

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.provider.AlarmClock
import android.widget.Button

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val set_alarm = findViewById<Button>(R.id.set_alarm)

        set_alarm.setOnClickListener {
            val intent = Intent(AlarmClock.ACTION_SET_ALARM)
            intent.putExtra(AlarmClock.EXTRA_MESSAGE, "My New Alarm")
            intent.putExtra(AlarmClock.EXTRA_HOUR, 11)
            intent.putExtra(AlarmClock.EXTRA_MINUTES, 32)
            intent.putExtra(AlarmClock.EXTRA_DAYS, arrayListOf(2, 3, 4))
            intent.putExtra(AlarmClock.EXTRA_HOUR, 14)

            startActivity(intent)
        }
    }
}