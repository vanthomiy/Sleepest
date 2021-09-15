package com.sleepestapp.sleepest.ui.alarms

import android.app.Application
import android.app.TimePickerDialog
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.asLiveData
import androidx.lifecycle.viewModelScope
import com.sleepestapp.sleepest.MainApplication
import com.sleepestapp.sleepest.R
import com.sleepestapp.sleepest.model.data.AlarmSleepChangeFrom
import com.sleepestapp.sleepest.storage.DataStoreRepository
import com.sleepestapp.sleepest.storage.DatabaseRepository
import com.sleepestapp.sleepest.util.SleepTimeValidationUtil
import com.sleepestapp.sleepest.util.WeekDaysUtil.getWeekDayByNumber
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import java.time.DayOfWeek
import java.time.LocalTime


class AlarmInstanceViewModel(
    val dataStoreRepository: DataStoreRepository,
    val dataBaseRepository: DatabaseRepository,
    val alarmId : Int
) : ViewModel() {

    //region Alarm Instance

    // The actual id of the alarm instance

    val actualAlarmLiveData by lazy{
        dataBaseRepository.getAlarmById(alarmId).asLiveData()
    }

    val isAlarmActive = MutableLiveData(false)
    val alarmName = MutableLiveData("")
    var is24HourFormat = false
    /**
     * Alarm active/disabled is toggled
     */
    fun onAlarmActiveToggled(view: View) {
        viewModelScope.launch {
            isAlarmActive.value?.let {
                dataBaseRepository.updateIsActive(it, alarmId)
                /*if (dataBaseRepository.getAlarmById(alarmId).first().isActive && dataBaseRepository.getAlarmById(alarmId).first().tempDisabled) {
                    Toast.makeText(context, context.getString(R.string.alarms_information_temporary_disabled), Toast.LENGTH_LONG).show()
                }*/
            }
        }

    }

    val extendedAlarmEntity = MutableLiveData(false)
    val visibleState = MutableLiveData(View.VISIBLE)
    val goneState = MutableLiveData(View.GONE)

    /**
     * Alarm name can be changed here
     * TODO(Not implemented yet)
     */
    fun onAlarmNameClick(view: View) {
        extendedAlarmEntity.value = (extendedAlarmEntity.value == false)
    }

    val wakeUpEarlyValue = MutableLiveData("07:30")
    val wakeUpLateValue = MutableLiveData("07:30")
    var wakeUpEarly: LocalTime = LocalTime.now()
    var wakeUpLate: LocalTime = LocalTime.now()

    /**
     * Wake Up early clicked
     */
    fun onWakeUpEarlyClicked(view: View){

        val hour = (wakeUpEarly.hour)
        val minute = (wakeUpEarly.minute)

        val tpd = TimePickerDialog(
            view.context,
            R.style.TimePickerTheme,
            { pickerView, h, m ->

                val tempWakeup = LocalTime.of(h, m)

                viewModelScope.launch {

                    SleepTimeValidationUtil.checkAlarmActionIsAllowedAndDoAction(
                        alarmId,
                        dataBaseRepository,
                        dataStoreRepository,
                        tempWakeup.toSecondOfDay(),
                        wakeUpLate.toSecondOfDay(),
                        sleepDuration,
                        AlarmSleepChangeFrom.WAKEUPEARLYLY
                    )
                }
            },
            hour,
            minute,
            is24HourFormat
        )
        tpd.show()
    }

    /**
     * Wake Up late clicked
     */
    fun onWakeUpLateClicked(view: View){
        val hour = (wakeUpLate.hour)
        val minute = (wakeUpLate.minute)

        val tpd = TimePickerDialog(
            view.context,
            R.style.TimePickerTheme,
            { view, h, m ->
                val tempWakeup = LocalTime.of(h, m)

                viewModelScope.launch {

                    SleepTimeValidationUtil.checkAlarmActionIsAllowedAndDoAction(
                        alarmId,
                        dataBaseRepository,
                        dataStoreRepository,
                        wakeUpEarly.toSecondOfDay(),
                        tempWakeup.toSecondOfDay(),
                        sleepDuration,
                        AlarmSleepChangeFrom.WAKEUPLATE
                    )
                }
            },
            hour,
            minute,
            is24HourFormat
        )

        tpd.show()
    }

    var sleepDuration : Int = 0
    val sleepDurationString = MutableLiveData("07:00")

    /**
     * Sleep duration changed by user
     */
    fun onDurationChange(hour: Int, minute: Int) {

        var hourSetter = hour
        if(hour >= 24)
            hourSetter = 23

        val time = LocalTime.of(hourSetter, (minute-1) * 15)

        viewModelScope.launch {
            SleepTimeValidationUtil.checkAlarmActionIsAllowedAndDoAction(
                alarmId,
                dataBaseRepository,
                dataStoreRepository,
                wakeUpEarly.toSecondOfDay(),
                wakeUpLate.toSecondOfDay(),
                time.toSecondOfDay(),
                AlarmSleepChangeFrom.DURATION
            )
        }
    }

    val selectedDays = MutableLiveData<MutableList<Int>>()
    val selectedDaysInfo = MutableLiveData("")

    /**
     * Day selection changed
     */
    fun onDayChanged(view: View){

        val day = view.tag.toString().toInt()
        if(selectedDays.value?.contains(day) == true){
            val list = selectedDays.value?.toMutableList()
            list?.remove(day)
            selectedDays.value = list?: mutableListOf()
        }
        else{
            val list = selectedDays.value?.toMutableList()
            list?.add(day)
            selectedDays.value  = list?: mutableListOf()
        }

        val dayOfWeekValues = DayOfWeek.values()
        val daysOfWeek = ArrayList<DayOfWeek>()
        selectedDays.value?.forEach{
            daysOfWeek.add(dayOfWeekValues[it])
        }

        viewModelScope.launch {
            dataBaseRepository.updateActiveDayOfWeek(daysOfWeek, alarmId)
        }
    }

    //endregion

    init {

        /**
         * Loads all the init values from the datastore and passes the values to the bindings for the alarm by id
         */
        viewModelScope.launch {

            var alarmSettings = dataBaseRepository.getAlarmById(alarmId).first()

            alarmSettings?.let{
                val wakeupEarly = LocalTime.ofSecondOfDay(alarmSettings.wakeupEarly.toLong())
                val wakeupLate = LocalTime.ofSecondOfDay(alarmSettings.wakeupLate.toLong())
                isAlarmActive.value = (alarmSettings.isActive)
                alarmName.value = (alarmSettings.alarmName)

                wakeUpEarly = wakeupEarly
                wakeUpLate = wakeupLate
                wakeUpEarlyValue.value = ((if (wakeUpEarly.hour < 10) "0" else "") + wakeUpEarly.hour.toString() + ":" + (if (wakeUpEarly.minute < 10) "0" else "") + (wakeUpEarly.minute.toString()))
                wakeUpLateValue.value = ((if (wakeUpLate.hour < 10) "0" else "") + wakeUpLate.hour.toString() + ":" + (if (wakeUpLate.minute < 10) "0" else "") + (wakeUpLate.minute.toString()))


                val dayOfWeek = mutableListOf<Int>()

                alarmSettings.activeDayOfWeek.forEach{
                    dayOfWeek.add(it.ordinal)
                }

                selectedDays.value = dayOfWeek

            }
        }
    }

    //region animation

    lateinit var transitionsContainer : ViewGroup


    //endregion
}


