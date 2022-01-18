package com.sleepestapp.sleepest.alarmclock

import android.content.Context
import com.sleepestapp.sleepest.sleepcalculation.SleepCalculationHandler
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.launch
import java.time.LocalDateTime

class AlarmClockSleepCalculationHandling(val context: Context) {
    private val sleepCalculationHandler : SleepCalculationHandler by lazy {
        SleepCalculationHandler(context)
    }
    private val scope: CoroutineScope = MainScope()

    /**
     * Defines a new wakeup
     * @param localTime Localtime, null is actual time
     * @param setAlarm in sleep time
     */
    fun defineNewUserWakeup(localTime: LocalDateTime?, setAlarm:Boolean) {
        scope.launch {
            sleepCalculationHandler.defineUserWakeup(localTime, setAlarm)
        }
    }
}