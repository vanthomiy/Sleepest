package com.doitstudio.sleepest_master.background

import android.content.Context
import com.doitstudio.sleepest_master.MainApplication
import com.doitstudio.sleepest_master.model.data.AlarmCycleStates
import com.doitstudio.sleepest_master.model.data.Constants
import com.doitstudio.sleepest_master.storage.DataStoreRepository
import com.doitstudio.sleepest_master.storage.DataStoreRepository.Companion.getRepo
import com.doitstudio.sleepest_master.storage.DatabaseRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import java.time.LocalTime

class AlarmCycleState(private val context: Context) {
    private val dataStoreRepository : DataStoreRepository //Instance of DataStoreRepo
    private val databaseRepository : DatabaseRepository //Instance of DatabaseRepo

    init {
        databaseRepository = (context.applicationContext as MainApplication).dataBaseRepository
        dataStoreRepository = getRepo(context.applicationContext)
    }

    fun getState() : AlarmCycleStates = runBlocking {
        return@runBlocking chooseState()
    }

    suspend private fun chooseState() : AlarmCycleStates {
        if (databaseRepository.getNextActiveAlarm() != null) {

            if (isBetweenTwoTimes(dataStoreRepository.getSleepTimeBegin(), databaseRepository.getNextActiveAlarm()!!.wakeupEarly - Constants.CALCULATION_START_DIFFERENCE,
                checkDayChange(dataStoreRepository.getSleepTimeBegin(), databaseRepository.getNextActiveAlarm()!!.wakeupEarly - Constants.CALCULATION_START_DIFFERENCE))) {

                    return AlarmCycleStates.BETWEEN_SLEEPTIME_START_AND_CALCULATION

            } else if (isBetweenTwoTimes(databaseRepository.getNextActiveAlarm()!!.wakeupEarly - Constants.CALCULATION_START_DIFFERENCE, databaseRepository.getNextActiveAlarm()!!.wakeupEarly,
                    checkDayChange(databaseRepository.getNextActiveAlarm()!!.wakeupEarly - Constants.CALCULATION_START_DIFFERENCE, databaseRepository.getNextActiveAlarm()!!.wakeupEarly))) {

                        return AlarmCycleStates.BETWEEN_CALCULATION_AND_FIRST_WAKEUP

            } else if (isBetweenTwoTimes(databaseRepository.getNextActiveAlarm()!!.wakeupEarly, databaseRepository.getNextActiveAlarm()!!.wakeupLate,
                    checkDayChange(databaseRepository.getNextActiveAlarm()!!.wakeupEarly, databaseRepository.getNextActiveAlarm()!!.wakeupLate))) {

                        return AlarmCycleStates.BETWEEN_FIRST_AND_LAST_WAKEUP

            }  else if (isBetweenTwoTimes(databaseRepository.getNextActiveAlarm()!!.wakeupLate, dataStoreRepository.getSleepTimeEnd(),
                    checkDayChange(databaseRepository.getNextActiveAlarm()!!.wakeupLate, dataStoreRepository.getSleepTimeEnd()))) {

                        return AlarmCycleStates.BETWEEN_LAST_WAKEUP_AND_SLEEPTIME_END

            } else if (isBetweenTwoTimes(dataStoreRepository.getSleepTimeEnd(), dataStoreRepository.getSleepTimeBegin(),
                    checkDayChange(dataStoreRepository.getSleepTimeEnd(), dataStoreRepository.getSleepTimeBegin()))) {

                return AlarmCycleStates.BETWEEN_SLEEPTIME_END_AND_SLEEPTIME_START

            }
        }

        return AlarmCycleStates.NO_STATE_DETECTED
    }

    /**
     * @param time1 first time
     * @param time2 second time
     */
    private fun checkDayChange(firstTime : Int, secondTime : Int) : Boolean {
        return (firstTime > secondTime)
    }

    private fun isBetweenTwoTimes(firstTime : Int, secondTime : Int, withDayChange : Boolean) : Boolean {
        if (withDayChange) {
            val a = ((LocalTime.now().toSecondOfDay() <= secondTime) || (LocalTime.now().toSecondOfDay() >= firstTime))
           return a
        } else {
            val a = LocalTime.now().toSecondOfDay() in firstTime..secondTime
            return a
        }
    }
}