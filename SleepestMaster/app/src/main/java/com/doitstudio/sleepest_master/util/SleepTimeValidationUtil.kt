package com.doitstudio.sleepest_master.util

import android.content.Context
import android.widget.Toast
import com.doitstudio.sleepest_master.storage.DataStoreRepository
import com.doitstudio.sleepest_master.storage.DatabaseRepository
import kotlinx.coroutines.flow.first

object SleepTimeValidationUtil {

    /**
     * Returns the seconds between two seconds of day
     */
    fun getTimeBetweenSecondsOfDay(endTime: Int, startTime:Int) : Int{

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
     * Checks if the wakeup time of an alarm is in the sleep time
     */
    suspend fun checkIfWakeUpTimeIsInSleepTime(dataStoreRepository: DataStoreRepository, context: Context, endTime: Int, startTime:Int) : Pair<Int, Int> {

        var endTimeNew =endTime
        var startTimeNew =startTime

        if(dataStoreRepository.getSleepTimeEnd() < endTime){
            Toast.makeText(context, "Sleep end time is not in Sleep Time", Toast.LENGTH_SHORT)
                .show()

            endTimeNew = dataStoreRepository.getSleepTimeEnd()
        }

        if(dataStoreRepository.getSleepTimeEnd() < startTime){
            Toast.makeText(context, "Sleep start time is not in Sleep Time", Toast.LENGTH_SHORT)
                .show()
            startTimeNew = dataStoreRepository.getSleepTimeEnd()
        }

        return Pair(startTimeNew, endTimeNew)
    }

    /**
     * Checks if sleep time can be reached
     * We need last alarm wakeup point and sleep time
     * Sleep time start
     */
    suspend fun checkIfSleepTimeCanBeReached(dataStoreRepository: DataStoreRepository, context: Context, alarmEnd:Int, requiredSleepTime:Int) {

        val sleep = dataStoreRepository.sleepParameterFlow.first()

        var sleepTime = getTimeBetweenSecondsOfDay(alarmEnd, sleep.sleepTimeStart)

        if(requiredSleepTime > sleepTime){
            if(sleep.autoSleepTime){

                val day = 24*60*60

                val restTime = (sleepTime - (requiredSleepTime + day/12))

                var newStartTime = sleep.sleepTimeStart + restTime

                if(newStartTime < 0)
                    newStartTime += day

                if(newStartTime > day)
                    newStartTime -= day

                dataStoreRepository.updateSleepTimeStart(newStartTime)

            }
            else{
                Toast.makeText(context, "You cant reach your required sleep time", Toast.LENGTH_SHORT)
                    .show()
            }
        }



    }

    /**
     * This is used to check if the alarm settings that are made are in relation to the sleep settings.
     * If we can find any problems we return the new values if necessary that will notify the user
     */
    suspend fun checkAlarmActionIsAllowedAndDoAction(dataStoreRepository: DataStoreRepository) {

    }

    /**
     * This is used to check if the sleep settings that are made are in relation to the alarms.
     * If we can find any problems we set the values in the database (they will be the setup by the observer) and make a toast if necessary that will notify the user
     */
    suspend fun checkSleepActionIsAllowedAndDoAction(databaseRepository: DatabaseRepository){

    }


}