package com.doitstudio.sleepest_master.util

import android.content.Context
import android.widget.Toast
import androidx.lifecycle.asLiveData
import com.doitstudio.sleepest_master.MainApplication
import com.doitstudio.sleepest_master.model.data.AlarmSleepChangeFrom
import com.doitstudio.sleepest_master.model.data.SleepSleepChangeFrom
import com.doitstudio.sleepest_master.storage.DataStoreRepository
import com.doitstudio.sleepest_master.storage.DatabaseRepository
import kotlinx.coroutines.flow.first
import java.lang.Math.abs
import java.time.LocalTime

object SleepTimeValidationUtil {

    /**
     * Returns the seconds between two seconds of day
     */
    private fun getTimeBetweenSecondsOfDay(endTime: Int, startTime:Int) : Int{

        val secondsOfDay = 60 * 60 * 24

        var timeOnDay = 0
        var timeNextDay = 0

        if(startTime > endTime)
        {
            timeOnDay = secondsOfDay - startTime
            timeNextDay = endTime
        }
        else {
            timeOnDay = endTime - startTime
        }

        return timeOnDay + timeNextDay
    }

    /**
     * This is used to check if the alarm settings that are made are in relation to the sleep settings.
     * If we can find any problems we return the new values if necessary that will notify the user
     */
    suspend fun checkAlarmActionIsAllowedAndDoAction(alarmId:Int,dataBaseRepository:DatabaseRepository, dataStoreRepository: DataStoreRepository, context: Context, wakeUpEarly: Int, wakeUpLate: Int, sleepDuration: Int, changeFrom: AlarmSleepChangeFrom) {


        val minTimeBuffer = 7200 // 2 hours

        var newWakeUpEarly = wakeUpEarly
        var newWakeUpLate = wakeUpLate
        var newSleepDuration = sleepDuration

        // Check if Wakeup Early and Wakeup Late matches, else change one of it
        if(wakeUpEarly > wakeUpLate) {
            newWakeUpLate = when (changeFrom) {
                AlarmSleepChangeFrom.WAKEUPEARLYLY -> wakeUpEarly
                else -> wakeUpLate
            }
            newWakeUpEarly = when (changeFrom) {
                AlarmSleepChangeFrom.WAKEUPLATE -> wakeUpLate
                else -> wakeUpEarly
            }
        }

        // now we need to get the sleep data
        val sleepSettings = dataStoreRepository.sleepParameterFlow.first()

        // Check if Wakeup Early is in sleep time
        if(wakeUpEarly > sleepSettings.sleepTimeEnd) {
            // differ between auto sleep time and user defined sleep time
            if(sleepSettings.autoSleepTime){
                val restTime = kotlin.math.abs(wakeUpEarly -  sleepSettings.sleepTimeEnd) + (minTimeBuffer / 2)

                // Adjust the sleep time automatically
                val newSleepTimeEnd = sleepSettings.sleepTimeEnd + restTime

                dataStoreRepository.updateSleepTimeEnd(newSleepTimeEnd)
            }
            else{
                newWakeUpEarly = sleepSettings.sleepTimeEnd
                Toast.makeText(context, "Out of sleep time! Change the sleep time end", Toast.LENGTH_SHORT)
                    .show()
            }
        }

        // Check if Wakeup Late is in sleep time
        if(wakeUpLate > sleepSettings.sleepTimeEnd) {
            // differ between auto sleep time and user defined sleep time
            if(sleepSettings.autoSleepTime){
                val restTime = kotlin.math.abs(wakeUpLate -  sleepSettings.sleepTimeEnd) + (minTimeBuffer / 2)

                // Adjust the sleep time automatically
                val newSleepTimeEnd = sleepSettings.sleepTimeEnd + restTime

                dataStoreRepository.updateSleepTimeEnd(newSleepTimeEnd)
            }
            else{
                newWakeUpLate = sleepSettings.sleepTimeEnd
                Toast.makeText(context, "Out of sleep time! Change the sleep time end", Toast.LENGTH_SHORT)
                    .show()
            }
        }

        //check if the possible sleep time is big enough for the sleep time
        val possibleSleepTime =
            getTimeBetweenSecondsOfDay(newWakeUpLate, sleepSettings.sleepTimeStart)
        if(possibleSleepTime < (sleepDuration + minTimeBuffer)){
            // differ between auto sleep time and user defined sleep time
            if(sleepSettings.autoSleepTime){
                val restTime =
                    kotlin.math.abs(possibleSleepTime - sleepDuration) + minTimeBuffer

                // Adjust the sleep time automatically
                val newSleepTimeStart = sleepSettings.sleepTimeStart - restTime

                dataStoreRepository.updateSleepTimeStart(newSleepTimeStart)
            }
            else{
                Toast.makeText(context,
                    "Not possible! Change the sleep time window or the latest wakeup point",
                    Toast.LENGTH_SHORT)
                    .show()

                if(changeFrom == AlarmSleepChangeFrom.DURATION){
                    newSleepDuration = (possibleSleepTime - minTimeBuffer)
                }
                else if(changeFrom == AlarmSleepChangeFrom.WAKEUPLATE){
                    val result =
                        kotlin.math.abs(possibleSleepTime - (sleepDuration + minTimeBuffer))
                    newWakeUpLate += result
                }
            }
        }

        if(wakeUpEarly != newWakeUpEarly || changeFrom == AlarmSleepChangeFrom.WAKEUPEARLYLY )
            dataBaseRepository.updateWakeupEarly(newWakeUpEarly, alarmId)
        if(wakeUpLate != newWakeUpLate || changeFrom == AlarmSleepChangeFrom.WAKEUPLATE)
            dataBaseRepository.updateWakeupLate(newWakeUpLate, alarmId)
        if(sleepDuration != newSleepDuration || changeFrom == AlarmSleepChangeFrom.DURATION)
            dataBaseRepository.updateSleepDuration(newSleepDuration, alarmId)
    }

