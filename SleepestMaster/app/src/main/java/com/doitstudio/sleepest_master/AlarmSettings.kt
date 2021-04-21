package com.doitstudio.sleepest_master

import android.app.AlertDialog
import android.content.DialogInterface
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.*
import androidx.core.view.isVisible
import androidx.lifecycle.asLiveData
import com.appyvet.rangebar.RangeBar
import com.doitstudio.sleepest_master.storage.db.AlarmEntity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import java.time.DayOfWeek
import java.time.LocalTime
import kotlin.collections.List as List1

class AlarmSettings : AppCompatActivity() {

    private val repository by lazy { (this.applicationContext as MainApplication).dbRepository }
    private val scope: CoroutineScope = MainScope()

    //region
    lateinit var sBar : SeekBar //Selecting the sleep amount
    lateinit var rBar : RangeBar //Selecting the wake up range https://github.com/Fedorkz/material-range-bar
    lateinit var tViewSleepAmount : TextView //Display the selected sleep amount
    lateinit var tViewWakeupTime : TextView //Display the selected wake up range
    lateinit var tViewExpandAlarmSettings: TextView
    lateinit var viewExtendedAlarmSettings : View //Display extended alarm settings
    lateinit var btnWeekdaySelect : Button //Popup window for selecting the weekdays for alarm
    lateinit var swAlarmActive : Switch //Select whether alarm is on or off
    lateinit var alarmSettings : AlarmEntity
    val alarmEntityLiveData by lazy { repository.alarmFlow.asLiveData()}
    //endregion

    val negativeButtonClick = {dialog: DialogInterface, which: Int ->
        Toast.makeText(applicationContext,
                android.R.string.no, Toast.LENGTH_SHORT).show()
    }
    
    fun saveAlarmIsActive(isActive: Boolean) {
        scope.launch {
            repository.updateIsActive(isActive, 1) }
    }

    fun saveSleepAmount(time: LocalTime) {
        tViewSleepAmount.text = " " + time.toString() + " Stunden"
        scope.launch {
            repository.updateSleepDuration(time.toSecondOfDay(), 1) }
    }

    fun saveWakeupRange(wakeupEarly: LocalTime, wakeupLate: LocalTime) {
        tViewWakeupTime.text = " " + wakeupEarly.toString() + " - " + wakeupLate.toString() + " Uhr"
        scope.launch {
            repository.updateWakeupEarly(wakeupEarly.toSecondOfDay(), 1)
            repository.updateWakeupLate(wakeupLate.toSecondOfDay(), 1)
        }
    }

    fun saveAlarmDaysWeek(daysOfWeek: ArrayList<DayOfWeek>) {
        scope.launch {
            repository.updateActiveDayOfWeek(daysOfWeek, 1)
        }
    }

    fun getActiveAlarmDays(): BooleanArray {
        val activeDays = mutableListOf<Boolean>()
        activeDays.add(alarmSettings.activeDayOfWeek.contains(DayOfWeek.MONDAY))
        activeDays.add(alarmSettings.activeDayOfWeek.contains(DayOfWeek.TUESDAY))
        activeDays.add(alarmSettings.activeDayOfWeek.contains(DayOfWeek.WEDNESDAY))
        activeDays.add(alarmSettings.activeDayOfWeek.contains(DayOfWeek.THURSDAY))
        activeDays.add(alarmSettings.activeDayOfWeek.contains(DayOfWeek.FRIDAY))
        activeDays.add(alarmSettings.activeDayOfWeek.contains(DayOfWeek.SATURDAY))
        activeDays.add(alarmSettings.activeDayOfWeek.contains(DayOfWeek.SUNDAY))
        return activeDays.toBooleanArray()
    }

