package com.sleepestapp.sleepest.background

import android.content.Context
import com.sleepestapp.sleepest.MainApplication
import com.sleepestapp.sleepest.model.data.AlarmCycleStates
import com.sleepestapp.sleepest.model.data.Constants
import com.sleepestapp.sleepest.storage.DataStoreRepository
import com.sleepestapp.sleepest.storage.DataStoreRepository.Companion.getRepo
import com.sleepestapp.sleepest.storage.DatabaseRepository
import kotlinx.coroutines.runBlocking
import java.time.LocalTime

/**
 * This class detects the state of the alarm cycle. The different cycle states are declared in enum AlarmCycleStates.
 */

class AlarmCycleState(context: Context) {
    private val dataStoreRepository : DataStoreRepository =
        getRepo(context.applicationContext) //Instance of DataStoreRepo
    private val databaseRepository : DatabaseRepository =
        (context.applicationContext as MainApplication).dataBaseRepository //Instance of DatabaseRepo


    /**
     * Returns the actual state.
     */
    fun getState() : AlarmCycleStates = runBlocking {
        return@runBlocking chooseState()
    }

    /**
     * Returns the state depending on the actual time
     */
    suspend private fun chooseState() : AlarmCycleStates {

        if (databaseRepository.getNextActiveAlarm(dataStoreRepository) != null) {

            if (isBetweenTwoTimes(dataStoreRepository.getSleepTimeBegin(), databaseRepository.getNextActiveAlarm(dataStoreRepository)!!.wakeupEarly - Constants.CALCULATION_START_DIFFERENCE,
                checkDayChange(dataStoreRepository.getSleepTimeBegin(), databaseRepository.getNextActiveAlarm(dataStoreRepository)!!.wakeupEarly - Constants.CALCULATION_START_DIFFERENCE))) {

                    return AlarmCycleStates.BETWEEN_SLEEPTIME_START_AND_CALCULATION

            } else if (isBetweenTwoTimes(databaseRepository.getNextActiveAlarm(dataStoreRepository)!!.wakeupEarly - Constants.CALCULATION_START_DIFFERENCE, databaseRepository.getNextActiveAlarm(dataStoreRepository)!!.wakeupEarly,
                    checkDayChange(databaseRepository.getNextActiveAlarm(dataStoreRepository)!!.wakeupEarly - Constants.CALCULATION_START_DIFFERENCE, databaseRepository.getNextActiveAlarm(dataStoreRepository)!!.wakeupEarly))) {

                    return AlarmCycleStates.BETWEEN_CALCULATION_AND_FIRST_WAKEUP

            } else if (isBetweenTwoTimes(databaseRepository.getNextActiveAlarm(dataStoreRepository)!!.wakeupEarly, databaseRepository.getNextActiveAlarm(dataStoreRepository)!!.wakeupLate,
                    checkDayChange(databaseRepository.getNextActiveAlarm(dataStoreRepository)!!.wakeupEarly, databaseRepository.getNextActiveAlarm(dataStoreRepository)!!.wakeupLate))) {

                    return AlarmCycleStates.BETWEEN_FIRST_AND_LAST_WAKEUP

            }  else if (isBetweenTwoTimes(databaseRepository.getNextActiveAlarm(dataStoreRepository)!!.wakeupLate, dataStoreRepository.getSleepTimeEnd(),
                    checkDayChange(databaseRepository.getNextActiveAlarm(dataStoreRepository)!!.wakeupLate, dataStoreRepository.getSleepTimeEnd()))) {

                    return AlarmCycleStates.BETWEEN_LAST_WAKEUP_AND_SLEEPTIME_END

                }
                isBetweenTwoTimes(dataStoreRepository.getSleepTimeEnd(), dataStoreRepository.getSleepTimeBegin(),
                    checkDayChange(dataStoreRepository.getSleepTimeEnd(), dataStoreRepository.getSleepTimeBegin())) -> {

                    return AlarmCycleStates.BETWEEN_SLEEPTIME_END_AND_SLEEPTIME_START

                }
            }
        }

        return AlarmCycleStates.NO_STATE_DETECTED
    }

    /**
     * Checks if midnight is between two times
     * @param firstTime first time
     * @param secondTime second time
     * @return Midnight between = true
     */
    private fun checkDayChange(firstTime : Int, secondTime : Int) : Boolean {
        return (firstTime > secondTime)
    }

    /**
     * @param firstTime The first time for the comparison
     * @param secondTime The second time for the comparison
     * @param withDayChange Is midnight between these two times
     * @return Is actual time between these two times or not: true = between.
     */
    private fun isBetweenTwoTimes(firstTime : Int, secondTime : Int, withDayChange : Boolean) : Boolean {
        return if (withDayChange) {
            ((LocalTime.now().toSecondOfDay() <= secondTime) || (LocalTime.now().toSecondOfDay() >= firstTime))
        } else {
            LocalTime.now().toSecondOfDay() in firstTime..secondTime
        }
    }
}