package com.sleepestapp.sleepest.util

import android.content.Context
import android.text.format.DateFormat
import com.sleepestapp.sleepest.model.data.AlarmSleepChangeFrom
import com.sleepestapp.sleepest.model.data.AlarmTimeData
import com.sleepestapp.sleepest.model.data.SleepSleepChangeFrom
import com.sleepestapp.sleepest.storage.DataStoreRepository
import com.sleepestapp.sleepest.storage.DatabaseRepository
import com.sleepestapp.sleepest.storage.db.AlarmEntity
import kotlinx.coroutines.flow.first
import java.time.LocalDate
import java.time.LocalTime

object SleepTimeValidationUtil {

    /**
     * Returns the seconds between two seconds of day
     */
    fun getTimeBetweenSecondsOfDay(endTime: Int, startTime:Int) : Int{

        val secondsOfDay = 60 * 60 * 24

        val timeOnDay: Int
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
     * Subtracts minutes from seconds of day. Day aware
     */
    fun subtractMinutesFromSecondsOfDay(secondsOfDay: Int, subtractMinutes:Int) : Int{

        val maxSecondsOfDay = 60 * 60 * 24

        val subtractedTime = secondsOfDay - (subtractMinutes * 60)

        return if (subtractedTime < 0)
            maxSecondsOfDay + subtractedTime
        else
            subtractedTime
    }

    /**
     * This is used to check if the alarm settings that are made are in relation to the sleep settings.
     * If we can find any problems we return the new values if necessary that will notify the user
     */
    suspend fun checkAlarmActionIsAllowedAndDoAction(alarmId:Int,dataBaseRepository:DatabaseRepository, dataStoreRepository: DataStoreRepository, wakeUpEarly: Int, wakeUpLate: Int, sleepDuration: Int, changeFrom: AlarmSleepChangeFrom) {


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
                /*Toast.makeText(context, "Out of sleep time! Change the sleep time end", Toast.LENGTH_SHORT)
                    .show()*/
            }
        }

