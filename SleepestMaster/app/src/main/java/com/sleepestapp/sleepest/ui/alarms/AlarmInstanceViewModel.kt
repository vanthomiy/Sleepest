package com.sleepestapp.sleepest.ui.alarms

import android.app.Application
import android.app.TimePickerDialog
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.databinding.ObservableArrayList
import androidx.databinding.ObservableField
import androidx.lifecycle.AndroidViewModel
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


class AlarmInstanceViewModel(application: Application) : AndroidViewModel(application) {

    // region init

    /**
     * Helper function to get the strings from ressources
     */
    private fun getStringXml(id:Int): String {
        return getApplication<Application>().resources.getString(id)
    }

    /**
     * Scope is used to call datastore async
     */
    private val scope: CoroutineScope = MainScope()

    /**
     * Get actual context
     */
    private val context by lazy{ getApplication<Application>().applicationContext }

    /**
     * The database Repository
     */
    val dataBaseRepository: DatabaseRepository by lazy {
        (context as MainApplication).dataBaseRepository
    }

    /**
     * The datastore Repository
     */
    private val dataStoreRepository: DataStoreRepository by lazy {
        (context as MainApplication).dataStoreRepository
    }



    // endregion

    //region Alarm Instance

    // The actual id of the alarm instance
    var alarmId = 1
    val isAlarmActive = ObservableField(false)
    val alarmName = ObservableField(getStringXml(R.string.alarm_instance_alarm))

    /**
     * Alarm active/disabled is toggled
     */
    fun onAlarmActiveToggled(view: View) {
        scope.launch {
            isAlarmActive.get()?.let {
                dataBaseRepository.updateIsActive(it, alarmId)
                if (dataBaseRepository.getAlarmById(alarmId).first().isActive && dataBaseRepository.getAlarmById(alarmId).first().tempDisabled) {
                    Toast.makeText(context, context.getString(R.string.alarms_information_temporary_disabled), Toast.LENGTH_LONG).show()
                }
            }
        }

    }

    val extendedAlarmEntity = ObservableField(false)
    val visibleState = ObservableField(View.VISIBLE)
    val goneState = ObservableField(View.GONE)

    /**
     * Alarm name can be changed here
     * TODO(Not implemented yet)
     */
    fun onAlarmNameClick(view: View) {
        extendedAlarmEntity.set(extendedAlarmEntity.get() == false)
    }

    val wakeUpEarlyValue = ObservableField("07:30")
    val wakeUpLateValue = ObservableField("07:30")
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

                scope.launch {

                    SleepTimeValidationUtil.checkAlarmActionIsAllowedAndDoAction(
                        alarmId,
                        dataBaseRepository,
                        dataStoreRepository,
                        pickerView.context,
                        tempWakeup.toSecondOfDay(),
                        wakeUpLate.toSecondOfDay(),
                        sleepDuration,
                        AlarmSleepChangeFrom.WAKEUPEARLYLY
                    )
                }
            },
            hour,
            minute,
            SleepTimeValidationUtil.Is24HourFormat(context)
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
            },
            hour,
            minute,
            SleepTimeValidationUtil.Is24HourFormat(context)
        )

        tpd.show()
    }

    var sleepDuration : Int = 0
    val sleepDurationString = ObservableField("07:00")

    /**
     * Sleep duration changed by user
     */
    fun onDurationChange(hour: Int, minute: Int) {

        var hourSetter = hour
        if(hour >= 24)
            hourSetter = 23

        val time = LocalTime.of(hourSetter, (minute-1) * 15)

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
    val selectedDaysInfo = ObservableField("")

    /**
     * Day selection changed
     */
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

        setDaysSelectedString()

        scope.launch {
            dataBaseRepository.updateActiveDayOfWeek(daysOfWeek, alarmId)
        }
    }

    /**
     * Update the selected days string
     */
    private fun setDaysSelectedString(){
        var info = ""

        if(selectedDays.isEmpty()){
            info = getStringXml(R.string.alarm_instance_no_day_choosen)
        }
        else if(selectedDays.count() >= 7)
        {
            info = getStringXml(R.string.alarm_instance_daily)
        }
        else if(selectedDays.count() == 2 && selectedDays.contains(5) && selectedDays.contains(6))
        {
            info = getStringXml(R.string.alarm_instance_weekend)
        }
        else if(selectedDays.count() == 5 && !selectedDays.contains(5) && !selectedDays.contains(6))
        {
            info = getStringXml(R.string.alarm_instance_working_day)
        }
        else{



            selectedDays.toList().sorted().forEach{
                if(info == ""){
                    info = getWeekDayByNumber(it)
                }
                else{
                    info +=  ", "+ getWeekDayByNumber(it)
                }
            }
        }

        selectedDaysInfo.set(info)

    }


    //endregion

    init {

        /**
         * Loads all the init values from the datastore and passes the values to the bindings for the alarm by id
         */
        scope.launch {

            var alarmSettings = dataBaseRepository.getAlarmById(alarmId).first()

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

            setDaysSelectedString()
        }
    }

    //region animation

    lateinit var transitionsContainer : ViewGroup


    //endregion
}


