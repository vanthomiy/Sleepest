package com.doitstudio.sleepest_master

import android.app.AlertDialog
import android.content.Context
import android.os.Bundle
import android.text.InputType
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

class AlarmInstance(val applicationContext: Context, private var alarmId: Int) : Fragment() {

    private val repository by lazy { (applicationContext as MainApplication).dbRepository }
    private val scope: CoroutineScope = MainScope()

    private lateinit var seekBar: SeekBar //Selecting the sleep amount
    private lateinit var rangeBar: RangeBar //Selecting the wake up range https://github.com/Fedorkz/material-range-bar
    private lateinit var tViewSleepAmount: TextView //Display the selected sleep amount
    private lateinit var tViewWakeupTime: TextView  //Display the selected wake up range
    private lateinit var tViewAlarmName : TextView //Topic of the alarm
    private lateinit var viewExtendedAlarmSettings : View //Display extended alarm settings
    private lateinit var btnSelectActiveWeekday : Button //Popup window for selecting the weekdays for alarm
    private lateinit var btnDeleteAlarmInstance: Button //Delete current alarm entity
    private lateinit var swAlarmActive : Switch //Select whether alarm is on or off
    private lateinit var alarmSettings : AlarmEntity
    private lateinit var usedIds : MutableSet<Int>
    private val alarmEntityLiveData by lazy { repository.alarmFlow.asLiveData()}

    private fun saveAlarmIsActive(isActive: Boolean) {
        scope.launch {
            repository.updateIsActive(isActive, alarmId) }
    }

    fun saveSleepAmount(time: LocalTime) {
        tViewSleepAmount.text = " " + time.toString() + " Stunden"
        scope.launch {
            repository.updateSleepDuration(time.toSecondOfDay(), alarmId) }
    }

    private fun saveWakeupRange(wakeupEarly: LocalTime, wakeupLate: LocalTime) {
        tViewWakeupTime.text = " " + wakeupEarly.toString() + " - " + wakeupLate.toString() + " Uhr"
        scope.launch {
            repository.updateWakeupEarly(wakeupEarly.toSecondOfDay(), alarmId)
            repository.updateWakeupLate(wakeupLate.toSecondOfDay(), alarmId) }
    }

    private fun saveAlarmDaysWeek(daysOfWeek: ArrayList<DayOfWeek>) {
        scope.launch {
            repository.updateActiveDayOfWeek(daysOfWeek, alarmId)
        }
    }

    private fun saveAlarmName(alarmName: String) {
        scope.launch {
            repository.updateAlarmName(alarmName, alarmId)
        }
    }

