package com.doitstudio.sleepest_master.ui.alarm

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
import com.doitstudio.sleepest_master.MainApplication
import com.doitstudio.sleepest_master.R
import com.doitstudio.sleepest_master.storage.db.AlarmEntity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import java.time.DayOfWeek
import java.time.LocalTime

/**
 * Class which represents one alarm entity.
 * Contains all settings for each individual alarm.
 */
class AlarmInstance(val applicationContext: Context, private var alarmId: Int) : Fragment() {

    // region variable declarations
    /** TODO Description */
    private val databaseRepository by lazy { (applicationContext as MainApplication).dataBaseRepository }

    /** TODO Description */
    private val scope: CoroutineScope = MainScope()

    /** Selecting the sleep amount */
    private lateinit var seekBar : SeekBar

    /** Selecting the wake up range. Source: https://github.com/Fedorkz/material-range-bar */
    private lateinit var rangeBar : RangeBar

    /** Display the selected sleep amount */
    private lateinit var tViewSleepAmount: TextView

    /** Display the selected wake up range */
    private lateinit var tViewWakeupTime: TextView

    /** Topic of the alarm */
    private lateinit var tViewAlarmName : TextView

    /** Shows the active weekdays */
    private lateinit var tViewActiveWeekdays: TextView

    /** Shows the selected sleep amount as hint */
    private lateinit var tViewSleepAmountHint: TextView

    /** Shows the selected wake up range as hint */
    private lateinit var tViewWakeupTimeHint: TextView

    /** Display extended alarm settings */
    private lateinit var viewExtendedAlarmSettings : View

    /** Popup window for selecting the weekdays for alarm */
    private lateinit var btnSelectActiveWeekday : Button

    /** Delete current alarm entity */
    private lateinit var btnDeleteAlarmInstance: Button

    /** Select whether alarm is on or off */
    private lateinit var swAlarmActive : Switch

    /** Represents the current alarm entity */
    private lateinit var alarmEntity : AlarmEntity

    /** TODO Description */
    private lateinit var usedIds : MutableSet<Int>

    /** TODO Description */
    private val alarmEntityLiveData by lazy { databaseRepository.alarmFlow.asLiveData()}
    //endregion

    /**
     * Save whether alarm is active or not into the DatabaseRepository.
     */
    private fun saveAlarmIsActive(isActive: Boolean) {
        scope.launch {
            databaseRepository.updateIsActive(isActive, alarmId) }
    }

    /**
     * Save the selected sleep amount into the DatabaseRepository.
     * Change the displayed alarm information based on the sleep amount.
     */
    fun saveSleepAmount(time: LocalTime) {
        tViewSleepAmount.text = " " + time.toString() + " Stunden"
        tViewSleepAmountHint.text = time.toString() + " h"
        scope.launch {
            databaseRepository.updateSleepDuration(time.toSecondOfDay(), alarmId) }
    }

    /**
     * Save the selected wake up range into the DatabaseRepository.
     * Change the displayed alarm information based on the wake up range.
     */
    private fun saveWakeupRange(wakeupEarly: LocalTime, wakeupLate: LocalTime) {
        tViewWakeupTime.text = " " + wakeupEarly.toString() + " - " + wakeupLate.toString() + " Uhr"
        tViewWakeupTimeHint.text = wakeupEarly.toString() + " - " + wakeupLate.toString()
        scope.launch {
            databaseRepository.updateWakeupEarly(wakeupEarly.toSecondOfDay(), alarmId)
            databaseRepository.updateWakeupLate(wakeupLate.toSecondOfDay(), alarmId) }
    }

    /**
     * Save the selected active weekdays on which the alarm should be active into the DatabaseRepository.
     */
    private fun saveAlarmDaysWeek(daysOfWeek: ArrayList<DayOfWeek>) {
        scope.launch {
            databaseRepository.updateActiveDayOfWeek(daysOfWeek, alarmId)
        }
    }

    /**
     * Save the name of the alarm entity into the DatabaseRepository.
     */
    private fun saveAlarmName(alarmName: String) {
        scope.launch {
            databaseRepository.updateAlarmName(alarmName, alarmId)
        }
    }

    /**
     * Checks which days of the week are checked as active for the current alarm entity.
     */
    private fun getActiveAlarmDays(): BooleanArray {
        val activeDays = mutableListOf<Boolean>()
        for (i in 0..6) {
            if (alarmEntity.activeDayOfWeek.contains(DayOfWeek.values()[i])) {
                activeDays.add(true)
            } else {
                activeDays.add(false)
            }
        }
        return activeDays.toBooleanArray()
    }

