package com.doitstudio.sleepest_master

import android.app.AlertDialog
import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.lifecycle.asLiveData
import com.appyvet.rangebar.RangeBar
import com.doitstudio.sleepest_master.storage.db.AlarmEntity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import java.time.DayOfWeek
import java.time.LocalTime

class AlarmInstance(applicationContext: Context) : Fragment() {
    private val repository by lazy { (applicationContext as MainApplication).dbRepository }
    private val scope: CoroutineScope = MainScope()

    lateinit var sBar: SeekBar //Selecting the sleep amount
    lateinit var rBar: RangeBar //Selecting the wake up range https://github.com/Fedorkz/material-range-bar
    lateinit var tViewSleepAmount: TextView //Display the selected sleep amount
    lateinit var tViewWakeupTime: TextView  //Display the selected wake up range
    lateinit var tViewExpandAlarmSettings: TextView //Used for expanding the alarmview
    lateinit var tViewAlarmViewTopic : TextView //Topic of the alarm
    lateinit var viewExtendedAlarmSettings : View //Display extended alarm settings
    lateinit var btnWeekdaySelect : Button //Popup window for selecting the weekdays for alarm
    lateinit var swAlarmActive : Switch //Select whether alarm is on or off
    lateinit var alarmSettings : AlarmEntity
    val alarmEntityLiveData by lazy { repository.alarmFlow.asLiveData()}

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
            repository.updateWakeupLate(wakeupLate.toSecondOfDay(), 1) }
    }

    fun saveAlarmDaysWeek(daysOfWeek: ArrayList<DayOfWeek>) {
        scope.launch {
            repository.updateActiveDayOfWeek(daysOfWeek, 1)
        }
    }

    fun getActiveAlarmDays(): BooleanArray {
        val activeDays = mutableListOf<Boolean>()
        for (i in 0..6) {
            if (alarmSettings.activeDayOfWeek.contains(DayOfWeek.values()[i])) {
                activeDays.add(true)
            } else {
                activeDays.add(false)
            }
        }
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

    fun selectActiveDaysOfWeek() {
        val items = arrayOf("Monday", "Tuesday", "Wednesday", "Thursday", "Friday", "Saturday", "Sunday")
        val daysOfWeek = DayOfWeek.values()
        val selectedList = ArrayList<Int>()
        val builder = AlertDialog.Builder(this.context)
        val activeDays = getActiveAlarmDays()

        for (i in activeDays.indices) { if (activeDays[i]) { selectedList.add(i) } } // Make sure we dont lose pre-selected information

        builder.setTitle("Alarmdays")
                .setMultiChoiceItems(items, activeDays) {
                    _, which, isChecked ->
                    if (isChecked) { selectedList.add(which) }
                    else if (selectedList.contains(which)) { selectedList.remove(Integer.valueOf(which)) }
                }
                .setPositiveButton("Save") {
                    _, _ ->
                    val selectedDays = ArrayList<DayOfWeek>()
                    for (j in selectedList.indices) { selectedDays.add(daysOfWeek[selectedList[j]]) }
                    if (selectedDays.isNotEmpty()) {
                        saveAlarmDaysWeek(selectedDays)
                        //Toast.makeText(applicationContext, "Speichern erfolgreich", Toast.LENGTH_SHORT).show()
                    }
                    else { //Toast.makeText(applicationContext, "Speichern fehlgeschlagen\nMindestens einen Tag auswählen! ", Toast.LENGTH_SHORT).show()
                    }
                }
                .setNegativeButton("Cancel") {
                    _, _ ->
                    //Toast.makeText(applicationContext, "Verworfen", Toast.LENGTH_SHORT).show()
                    }
                .show()
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.alarm_entity, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        sBar = view.findViewById(R.id.sBar_sleepAmount)
        rBar = view.findViewById(R.id.rBar_wakeupRange)
        tViewSleepAmount = view.findViewById(R.id.tV_sleepAmountSelection)
        tViewWakeupTime = view.findViewById(R.id.tV_swakeupRangeSelection)
        tViewExpandAlarmSettings = view.findViewById(R.id.tV_expandAlarmSettings)
        tViewAlarmViewTopic = view.findViewById(R.id.tV_AlarmViewTopic)
        viewExtendedAlarmSettings = view.findViewById(R.id.cL_extendedAlarmSettings)
        btnWeekdaySelect = view.findViewById(R.id.btn_alarmDaysWeek)
        swAlarmActive = view.findViewById(R.id.sw_alarmOnOff)

        tViewExpandAlarmSettings.setOnClickListener {
            viewExtendedAlarmSettings.isVisible = !viewExtendedAlarmSettings.isVisible
            tViewExpandAlarmSettings.isVisible = !viewExtendedAlarmSettings.isVisible
        }

        tViewAlarmViewTopic.setOnClickListener {
            viewExtendedAlarmSettings.isVisible = !viewExtendedAlarmSettings.isVisible
            tViewExpandAlarmSettings.isVisible = !viewExtendedAlarmSettings.isVisible
        }

        alarmEntityLiveData.observe(viewLifecycleOwner) {
            alarmList ->
            alarmSettings = alarmList[0]
        }

        swAlarmActive.setOnClickListener {
            saveAlarmIsActive(swAlarmActive.isChecked)
        }

        btnWeekdaySelect.setOnClickListener {
            selectActiveDaysOfWeek()
        }

        scope.launch {
            SetupAlarmSettings()
        }

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