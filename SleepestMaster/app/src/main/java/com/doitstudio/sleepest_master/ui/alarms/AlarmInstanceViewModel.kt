package com.doitstudio.sleepest_master.ui.alarms

import android.app.AlertDialog
import android.app.Application
import android.app.TimePickerDialog
import android.text.InputType
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.SeekBar
import android.widget.Toast
import androidx.databinding.Observable
import androidx.databinding.Observable.OnPropertyChangedCallback
import androidx.databinding.ObservableArrayList
import androidx.databinding.ObservableField
import androidx.databinding.ObservableInt
import androidx.lifecycle.AndroidViewModel
import com.doitstudio.sleepest_master.MainApplication
import com.doitstudio.sleepest_master.model.data.AlarmSleepChangeFrom
import com.doitstudio.sleepest_master.storage.DataStoreRepository
import com.doitstudio.sleepest_master.storage.DatabaseRepository
import com.doitstudio.sleepest_master.util.SleepTimeValidationUtil
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import java.time.DayOfWeek
import java.time.LocalTime


class AlarmInstanceViewModel(application: Application) : AndroidViewModel(application) {

    private val scope: CoroutineScope = MainScope()
    private val context by lazy{ getApplication<Application>().applicationContext }

    private val dataBaseRepository: DatabaseRepository by lazy {
        (context as MainApplication).dataBaseRepository
    }
    private val dataStoreRepository: DataStoreRepository by lazy {
        (context as MainApplication).dataStoreRepository
    }

    var alarmId = 1

    //region Alarm Instance

    val isAlarmActive = ObservableField(false)
    val alarmName = ObservableField("Alarm")
    fun onAlarmActiveToggled(view: View) {
        scope.launch {
            isAlarmActive.get()?.let {
                dataBaseRepository.updateIsActive(it, alarmId)
            }
        }

    }

    private val extendedAlarmEntity = ObservableField(true)
    val visibleState = ObservableField(View.VISIBLE)
    val goneState = ObservableField(View.GONE)

    fun onAlarmNameClick(view: View) {
        extendedAlarmEntity.set(extendedAlarmEntity.get() == false)
    }

    fun onLongAlarmNameClick(view: View) {
        val input = EditText(context)
        input.inputType = InputType.TYPE_CLASS_TEXT

        val builder = AlertDialog.Builder(context, AlertDialog.THEME_DEVICE_DEFAULT_DARK)
        builder.setTitle("Set alarmname")
            .setView(input)
            .setPositiveButton("Save") { _, _ ->
                val alarmName = input.text.toString()
                if (alarmName.isEmpty()) {
                    Toast.makeText(
                        context,
                        "Speichern fehlgeschlagen\nMindestens einen Char eingeben! ",
                        Toast.LENGTH_SHORT
                    ).show()
                }
                else {
                    scope.launch {
                        dataBaseRepository.updateAlarmName(alarmName, alarmId)
                    }                }
            }
            .setNegativeButton("Cancel") { dialog, _ ->
                dialog.cancel()
            }
            .show()
    }

    val wakeUpEarlyValue = ObservableField("07:30")
    val wakeUpLateValue = ObservableField("07:30")
    var wakeUpEarly: LocalTime = LocalTime.now()
    var wakeUpLate: LocalTime = LocalTime.now()

    fun onWakeUpEarlyClicked(view: View){

        val hour = (wakeUpEarly.hour)
        val minute = (wakeUpEarly.minute)

        val tpd = TimePickerDialog(
            view.context,
            { view, h, m ->

                val tempWakeup = LocalTime.of(h, m)

                scope.launch {

                    SleepTimeValidationUtil.checkAlarmActionIsAllowedAndDoAction(
                        alarmId,
                        dataBaseRepository,
                        dataStoreRepository,
                        view.context,
                        tempWakeup.toSecondOfDay(),
                        wakeUpLate.toSecondOfDay(),
                        sleepDuration,
                        AlarmSleepChangeFrom.WAKEUPEARLYLY
                    )
                }
            },
            hour,
            minute,
            false
        )
        tpd.show()
    }

    fun onWakeUpLateClicked(view: View){
        val hour = (wakeUpLate.hour)
        val minute = (wakeUpLate.minute)

        val tpd = TimePickerDialog(
            view.context,
            TimePickerDialog.OnTimeSetListener(function = { view, h, m ->

                val tempWakeup = LocalTime.of(h, m)

                scope.launch {

                    SleepTimeValidationUtil.checkAlarmActionIsAllowedAndDoAction(
                        alarmId,
                        dataBaseRepository,
                        dataStoreRepository,
                        view.context,
                        wakeUpEarly.toSecondOfDay(),
                        tempWakeup.toSecondOfDay(),
                        sleepDuration,
                        AlarmSleepChangeFrom.WAKEUPLATE
                    )
                }
            }),
            hour,
            minute,
            false
        )

        tpd.show()
    }

    var sleepDuration : Int = 0

    fun onDurationChange(hour: Int, minute: Int) {

        val time = LocalTime.of(hour, minute)

        scope.launch {
            SleepTimeValidationUtil.checkAlarmActionIsAllowedAndDoAction(
                alarmId,
                dataBaseRepository,
                dataStoreRepository,
                context,
                wakeUpEarly.toSecondOfDay(),
                wakeUpLate.toSecondOfDay(),
                time.toSecondOfDay(),
                AlarmSleepChangeFrom.DURATION
            )
        }
    }


    val selectedDays = ObservableArrayList<Int>()

    fun onDayChanged(view: View){

        val day = view.tag.toString().toInt()
        if(selectedDays.contains(day)){
            selectedDays.remove(day)
        }
        else{
            selectedDays.add(day)
        }

        val dayOfWeekValues = DayOfWeek.values()
        val daysOfWeek = ArrayList<DayOfWeek>()
        selectedDays.forEach{
            daysOfWeek.add(dayOfWeekValues[it])
        }

        scope.launch {
            dataBaseRepository.updateActiveDayOfWeek(daysOfWeek, alarmId)
        }
    }


    //endregion

    init {
        scope.launch {
            var alarmSettings = dataBaseRepository.getAlarmById(alarmId).first()

            val sleepDuration = LocalTime.ofSecondOfDay(alarmSettings.sleepDuration.toLong())
            val wakeupEarly = LocalTime.ofSecondOfDay(alarmSettings.wakeupEarly.toLong())
            val wakeupLate = LocalTime.ofSecondOfDay(alarmSettings.wakeupLate.toLong())
            isAlarmActive.set(alarmSettings.isActive)
            alarmName.set(alarmSettings.alarmName)

            wakeUpEarly = wakeupEarly
            wakeUpLate = wakeupLate
            wakeUpEarlyValue.set((if (wakeUpEarly.hour < 10) "0" else "") + wakeUpEarly.hour.toString() + ":" + (if (wakeUpEarly.minute < 10) "0" else "") + (wakeUpEarly.minute.toString()))
            wakeUpLateValue.set((if (wakeUpLate.hour < 10) "0" else "") + wakeUpLate.hour.toString() + ":" + (if (wakeUpLate.minute < 10) "0" else "") + (wakeUpLate.minute.toString()))

            alarmSettings.activeDayOfWeek.forEach{
                selectedDays.add(it.ordinal)
            }

        }
    }


    //region animation

    lateinit var transitionsContainer : ViewGroup


    //endregion
}