    /**
     * Converts the currently active alarm days of the week into a string, which will be displayed as a hint.
     */
    private fun convertActiveAlarmDays(selectedDays: ArrayList<DayOfWeek>): String {
        var daysString = ""
        if (selectedDays.contains(DayOfWeek.MONDAY)) { daysString += "Mo " }
        if (selectedDays.contains(DayOfWeek.TUESDAY)) { daysString += "Tu " }
        if (selectedDays.contains(DayOfWeek.WEDNESDAY)) { daysString += "We " }
        if (selectedDays.contains(DayOfWeek.THURSDAY)) { daysString += "Th " }
        if (selectedDays.contains(DayOfWeek.FRIDAY)) { daysString += "Fr " }
        if (selectedDays.contains(DayOfWeek.SATURDAY)) { daysString += "Sa " }
        if (selectedDays.contains(DayOfWeek.SUNDAY)) { daysString += "Su " }
        return daysString
    }

    /**
     * TODO Description
     */
    private suspend fun setupAlarmSettings() {
        alarmEntity = databaseRepository.getAlarmById(alarmId).first()

        val wakeupTime = LocalTime.ofSecondOfDay(alarmEntity.sleepDuration.toLong())
        val wakeupEarly = LocalTime.ofSecondOfDay(alarmEntity.wakeupEarly.toLong())
        val wakeupLate = LocalTime.ofSecondOfDay(alarmEntity.wakeupLate.toLong())
        val alarmName = alarmEntity.alarmName
        val isActive = alarmEntity.isActive

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
        tViewActiveWeekdays.text = convertActiveAlarmDays(alarmEntity.activeDayOfWeek)
        tViewSleepAmountHint.text = wakeupTime.toString() + " h"
        tViewWakeupTimeHint.text = wakeupEarly.toString() + " - " + wakeupLate.toString()
    }

    /**
     * Opens a dialogue which lets the user decide, on which weekdays the alarm should be active.
     */
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
                        tViewActiveWeekdays.text = convertActiveAlarmDays(selectedDays)
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

    /**
     * Opens a dialogue, which lets the user alter the name of the current alarm entity.
     */
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

    /**
     * Deletes the current alarm entity from the DatabaseRepository.
     */
    private fun deleteAlarmEntity() {
        AlarmsFragment.getAlarmFragment().removeAlarmEntity(alarmId)
        scope.launch {
            databaseRepository.deleteAlarm(alarmEntity)
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.alarm_entity, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // region variable initializations
        seekBar = view.findViewById(R.id.sBar_sleepAmount)
        rangeBar = view.findViewById(R.id.rBar_wakeupRange)
        tViewSleepAmount = view.findViewById(R.id.tV_sleepAmountSelection)
        tViewWakeupTime = view.findViewById(R.id.tV_wakeupRangeSelection)
        tViewAlarmName = view.findViewById(R.id.tV_alarmName)
        tViewActiveWeekdays = view.findViewById(R.id.tV_activeWeekdays)
        tViewSleepAmountHint = view.findViewById(R.id.tV_wakeupRangeHint)
        tViewWakeupTimeHint = view.findViewById(R.id.tV_sleepAmountHint)
        viewExtendedAlarmSettings = view.findViewById(R.id.cL_extendedAlarmEntity)
        btnSelectActiveWeekday = view.findViewById(R.id.btn_selectActiveWeekday)
        swAlarmActive = view.findViewById(R.id.sw_alarmIsActive)
        btnDeleteAlarmInstance = view.findViewById(R.id.btn_deleteAlarm)
        usedIds = mutableSetOf()
        //endregion

        tViewAlarmName.setOnClickListener {
            viewExtendedAlarmSettings.isVisible = !viewExtendedAlarmSettings.isVisible
            tViewActiveWeekdays.isVisible = !tViewActiveWeekdays.isVisible
            tViewSleepAmountHint.isVisible = !tViewSleepAmountHint.isVisible
            tViewWakeupTimeHint.isVisible = !tViewWakeupTimeHint.isVisible
        }

        tViewAlarmName.setOnLongClickListener {
            getAlarmName()
            true
        }

        alarmEntityLiveData.observe(viewLifecycleOwner) {
            alarmList ->
            alarmEntity = alarmList.first { x -> x.id == alarmId }
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