    /**
     * This is used to check if the sleep settings that are made are in relation to the alarms.
     * If we can find any problems we set the values in the database (they will be the setup by the observer) and make a toast if necessary that will notify the user
     */
    suspend fun checkSleepActionIsAllowedAndDoAction(dataStoreRepository: DataStoreRepository, dataBaseRepository:DatabaseRepository, context: Context, sleepTimeStart : Int, sleepTimeEnd : Int, sleepDuration : Int, autoSleepTime : Boolean, changeFrom: SleepSleepChangeFrom){

        val minTimeBuffer = 7200 // 2 hours

        var newSleepTimeStart = sleepTimeStart
        var newSleepTimeEnd = sleepTimeEnd
        var newSleepDuration = sleepDuration

        //check if the possible sleep time is big enough for the sleep time
        val possibleSleepTime =
            getTimeBetweenSecondsOfDay(newSleepTimeEnd, newSleepTimeStart)

        // Check sleep params itself
        if(possibleSleepTime < (newSleepDuration + minTimeBuffer)){
            val timeDiff = kotlin.math.abs(possibleSleepTime - (newSleepDuration + minTimeBuffer))

            if(changeFrom == SleepSleepChangeFrom.DURATION){
                if(autoSleepTime){
                    newSleepTimeStart = (newSleepTimeStart - timeDiff/2)
                    newSleepTimeEnd = (newSleepTimeEnd + timeDiff/2)
                }
                else{
                    newSleepDuration = possibleSleepTime - minTimeBuffer
                }
            }
            else{

                newSleepTimeStart = when (changeFrom) {
                    SleepSleepChangeFrom.SLEEPTIMESTART -> {
                        Toast.makeText(context, "Conflicts with set sleep duration. Latest sleep time start is set", Toast.LENGTH_SHORT)
                            .show()
                        (newSleepTimeStart - timeDiff)
                        }
                    else -> newSleepTimeStart
                }
                newSleepTimeEnd = when (changeFrom) {
                    SleepSleepChangeFrom.SLEEPTIMEEND -> {
                        Toast.makeText(context, "Conflicts with set sleep duration. Earliest sleep time end is set", Toast.LENGTH_SHORT)
                            .show()
                        (newSleepTimeEnd + timeDiff)
                    }
                    else -> newSleepTimeEnd
                }
            }
        }

        // Check params for every alarm entity...
        val allAlarms = dataBaseRepository.alarmFlow.first()

        allAlarms.forEach{ alarm ->
            if(alarm.wakeupLate > (newSleepTimeEnd - minTimeBuffer/2)){
                newSleepTimeEnd = alarm.wakeupLate + minTimeBuffer/2
                Toast.makeText(context, "Conflicts with an alarm! Latest possible sleep end time is set", Toast.LENGTH_SHORT)
                    .show()
            }


            //check if the possible sleep time is big enough for the sleep time
            val possibleSleepTime =
                getTimeBetweenSecondsOfDay(alarm.wakeupLate, newSleepTimeStart)

            val timeDiff = kotlin.math.abs(possibleSleepTime - (alarm.sleepDuration + minTimeBuffer))

            if(possibleSleepTime < (alarm.sleepDuration + minTimeBuffer))
            {
                newSleepTimeStart = when (changeFrom) {
                    SleepSleepChangeFrom.SLEEPTIMESTART -> {
                        Toast.makeText(context, "Conflicts with an alarm! Earliest possible sleep start time is set", Toast.LENGTH_SHORT)
                            .show()

                        newSleepTimeStart - timeDiff
                    }

                    else -> sleepTimeStart
                }
            }
        }

        if(sleepTimeStart != newSleepTimeStart || changeFrom == SleepSleepChangeFrom.SLEEPTIMESTART)
            dataStoreRepository.updateSleepTimeStart(newSleepTimeStart)
        if(sleepTimeEnd != newSleepTimeEnd || changeFrom == SleepSleepChangeFrom.SLEEPTIMEEND)
            dataStoreRepository.updateSleepTimeEnd(newSleepTimeEnd)
        if(sleepDuration != newSleepDuration || changeFrom == SleepSleepChangeFrom.DURATION)
            dataStoreRepository.updateUserWantedSleepTime(newSleepDuration)
            dataStoreRepository.triggerObserver()

    }

    fun createMinutePickerHelper() : Array<String>{
        return arrayOf("0","15","30","45")
    }
}