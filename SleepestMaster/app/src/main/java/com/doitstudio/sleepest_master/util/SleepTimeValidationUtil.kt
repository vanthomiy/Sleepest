package com.doitstudio.sleepest_master.util

import android.content.Context
import android.widget.Toast
import com.doitstudio.sleepest_master.model.data.AlarmSleepChangeFrom
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
     * Checks if in general the sleep time is long enough to reach the users sleep goal
     */
    fun checkIfSleepTimeMatchesSleepDuration(context:Context, sleepTime:Int, endTime: Int, startTime:Int, enoughTimeToSleep:Boolean) : Boolean {

        val availableTime = getTimeBetweenSecondsOfDay(endTime, startTime)

        return if (availableTime > sleepTime) {
            if (!enoughTimeToSleep)
                Toast.makeText(context, "You can now reach your sleep time", Toast.LENGTH_SHORT)
                    .show()
            true
        } else {
            if (enoughTimeToSleep)
                Toast.makeText(
                    context,
                    "You cant reach your desired sleep duration",
                    Toast.LENGTH_SHORT
                ).show()
            false
        }

    }

    /**
     * Checks if in general the sleep time is long enough to reach the users sleep goal and auto change it
     */
    suspend fun checkIfSleepTimeMatchesSleepDurationAuto(dataStoreRepository: DataStoreRepository, sleepTime:Int, endTime: Int, startTime:Int, enoughTimeToSleep:Boolean) : Pair<Int, Int> {
        val availableTime = getTimeBetweenSecondsOfDay(endTime, startTime)
        val day = 24*60*60

        val restTime = (availableTime - (sleepTime + day/12))

        var newEndTime = endTime - restTime/2
        var newStartTime = startTime + restTime/2

        if(newEndTime > day)
            newEndTime -= day

        if(newStartTime < 0)
            newStartTime += day

        if(newStartTime > day)
            newStartTime -= day

        if(newEndTime < 0)
            newEndTime += day

        dataStoreRepository.updateSleepTimeEnd(newEndTime)
        dataStoreRepository.updateSleepTimeStart(newStartTime)

        return Pair(newEndTime, newStartTime)
    }

    /**
     * This is used to check if the alarm settings that are made are in relation to the sleep settings.
     * If we can find any problems we return the new values if necessary that will notify the user
     */
    suspend fun checkAlarmActionIsAllowedAndDoAction(alarmId:Int,dataBaseRepository:DatabaseRepository, dataStoreRepository: DataStoreRepository, context: Context, wakeUpEarly: Int, wakeUpLate: Int, sleepDuration: Int, changeFrom: AlarmSleepChangeFrom) : Int {


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
                    return (possibleSleepTime - minTimeBuffer)
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

        return 0
    }

    /**
     * This is used to check if the sleep settings that are made are in relation to the alarms.
     * If we can find any problems we set the values in the database (they will be the setup by the observer) and make a toast if necessary that will notify the user
     */
    suspend fun checkSleepActionIsAllowedAndDoAction(databaseRepository: DatabaseRepository){

    }


}