        // Check if Wakeup Late is in sleep time
        if(wakeUpLate >= sleepSettings.sleepTimeEnd) {
            // differ between auto sleep time and user defined sleep time
            if(sleepSettings.autoSleepTime){
                //val restTime = kotlin.math.abs(wakeUpLate -  sleepSettings.sleepTimeEnd) + (minTimeBuffer / 2)

                // Adjust the sleep time automatically
                val newSleepTimeEnd = sleepSettings.sleepTimeEnd + 900//+ restTime

                dataStoreRepository.updateSleepTimeEnd(newSleepTimeEnd)
            }
            else{
                newWakeUpLate = sleepSettings.sleepTimeEnd - 900
                /*Toast.makeText(context, "Out of sleep time! Change the sleep time end", Toast.LENGTH_SHORT)
                    .show()*/
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
                /*Toast.makeText(context,
                    "Not possible! Change the sleep time window or the latest wakeup point",
                    Toast.LENGTH_SHORT)
                    .show()*/

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

        if(wakeUpEarly != newWakeUpEarly || changeFrom == AlarmSleepChangeFrom.WAKEUPEARLYLY ) {
            dataBaseRepository.updateWakeupEarly(newWakeUpEarly, alarmId)

            if (LocalTime.now().toSecondOfDay() !in (newWakeUpEarly + 1) until newWakeUpLate) {
                dataBaseRepository.updateWakeupTime(newWakeUpEarly, alarmId)
            }
        }

        if(wakeUpLate != newWakeUpLate || changeFrom == AlarmSleepChangeFrom.WAKEUPLATE){
            dataBaseRepository.updateWakeupLate(newWakeUpLate, alarmId)
            dataStoreRepository.triggerAlarmObserver()
        }

        if(sleepDuration != newSleepDuration || changeFrom == AlarmSleepChangeFrom.DURATION){
            dataBaseRepository.updateSleepDuration(newSleepDuration, alarmId)
            dataStoreRepository.triggerAlarmObserver()
        }
    }

    /**
     * This is used to check if the sleep settings that are made are in relation to the alarms.
     * If we can find any problems we set the values in the database (they will be the setup by the observer) and make a toast if necessary that will notify the user
     */
    suspend fun checkSleepActionIsAllowedAndDoAction(dataStoreRepository: DataStoreRepository, dataBaseRepository:DatabaseRepository, sleepTimeStart : Int, sleepTimeEnd : Int, sleepDuration : Int, autoSleepTime : Boolean, changeFrom: SleepSleepChangeFrom){

        val minTimeBuffer = 7200 // 2 hours

        var newSleepTimeStart = sleepTimeStart
        var newSleepTimeEnd = sleepTimeEnd
        var newSleepDuration = sleepDuration

        //check if the possible sleep time is big enough for the sleep time
        var possibleSleepTime =
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
                        /*Toast.makeText(context, "Conflicts with set sleep duration. Latest sleep time start is set", Toast.LENGTH_SHORT)
                            .show()*/
                        (newSleepTimeStart - timeDiff)
                        }
                    else -> newSleepTimeStart
                }
                newSleepTimeEnd = when (changeFrom) {
                    SleepSleepChangeFrom.SLEEPTIMEEND -> {
                        /*Toast.makeText(context., "Conflicts with set sleep duration. Earliest sleep time end is set", Toast.LENGTH_SHORT)
                            .show()*/
                        (newSleepTimeEnd + timeDiff)
                    }
                    else -> newSleepTimeEnd
                }
            }
        }

        // Check params for every alarm entity...
        val allAlarms = dataBaseRepository.alarmFlow.first()

        allAlarms.forEach{ alarm ->
            if(alarm.wakeupLate >= (newSleepTimeEnd /*- minTimeBuffer/2*/)){
                newSleepTimeEnd = alarm.wakeupLate + 900//+ minTimeBuffer/2
                /*Toast.makeText(this, "Conflicts with an alarm! Latest possible sleep end time is set", Toast.LENGTH_SHORT)
                    .show()*/
            }


            //check if the possible sleep time is big enough for the sleep time
            possibleSleepTime =
                getTimeBetweenSecondsOfDay(alarm.wakeupLate, newSleepTimeStart)

            val timeDiff = kotlin.math.abs(possibleSleepTime - (alarm.sleepDuration + minTimeBuffer))

            if(possibleSleepTime < (alarm.sleepDuration + minTimeBuffer))
            {
                newSleepTimeStart = when (changeFrom) {
                    SleepSleepChangeFrom.SLEEPTIMESTART -> {
                        /*Toast.makeText(context, "Conflicts with an alarm! Earliest possible sleep start time is set", Toast.LENGTH_SHORT)
                            .show()*/

                        newSleepTimeStart - timeDiff
                    }

                    else -> sleepTimeStart
                }
            }
        }

        if(sleepTimeStart != newSleepTimeStart || changeFrom == SleepSleepChangeFrom.SLEEPTIMESTART){
            dataStoreRepository.updateSleepTimeStart(newSleepTimeStart)
            dataStoreRepository.triggerSleepObserver()
        }
        if(sleepTimeEnd != newSleepTimeEnd || changeFrom == SleepSleepChangeFrom.SLEEPTIMEEND){
            dataStoreRepository.updateSleepTimeEnd(newSleepTimeEnd)
            dataStoreRepository.triggerSleepObserver()
        }
        if(sleepDuration != newSleepDuration || changeFrom == SleepSleepChangeFrom.DURATION){
            dataStoreRepository.updateUserWantedSleepTime(newSleepDuration)
            dataStoreRepository.triggerSleepObserver()
        }
    }

    fun createMinutePickerHelper() : Array<String>{
        return arrayOf("0","15","30","45")
    }

    /**
     * Returns true if the date format is 24h format else false
     */
    fun is24HourFormat(context:Context) : Boolean{
        return DateFormat.is24HourFormat(context)
    }

    /**
     * Gets seconds of day with local time
     */
    fun getSecondsOfDay() : Int{

        return LocalTime.now().toSecondOfDay()

    }

    suspend fun getActiveAlarms(allAlarms : List<AlarmEntity>, dataStoreRepository: DataStoreRepository) : List<AlarmEntity>
    {
        val activeAlarms = mutableListOf<AlarmEntity>()

        allAlarms.forEach {
            alarm ->

            val alarmTimeData = getActualAlarmTimeData(dataStoreRepository)

            val dateTime = LocalDate.now()

            val date = if(alarmTimeData.alarmIsOnSameDay)
                dateTime
            else
                dateTime.plusDays(1)

            if(alarm.activeDayOfWeek.contains(date.dayOfWeek) && alarm.isActive)
                activeAlarms.add(alarm)
        }

        return activeAlarms
    }

    suspend fun getActualAlarmTimeData(dataStoreRepository: DataStoreRepository, time:LocalTime = LocalTime.now()): AlarmTimeData {
        // get sleep times
        val times = dataStoreRepository.sleepParameterFlow.first()

        // get the seconds of the actual day
        val seconds = time.toSecondOfDay()

        // check if sleep time is over two days
        val sleepTimeOverTwoDays = times.sleepTimeStart > times.sleepTimeEnd

        // check if time is before sleep time on day
        val isBeforeSleepTime = times.sleepTimeStart > seconds

        // check if time is in sleep time
        val isInSleepTime = if(sleepTimeOverTwoDays){
            (times.sleepTimeStart < seconds || times.sleepTimeEnd > seconds)
        }
        else {
            (times.sleepTimeStart < seconds && times.sleepTimeEnd > seconds)
        }

        val alarmIsOnSameDay = if(sleepTimeOverTwoDays) {
            (isInSleepTime && seconds < times.sleepTimeEnd)
        }
        else{
            (isInSleepTime || isBeforeSleepTime)
        }


        return AlarmTimeData(
            isBeforeSleepTime,
            isInSleepTime,
            sleepTimeOverTwoDays,
            alarmIsOnSameDay)
    }



}