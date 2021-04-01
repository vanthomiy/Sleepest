package com.doitstudio.sleepest_master

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.SeekBar
import android.widget.TextView
import com.appyvet.rangebar.RangeBar
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.launch
import java.time.LocalTime
import java.util.*

class AlarmSettings : AppCompatActivity() {


    private val repository by lazy { (this.applicationContext as MainApplication).dataStoreRepository }
    private val scope: CoroutineScope = MainScope()


    lateinit var sBar : SeekBar
    lateinit var rBar : RangeBar //https://github.com/Fedorkz/material-range-bar
    lateinit var tViewSleepAmount : TextView
    lateinit var tViewWakeupTime : TextView

    lateinit var sleepDuration : Date //in LocalTime ändern
    lateinit var wakeupEarly : Date //in LocalTime ändern
    lateinit var wakeupLate : Date //in LocalTime ändern


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_alarm_settings)

        sBar = findViewById(R.id.seekBar)
        rBar = findViewById(R.id.rangebar)
        tViewSleepAmount = findViewById(R.id.textView_schlafdauerAuswahl)
        tViewWakeupTime = findViewById(R.id.textView_aufwachzeitpunktAuswahl)
        tViewSleepAmount.text = " Select Time"
        tViewWakeupTime.text = " Select Time Range"

        sBar.setOnSeekBarChangeListener(object: SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(sBar: SeekBar?, progress: Int, fromUser: Boolean) {
                val progressTemp = progress*0.5 + 5
                var minutes = 0
                if (progressTemp % 1 == 0.5) { minutes = 30 }
                sleepDuration = Date(2021, 4, 1, progressTemp.toInt(), minutes)
                val time = LocalTime.of(progressTemp.toInt(), minutes)
                val temp = time.toSecondOfDay()
                tViewSleepAmount.text =  " " + time.hour.toString() + " h " + time.minute.toString() + " min"
                //tViewSleepAmount.text =  " " + alarmTime.hours.toString() + " h " + alarmTime.minutes.toString() + " min"

                scope.launch {
                    repository.updateSleepDuration(time.toSecondOfDay())
                }

            }

            override fun onStartTrackingTouch(sBar: SeekBar) {
                val progressTemp = sBar.progress*0.5 + 5
                var minutes = 0
                if (progressTemp % 1 == 0.5) { minutes = 30 }
                sleepDuration = Date(2021, 4, 1, progressTemp.toInt(), minutes)
                val time = LocalTime.of(progressTemp.toInt(), minutes)
                tViewSleepAmount.text =  " " + time.hour.toString() + " h " + time.minute.toString() + " min"
                //tViewSleepAmount.text = " " + alarmTime.hours.toString() + " h " + alarmTime.minutes.toString() + " min"
            }

            override fun onStopTrackingTouch(sBar: SeekBar) {
                val progressTemp = sBar.progress*0.5 + 5
                var minutes = 0
                if (progressTemp % 1 == 0.5) { minutes = 30 }
                sleepDuration = Date(2021, 4, 1, progressTemp.toInt(), minutes)
                val time = LocalTime.of(progressTemp.toInt(), minutes)
                tViewSleepAmount.text =  " " + time.hour.toString() + " h " + time.minute.toString() + " min"
                //tViewSleepAmount.text = " " + alarmTime.hours.toString() + " h " + alarmTime.minutes.toString() + " min"
            }
        })

        rBar.setOnRangeBarChangeListener(object: RangeBar.OnRangeBarChangeListener {
            override fun onRangeChangeListener(
                rangeBar: RangeBar?,
                leftPinIndex: Int,
                rightPinIndex: Int,
                leftPinValue: String?,
                rightPinValue: String?
            ) {

                var minutes = 0
                if (rBar.leftPinValue.contains(".5")) { minutes = 30 }
                wakeupEarly = Date(2021, 4, 1, rBar.leftPinValue.toFloat().toInt(), minutes)

                if (rBar.rightPinValue.contains(".5")) { minutes = 30 } else {minutes = 0 }
                wakeupLate = Date(2021, 4, 1, rBar.rightPinValue.toFloat().toInt(), minutes)

                tViewWakeupTime.text = "  " + wakeupEarly.hours.toString() + ":" + wakeupEarly.minutes.toString() + " - " +
                        wakeupLate.hours.toString() + ":" + wakeupLate.minutes.toString()
            }
        })
    }
}