package com.doitstudio.sleepest_master.ui.alarms

import android.app.AlertDialog
import android.content.Context
import android.os.Bundle
import android.text.InputType
import android.transition.TransitionManager
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.asLiveData
import com.appyvet.rangebar.RangeBar
import com.doitstudio.sleepest_master.MainApplication
import com.doitstudio.sleepest_master.R
import com.doitstudio.sleepest_master.databinding.AlarmEntityBinding
import com.doitstudio.sleepest_master.databinding.FragmentAlarmsBinding
import com.doitstudio.sleepest_master.storage.db.AlarmEntity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import java.time.DayOfWeek
import java.time.LocalTime

class AlarmInstanceFragment(val applicationContext: Context, private var alarmId: Int) : Fragment() {

    private val repository by lazy { (applicationContext as MainApplication).dataBaseRepository }
    private val scope: CoroutineScope = MainScope()

    private lateinit var binding: AlarmEntityBinding
    private val viewModel by lazy { ViewModelProvider(this).get(AlarmInstanceViewModel::class.java) }
    private val basicViewModel by lazy { ViewModelProvider(requireActivity()).get(AlarmsViewModel::class.java) }


    private lateinit var seekBar : SeekBar //Selecting the sleep amount
    private lateinit var tViewSleepAmount: TextView //Display the selected sleep amount
    private lateinit var tViewActiveWeekdays: TextView //Shows the active weekdays
    private lateinit var tViewSleepAmountHint: TextView //Shows the selected sleep amount as hint
    private lateinit var viewExtendedAlarmSettings : View //Display extended alarm settings
    private lateinit var cLAlarmEntityInnerLayer : ViewGroup //Display extended alarm settings
    private lateinit var btnSelectActiveWeekday : Button //Popup window for selecting the weekdays for alarm
    private lateinit var btnDeleteAlarmInstance: Button //Delete current alarm entity
    private lateinit var alarmSettings : AlarmEntity
    private lateinit var usedIds : MutableSet<Int>
    private val alarmEntityLiveData by lazy { repository.alarmFlow.asLiveData()}

    private fun saveAlarmDaysWeek(daysOfWeek: ArrayList<DayOfWeek>) {
        scope.launch {
            repository.updateActiveDayOfWeek(daysOfWeek, alarmId)
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

    private suspend fun setupAlarmSettings() {
        alarmSettings = repository.getAlarmById(alarmId).first()

        val wakeupTime = LocalTime.ofSecondOfDay(alarmSettings.sleepDuration.toLong())
        val wakeupEarly = LocalTime.ofSecondOfDay(alarmSettings.wakeupEarly.toLong())
        val wakeupLate = LocalTime.ofSecondOfDay(alarmSettings.wakeupLate.toLong())

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

        //UpdateViews
        tViewSleepAmount.text = " " + wakeupTime.toString() + " Stunden"
        tViewActiveWeekdays.text = convertActiveAlarmDays(alarmSettings.activeDayOfWeek)
        tViewSleepAmountHint.text = wakeupTime.toString() + " h"
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


    private fun deleteAlarmEntity() {
        TransitionManager.beginDelayedTransition(cLAlarmEntityInnerLayer);
        AlarmsFragment.getAlarmFragment().removeAlarmEntity(alarmId)

        scope.launch {
            repository.deleteAlarm(alarmSettings)
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {

        binding = AlarmEntityBinding.inflate(inflater, container, false)
        binding.alarmsViewModel = viewModel
        viewModel.alarmId =

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        seekBar = view.findViewById(R.id.sBar_sleepAmount)
        tViewSleepAmount = view.findViewById(R.id.tV_sleepAmountSelection)
        tViewActiveWeekdays = view.findViewById(R.id.tV_activeWeekdays)
        tViewSleepAmountHint = view.findViewById(R.id.tV_wakeupRangeHint)
        viewExtendedAlarmSettings = view.findViewById(R.id.cL_extendedAlarmEntity)
        btnSelectActiveWeekday = view.findViewById(R.id.btn_selectActiveWeekday)
        cLAlarmEntityInnerLayer = view.findViewById(R.id.cL_alarmEntityInnerLayer)
        btnDeleteAlarmInstance = view.findViewById(R.id.btn_deleteAlarm)
        usedIds = mutableSetOf()



        alarmEntityLiveData.observe(viewLifecycleOwner) { alarmList ->
            alarmSettings = alarmList.first { x -> x.id == alarmId }
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

    }
}