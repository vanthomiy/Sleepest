package com.doitstudio.sleepest_master.util

import android.content.Context
import android.widget.Toast
import com.doitstudio.sleepest_master.MainApplication
import com.doitstudio.sleepest_master.storage.DataStoreRepository
import kotlin.math.abs

object SleepTimeValidationUtil {



    /**
     * Checks if in general the sleep time is long enough to reach the users sleep goal
     */
    suspend fun checkIfSleepTimeMatchesSleepDuration(sleepTime:Int, dataStoreRepository: DataStoreRepository) : Boolean{

        val availableTime = getTimeBetweenSecondsOfDay(dataStoreRepository.getSleepTimeEnd(), dataStoreRepository.getSleepTimeBegin())

        return availableTime > sleepTime
    }

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
}