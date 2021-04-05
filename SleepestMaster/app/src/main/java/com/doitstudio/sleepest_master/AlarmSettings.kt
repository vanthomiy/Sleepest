package com.doitstudio.sleepest_master

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.SeekBar
import android.widget.TextView
import androidx.lifecycle.asLiveData
import com.appyvet.rangebar.RangeBar
import com.faskn.lib.PieChart
import com.faskn.lib.Slice
import com.faskn.lib.buildChart
import kotlinx.android.synthetic.main.activity_alarm_settings.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.launch
import java.lang.reflect.WildcardType
import java.time.LocalTime
import kotlin.concurrent.fixedRateTimer
import kotlin.properties.Delegates
import kotlin.random.Random

class AlarmSettings : AppCompatActivity() {

    private val repository by lazy { (this.applicationContext as MainApplication).dataStoreRepository }
    private val scope: CoroutineScope = MainScope()
    private val alarmSettingsLiveData by lazy{ repository.alarmFlow.asLiveData()}

    lateinit var sBar : SeekBar
    lateinit var rBar : RangeBar //https://github.com/Fedorkz/material-range-bar
    lateinit var tViewSleepAmount : TextView
    lateinit var tViewWakeupTime : TextView
    var firstSetupBars by Delegates.notNull<Boolean>()

    lateinit var text: String

    fun saveSleepAmount(time: LocalTime) {
        tViewSleepAmount.text = " " + time.toString() + " Stunden"
        scope.launch {
            repository.updateSleepDuration(time.toSecondOfDay()) }
    }

    fun saveWakeupRange(wakeupEarly: LocalTime, wakeupLate: LocalTime) {
        tViewWakeupTime.text = " " + wakeupEarly.toString() + " - " + wakeupLate.toString() + " Uhr"
        scope.launch {
            repository.updateWakeUpLate(wakeupLate.toSecondOfDay())
            repository.updateWakeUpEarly(wakeupEarly.toSecondOfDay())
        }
    }

    fun SetupAlarmSettings() {
        // Listener for live data changes. Is used for the setup when building up the activity and reasigning the values
        alarmSettingsLiveData.observe(this)
        {
            alarmSettings ->
            alarmSettings.sleepDuration
            alarmSettings.wakeupEarly
            alarmSettings.wakeupLate

            if (firstSetupBars) {

                var wakeupTime = LocalTime.ofSecondOfDay(alarmSettings.sleepDuration.toLong())
                if (wakeupTime == LocalTime.of(0, 0)) { wakeupTime = LocalTime.of(8, 0) }

                var wakeupEarly = LocalTime.ofSecondOfDay(alarmSettings.wakeupEarly.toLong())
                if (wakeupEarly < LocalTime.of(5, 0)) { wakeupEarly = LocalTime.of(6, 0) }

                var wakeupLate = LocalTime.ofSecondOfDay(alarmSettings.wakeupLate.toLong())
                if (wakeupLate == LocalTime.of(0, 0)) { wakeupLate = LocalTime.of(9, 0) }

                // Setup the sleepAmount bar
                if (wakeupTime.minute == 30) { sBar.progress = (wakeupTime.hour - 5) * 2 + 1 }
                else { sBar.progress = (wakeupTime.hour - 5) * 2 }

                //Setup the wakeupRange bar
                var rBarLeft = 0.0
                var rBarRight = 0.0

                if (wakeupEarly.minute == 30) { rBarLeft = wakeupEarly.hour.toDouble() + 0.5 }
                else { rBarLeft = wakeupEarly.hour.toDouble() }

                if (wakeupLate.minute == 30) { rBarRight = wakeupLate.hour.toDouble() + 0.5 }
                else { rBarRight = wakeupLate.hour.toDouble() }

                rBar.setRangePinsByValue(rBarLeft.toFloat(), rBarRight.toFloat())

                //UpdateViews
                tViewSleepAmount.text = " " + wakeupTime.toString() + " Stunden"
                tViewWakeupTime.text = " " + wakeupEarly.toString() + " - " + wakeupLate.toString() + " Uhr"

                // Prevent from looping while the user manually changes the bar values
                firstSetupBars = false
            }
        }
    }

    private fun provideSlices(): ArrayList<Slice> {
        return arrayListOf(
                Slice(
                        Random.nextInt(1000, 3000).toFloat(),
                        R.color.purple,
                        "Non-REM 1"
                ),
                Slice(
                        Random.nextInt(1000, 2000).toFloat(),
                        R.color.purple_200,
                        "Non-REM 2"
                ),
                Slice(
                        Random.nextInt(1000, 5000).toFloat(),
                        R.color.purple_500,
                        "REM"
                ),
                Slice(
                        Random.nextInt(1000, 10000).toFloat(),
                        R.color.purple_700,
                        "Non-Sleep"
                ),
        )
    }

    fun func(index: Float) {
        when (index) {
            0.0.toFloat() -> text = "Non-REM 1"
            1.0.toFloat() -> text = "Non-REM 2"
            2.0.toFloat() -> text = "REM"
            else -> {
                text = "Wach"
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_alarm_settings)

        // SeekBar and Rangebar
        sBar = findViewById(R.id.seekBar)
        rBar = findViewById(R.id.rangebar)
        tViewSleepAmount = findViewById(R.id.textView_schlafdauerAuswahl)
        tViewWakeupTime = findViewById(R.id.textView_aufwachzeitpunktAuswahl)
        firstSetupBars = true

        // Piechart https://github.com/furkanaskin/ClickablePieChart
        val pieChartDSL = buildChart {
            slices { provideSlices() }
            sliceWidth { 80f }
            sliceStartPoint { 0f }
            clickListener { angle, index ->
                //func(index)
            }
        }
        chart.setPieChart(pieChartDSL)
        chart.showLegend(legendLayout)

        if (alarmSettingsLiveData.value?.sleepDuration == null) {
            saveSleepAmount(LocalTime.of(7, 0))
            saveWakeupRange(LocalTime.of(6, 0), LocalTime.of(9, 0))
        }

        SetupAlarmSettings()

        sBar.setOnSeekBarChangeListener(object: SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(sBar: SeekBar?, progress: Int, fromUser: Boolean) {
                val progressTemp = progress*0.5 + 5
                var minutes = 0
                if (progressTemp % 1 == 0.5) { minutes = 30 }

                saveSleepAmount(LocalTime.of(progressTemp.toInt(), minutes))
            }

            override fun onStartTrackingTouch(sBar: SeekBar) {
                val progressTemp = sBar.progress*0.5 + 5
                var minutes = 0
                if (progressTemp % 1 == 0.5) { minutes = 30 }

                saveSleepAmount(LocalTime.of(progressTemp.toInt(), minutes))
            }

            override fun onStopTrackingTouch(sBar: SeekBar) {
                val progressTemp = sBar.progress*0.5 + 5
                var minutes = 0
                if (progressTemp % 1 == 0.5) { minutes = 30 }

                saveSleepAmount(LocalTime.of(progressTemp.toInt(), minutes))
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

                var minutesLeft = 0
                var minutesRight = 0
                if (rBar.leftPinValue.contains(".5")) { minutesLeft = 30 }
                if (rBar.rightPinValue.contains(".5")) { minutesRight = 30 }

                saveWakeupRange(LocalTime.of(rBar.leftPinValue.toFloat().toInt(), minutesLeft), LocalTime.of(rBar.rightPinValue.toFloat().toInt(), minutesRight))
            }
        })
    }
}