    private fun getActiveAlarmDays(): BooleanArray {
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

    private suspend fun setupAlarmSettings() {
        alarmSettings = repository.getAlarmById(alarmId).first()

        val wakeupTime = LocalTime.ofSecondOfDay(alarmSettings.sleepDuration.toLong())
        val wakeupEarly = LocalTime.ofSecondOfDay(alarmSettings.wakeupEarly.toLong())
        val wakeupLate = LocalTime.ofSecondOfDay(alarmSettings.wakeupLate.toLong())
        val alarmName = alarmSettings.alarmName
        val isActive = alarmSettings.isActive

        // Setup the sleepAmount bar
        if (wakeupTime.minute == 30) { seekBar.progress = (wakeupTime.hour - 5) * 2 + 1 }
        else { seekBar.progress = (wakeupTime.hour - 5) * 2 }

        //Setup the wakeupRange bar
        var rBarLeft = 0.0
        var rBarRight = 0.0

        if (wakeupEarly.minute == 30) { rBarLeft = wakeupEarly.hour.toDouble() + 0.5 }
        else { rBarLeft = wakeupEarly.hour.toDouble() }

        if (wakeupLate.minute == 30) { rBarRight = wakeupLate.hour.toDouble() + 0.5 }
        else { rBarRight = wakeupLate.hour.toDouble() }

        rangeBar.setRangePinsByValue(rBarLeft.toFloat(), rBarRight.toFloat())
        swAlarmActive.isChecked = isActive

        //UpdateViews
        tViewSleepAmount.text = " " + wakeupTime.toString() + " Stunden"
        tViewWakeupTime.text = " " + wakeupEarly.toString() + " - " + wakeupLate.toString() + " Uhr"
        tViewAlarmName.text = alarmName
    }

    private fun selectActiveDaysOfWeek() {
        val items = arrayOf("Monday", "Tuesday", "Wednesday", "Thursday", "Friday", "Saturday", "Sunday")
        val daysOfWeek = DayOfWeek.values()
        val selectedList = ArrayList<Int>()
        val builder = AlertDialog.Builder(this.context, AlertDialog.THEME_DEVICE_DEFAULT_DARK)
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
                        Toast.makeText(applicationContext, "Speichern erfolgreich", Toast.LENGTH_SHORT).show()
                    }
                    else {
                        Toast.makeText(applicationContext, "Speichern fehlgeschlagen\nMindestens einen Tag auswählen! ", Toast.LENGTH_SHORT).show()
                    }
                }
                .setNegativeButton("Cancel") {
                    _, _ ->
                    Toast.makeText(applicationContext, "Verworfen", Toast.LENGTH_SHORT).show()
                    }
                .show()
    }

    private fun getAlarmName(){
        val input = EditText(context)
        input.inputType = InputType.TYPE_CLASS_TEXT

        val builder = AlertDialog.Builder(context, AlertDialog.THEME_DEVICE_DEFAULT_DARK)
        builder.setTitle("Set alarmname")
            .setView(input)
            .setPositiveButton("Save") {
                    _, _ ->
                val alarmName = input.text.toString()
                if (alarmName.isEmpty()) {
                    Toast.makeText(applicationContext, "Speichern fehlgeschlagen\nMindestens einen Char eingeben! ", Toast.LENGTH_SHORT).show()
                }
                else {
                    tViewAlarmName.text = alarmName
                    saveAlarmName(alarmName)
                }
            }
            .setNegativeButton("Cancel") {
                dialog, _ ->
                dialog.cancel()
            }
            .show()
    }

    private fun deleteAlarmEntity() {
        AlarmsFragment.getAlarmFragment().removeAlarmEntity(alarmId)
        scope.launch {
            repository.deleteAlarm(alarmSettings)
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.alarm_entity, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        seekBar = view.findViewById(R.id.sBar_sleepAmount)
        rangeBar = view.findViewById(R.id.rBar_wakeupRange)
        tViewSleepAmount = view.findViewById(R.id.tV_sleepAmountSelection)
        tViewWakeupTime = view.findViewById(R.id.tV_wakeupRangeSelection)
        tViewAlarmName = view.findViewById(R.id.tV_alarmName)
        viewExtendedAlarmSettings = view.findViewById(R.id.cL_extendedAlarmEntity)
        btnSelectActiveWeekday = view.findViewById(R.id.btn_selectActiveWeekday)
        swAlarmActive = view.findViewById(R.id.sw_alarmIsActive)
        btnDeleteAlarmInstance = view.findViewById(R.id.btn_deleteAlarm)
        usedIds = mutableSetOf()

        tViewAlarmName.setOnClickListener {
            viewExtendedAlarmSettings.isVisible = !viewExtendedAlarmSettings.isVisible
        }

        tViewAlarmName.setOnLongClickListener {
            getAlarmName()
            true
        }

        alarmEntityLiveData.observe(viewLifecycleOwner) {
            alarmList ->
            alarmSettings = alarmList.first { x -> x.id == alarmId }
        }

        swAlarmActive.setOnClickListener {
            saveAlarmIsActive(swAlarmActive.isChecked)
        }

        btnSelectActiveWeekday.setOnClickListener {
            selectActiveDaysOfWeek()
        }

        btnDeleteAlarmInstance.setOnClickListener {
            deleteAlarmEntity()
        }

        scope.launch {
            setupAlarmSettings()
        }

        seekBar.setOnSeekBarChangeListener(object: SeekBar.OnSeekBarChangeListener {
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

        rangeBar.setOnRangeBarChangeListener { _, _, _, _, _ ->
            var minutesLeft = 0
            var minutesRight = 0
            if (rangeBar.leftPinValue.contains(".5")) {
                minutesLeft = 30
            }
            if (rangeBar.rightPinValue.contains(".5")) {
                minutesRight = 30
            }

            saveWakeupRange(
                LocalTime.of(rangeBar.leftPinValue.toFloat().toInt(), minutesLeft),
                LocalTime.of(rangeBar.rightPinValue.toFloat().toInt(), minutesRight)
            )
        }
    }
}