    suspend fun SetupAlarmSettings() {
        alarmSettings = repository.getAlarmById(1).first()

        val wakeupTime = LocalTime.ofSecondOfDay(alarmSettings.sleepDuration.toLong())
        val wakeupEarly = LocalTime.ofSecondOfDay(alarmSettings.wakeupEarly.toLong())
        val wakeupLate = LocalTime.ofSecondOfDay(alarmSettings.wakeupLate.toLong())
        val isActive = alarmSettings.isActive

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
        swAlarmActive.isChecked = isActive

        //UpdateViews
        tViewSleepAmount.text = " " + wakeupTime.toString() + " Stunden"
        tViewWakeupTime.text = " " + wakeupEarly.toString() + " - " + wakeupLate.toString() + " Uhr"
    }

    /*
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
     */

    fun selectActiveDaysOfWeek() {
        //WeekdayDialog().show(supportFragmentManager, "Wochentage")

        val items = arrayOf("Monday", "Tuesday", "Wednesday", "Thursday", "Friday", "Saturday", "Sunday")
        val daysOfWeek = arrayOf(DayOfWeek.MONDAY, DayOfWeek.TUESDAY, DayOfWeek.WEDNESDAY, DayOfWeek.THURSDAY, DayOfWeek.FRIDAY, DayOfWeek.SATURDAY, DayOfWeek.SUNDAY)
        val selectedList = ArrayList<Int>()
        val builder = AlertDialog.Builder(this)
        val activeDays = getActiveAlarmDays()

        if (activeDays[0]) { selectedList.add(0) }
        if (activeDays[1]) { selectedList.add(1) }
        if (activeDays[2]) { selectedList.add(2) }
        if (activeDays[3]) { selectedList.add(3) }
        if (activeDays[4]) { selectedList.add(4) }
        if (activeDays[5]) { selectedList.add(5) }
        if (activeDays[6]) { selectedList.add(6) }

        builder.setTitle("Alarmdays")
                .setMultiChoiceItems(items, activeDays) {
                    _, which, isChecked ->
                    if (isChecked) {
                        selectedList.add(which) }
                    else if (selectedList.contains(which)) {
                        selectedList.remove(Integer.valueOf(which)) }
                }
                .setPositiveButton("Ok") {
                    _, _ ->
                    val selectedDays = ArrayList<DayOfWeek>()
                    for (j in selectedList.indices) { selectedDays.add(daysOfWeek[selectedList[j]]) }
                    //Toast.makeText(applicationContext, android.R.string.yes, Toast.LENGTH_SHORT).show()
                    saveAlarmDaysWeek(selectedDays)
                }
                .setNegativeButton("Cancel", DialogInterface.OnClickListener(negativeButtonClick))
                .show()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_alarm_settings)

        //region
        sBar = findViewById(R.id.sBar_sleepAmount)
        rBar = findViewById(R.id.rBar_wakeupRange)
        tViewSleepAmount = findViewById(R.id.tV_sleepAmountSelection)
        tViewWakeupTime = findViewById(R.id.tV_swakeupRangeSelection)
        tViewExpandAlarmSettings = findViewById(R.id.tV_expandAlarmSettings)
        viewExtendedAlarmSettings = findViewById(R.id.cL_extendedAlarmSettings)
        btnWeekdaySelect = findViewById(R.id.btn_alarmDaysWeek)
        swAlarmActive = findViewById(R.id.sw_alarmOnOff)
        //endregion

        alarmEntityLiveData.observe(this) {
            alarmList ->
            alarmSettings = alarmList[0]
        }

        swAlarmActive.setOnClickListener {
            saveAlarmIsActive(swAlarmActive.isChecked)
        }

        btnWeekdaySelect.setOnClickListener {
            //onClickWeek()
            selectActiveDaysOfWeek()
        }

        tViewExpandAlarmSettings.setOnClickListener {
            if (viewExtendedAlarmSettings.isVisible) {
                viewExtendedAlarmSettings.isVisible = false
            }
            else {
                viewExtendedAlarmSettings.isVisible = true
            }
        }

        scope.launch {
            //repository.insertAlarm(alarmEntity)
            SetupAlarmSettings()
        }

        /*
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
         */

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