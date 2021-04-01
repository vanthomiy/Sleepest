package com.doitstudio.sleepest_master

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.SeekBar
import android.widget.TextView
import com.appyvet.rangebar.RangeBar

class AlarmSettings : AppCompatActivity() {

    lateinit var sBar : SeekBar
    lateinit var rBar : RangeBar //https://github.com/Fedorkz/material-range-bar
    lateinit var tViewSleepAmount : TextView
    lateinit var tViewWakeupTime : TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_alarm_settings)

        sBar = findViewById(R.id.seekBar)
        rBar = findViewById(R.id.rangebar)
        tViewSleepAmount = findViewById(R.id.textView_schlafdauerAuswahl)
        tViewWakeupTime = findViewById(R.id.textView_aufwachzeitpunktAuswahl)
        tViewSleepAmount.text = "  " + (sBar.progress*0.5 + 5).toString() + " Stunden"
        tViewWakeupTime.text = "  " + rBar.leftPinValue + " - " + rBar.rightPinValue + "Uhr"

        sBar.setOnSeekBarChangeListener(object: SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(sBar: SeekBar?, progress: Int, fromUser: Boolean) {
                tViewSleepAmount.text = "  " + (progress*0.5 + 5).toString() + " Stunden"
            }

            override fun onStartTrackingTouch(sBar: SeekBar) {
                tViewSleepAmount.text = "  " + (sBar.progress*0.5 + 5).toString() + " Stunden"
            }

            override fun onStopTrackingTouch(sBar: SeekBar) {
                tViewSleepAmount.text = "  " + (sBar.progress*0.5 + 5).toString() + " Stunden"
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
                tViewWakeupTime.text = "  " + rBar.leftPinValue + " - " + rBar.rightPinValue + " Uhr"
            }
        })
    }
}