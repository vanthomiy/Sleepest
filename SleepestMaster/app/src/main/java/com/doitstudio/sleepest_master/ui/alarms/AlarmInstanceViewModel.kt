package com.doitstudio.sleepest_master.ui.alarms

import android.app.AlertDialog
import android.app.Application
import android.app.TimePickerDialog
import android.text.InputType
import android.transition.TransitionManager
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.EditText
import android.widget.SeekBar
import android.widget.Toast
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.view.isVisible
import androidx.databinding.ObservableArrayList
import androidx.databinding.ObservableArrayMap
import androidx.databinding.ObservableField
import androidx.databinding.ObservableMap
import androidx.lifecycle.AndroidViewModel
import com.doitstudio.sleepest_master.MainApplication
import com.doitstudio.sleepest_master.R
import com.doitstudio.sleepest_master.storage.DataStoreRepository
import com.doitstudio.sleepest_master.storage.DatabaseRepository
import com.doitstudio.sleepest_master.util.SleepTimeValidationUtil
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.serialization.PrimitiveKind
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

    val extendedAlarmEntity = ObservableField(true)
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
            .setPositiveButton("Save") {
                    _, _ ->
                val alarmName = input.text.toString()
                if (alarmName.isEmpty()) {
                    Toast.makeText(context, "Speichern fehlgeschlagen\nMindestens einen Char eingeben! ", Toast.LENGTH_SHORT).show()
                }
                else {
                    scope.launch {
                        dataBaseRepository.updateAlarmName(alarmName, alarmId)
                    }                }
            }
            .setNegativeButton("Cancel") {
                    dialog, _ ->
                dialog.cancel()
            }
            .show()
    }

    var enoughTimeToSleep = true

    val sleepStartValue = ObservableField("07:30")
    val sleepEndValue = ObservableField("07:30")
    var sleepStartTime = LocalTime.now()
    var sleepEndTime = LocalTime.now()

    fun onAlarmStartClicked(view: View){

        val hour = (sleepStartTime.hour)
        val minute = (sleepStartTime.minute)

        val tpd = TimePickerDialog(
            view.context,
            { view, h, m ->

                sleepStartTime = LocalTime.of(h, m)


                scope.launch {

                    var times = SleepTimeValidationUtil.checkIfWakeUpTimeIsInSleepTime(dataStoreRepository, view.context, sleepEndTime.toSecondOfDay(), sleepStartTime.toSecondOfDay())
                    sleepStartTime = LocalTime.ofSecondOfDay(times.first.toLong())

                    sleepStartValue.set((if (sleepStartTime.hour < 10) "0" else "") + sleepStartTime.hour.toString() + ":" + (if (sleepStartTime.minute < 10) "0" else "") + sleepStartTime.minute.toString())

                    dataBaseRepository.updateWakeupEarly(times.first, alarmId)

                    if(times.second < times.first){
                        sleepEndValue.set(sleepStartValue.get())
                        sleepEndTime = sleepStartTime
                        dataBaseRepository.updateWakeupLate(times.first, alarmId)
                    }
                }
            },
            hour,
            minute,
            false
        )

        tpd.show()
    }

    fun onAlarmEndClicked(view: View){
        val hour = (sleepEndTime.hour)
        val minute = (sleepEndTime.minute)

        val tpd = TimePickerDialog(
            view.context,
            TimePickerDialog.OnTimeSetListener(function = { view, h, m ->

                sleepEndTime = LocalTime.of(h, m)

                scope.launch {
                    var times = SleepTimeValidationUtil.checkIfWakeUpTimeIsInSleepTime(dataStoreRepository, view.context, sleepEndTime.toSecondOfDay(), sleepStartTime.toSecondOfDay())
                    sleepEndTime = LocalTime.ofSecondOfDay(times.second.toLong())

                    sleepEndValue.set((if (sleepEndTime.hour < 10) "0" else "") + sleepEndTime.hour.toString() + ":" + (if (sleepEndTime.minute < 10) "0" else "") + sleepEndTime.minute.toString())

                    dataBaseRepository.updateWakeupLate(times.second, alarmId)

                    if(times.first > times.second){
                        sleepStartValue.set(sleepEndValue.get())
                        sleepStartTime = sleepEndTime
                        dataBaseRepository.updateWakeupEarly(times.second, alarmId)
                    }

                    SleepTimeValidationUtil.checkIfSleepTimeCanBeReached(dataStoreRepository, context, sleepEndTime.toSecondOfDay(), sleepDurationValue.get()!!)
                }
            }),
            hour,
            minute,
            false
        )

        tpd.show()
    }

    val sleepDurationString = ObservableField("7h")
    val sleepDurationValue = ObservableField(0)

    fun onProgressChanged(sBar: SeekBar?, progress: Int, fromUser: Boolean) {
        val progressTemp = progress*0.5 + 5
        var minutes = 0
        if (progressTemp % 1 == 0.5) { minutes = 30 }

        val time = (LocalTime.of(progressTemp.toInt(), minutes))
        sleepDurationString.set(time.hour.toString() + "h " + time.minute.toString() + "m")
        sleepDurationValue.set(time.toSecondOfDay())

        scope.launch {
            dataBaseRepository.updateSleepDuration(time.toSecondOfDay(), alarmId)

            val sleepSettings = dataStoreRepository.sleepParameterFlow.first()

            if(!sleepSettings.autoSleepTime){
                enoughTimeToSleep = SleepTimeValidationUtil.checkIfSleepTimeMatchesSleepDuration(context, time.toSecondOfDay(), sleepSettings.sleepTimeEnd, sleepSettings.sleepTimeStart, enoughTimeToSleep)
            }
            else{
                SleepTimeValidationUtil.checkIfSleepTimeMatchesSleepDurationAuto(dataStoreRepository, time.toSecondOfDay(), sleepSettings.sleepTimeEnd, sleepSettings.sleepTimeStart, enoughTimeToSleep)

            }

            SleepTimeValidationUtil.checkIfSleepTimeCanBeReached(dataStoreRepository, context, sleepEndTime.toSecondOfDay(), time.toSecondOfDay())
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

            // Setup the sleepAmount bar
            if (sleepDuration.minute == 30) { sleepDurationValue.set((sleepDuration.hour - 5) * 2 + 1) }
            else { sleepDurationValue.set((sleepDuration.hour - 5) * 2) }

            sleepDurationString.set(sleepDuration.hour.toString() + "h " + sleepDuration.minute.toString() + "m")

            sleepStartTime = wakeupEarly
            sleepEndTime = wakeupLate
            sleepStartValue.set((if (sleepStartTime.hour < 10) "0" else "") + sleepStartTime.hour.toString() + ":" + (if (sleepStartTime.minute < 10) "0" else "") + (sleepStartTime.minute.toString()))
            sleepEndValue.set((if (sleepEndTime.hour < 10) "0" else "") + sleepEndTime.hour.toString() + ":" + (if (sleepEndTime.minute < 10) "0" else "") + (sleepEndTime.minute.toString()))

        }
    }

    //region animation

    lateinit var transitionsContainer : ViewGroup


    //endregion
}


