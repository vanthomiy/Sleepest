package com.doitstudio.sleepest_master.background

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.widget.Toast
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.PeriodicWorkRequest
import androidx.work.WorkManager
import com.doitstudio.sleepest_master.MainApplication
import com.doitstudio.sleepest_master.R
import com.doitstudio.sleepest_master.alarmclock.AlarmClockAudio
import com.doitstudio.sleepest_master.alarmclock.AlarmClockReceiver
import com.doitstudio.sleepest_master.googleapi.SleepHandler
import com.doitstudio.sleepest_master.model.data.Actions
import com.doitstudio.sleepest_master.model.data.AlarmClockReceiverUsage
import com.doitstudio.sleepest_master.model.data.AlarmReceiverUsage
import com.doitstudio.sleepest_master.model.data.Constants
import com.doitstudio.sleepest_master.sleepcalculation.SleepCalculationHandler
import com.doitstudio.sleepest_master.storage.DataStoreRepository
import com.doitstudio.sleepest_master.storage.DatabaseRepository
import com.doitstudio.sleepest_master.util.TimeConverterUtil
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import java.time.LocalDateTime
import java.time.LocalTime
import java.util.*
import java.util.concurrent.TimeUnit

class BackgroundAlarmTimeHandler(val context: Context) {

    private val dataBaseRepository: DatabaseRepository by lazy {
        (context.applicationContext as MainApplication).dataBaseRepository
    }
    private val dataStoreRepository: DataStoreRepository by lazy {
        (context.applicationContext as MainApplication).dataStoreRepository
    }
    private val sleepHandler : SleepHandler by lazy {
        SleepHandler.getHandler(context)
    }
    private val sleepCalculationHandler : SleepCalculationHandler by lazy {
        SleepCalculationHandler.getHandler(context)
    }
    private val scope: CoroutineScope = MainScope()

    private var sleepTimeBeginTemp = 0
    private var sleepTimeEndTemp = 0
    private var firstWakeupTemp = 0
    private var lastWakeupTemp = 0

    init {
        scope.launch {
            sleepTimeBeginTemp = getSleepTimeBeginValue()
            sleepTimeEndTemp = getSleepTimeEndValue()
            firstWakeupTemp = getFirstWakeup()
            lastWakeupTemp = getLastWakeup()
        }

    }

    fun changeSleepTime() {
        scope.launch {

            if (checkInSleepTime() && (getSleepTimeBeginValue() != sleepTimeBeginTemp)) {
                beginOfSleepTime(false)
                AlarmReceiver.cancelAlarm(context, AlarmReceiverUsage.START_FOREGROUND)
            } else {
                if ((sleepTimeEndTemp != getSleepTimeEndValue()) && !checkInSleepTime()) {
                    endOfSleepTime(false)
                } else if (checkInSleepTime() && (sleepTimeEndTemp != getSleepTimeEndValue())) {
                    val calendar = TimeConverterUtil.getAlarmDate(getSleepTimeEndValue())
                    AlarmReceiver.startAlarmManager(calendar.get(Calendar.DAY_OF_WEEK), calendar.get(Calendar.HOUR_OF_DAY), calendar.get(Calendar.MINUTE), context.applicationContext, AlarmReceiverUsage.STOP_WORKMANAGER);
                }

                if ((getSleepTimeBeginValue() != sleepTimeBeginTemp) && !checkInSleepTime()) {
                    val calendarAlarm = TimeConverterUtil.getAlarmDate(getSleepTimeBeginValue())
                    AlarmReceiver.startAlarmManager(
                        calendarAlarm[Calendar.DAY_OF_WEEK],
                        calendarAlarm[Calendar.HOUR_OF_DAY],
                        calendarAlarm[Calendar.MINUTE], context, AlarmReceiverUsage.START_FOREGROUND)

                    val pref = context.getSharedPreferences("AlarmReceiver1", 0)
                    val ed = pref.edit()
                    ed.putString("usage", "MainActivity")
                    ed.putInt("day", calendarAlarm[Calendar.DAY_OF_WEEK])
                    ed.putInt("hour", calendarAlarm[Calendar.HOUR_OF_DAY])
                    ed.putInt("minute", calendarAlarm[Calendar.MINUTE])
                    ed.apply()
                }
            }

            sleepTimeBeginTemp = getSleepTimeBeginValue()
            sleepTimeEndTemp = getSleepTimeEndValue()
        }
    }

    fun changeOfAlarmEntity(listEmpty : Boolean) {
        scope.launch {
            if (checkInSleepTime() && checkAlarmActive() && !checkForegroundStatus() && !checkAlarmFired() && !checkAlarmTempDisabled() && !listEmpty) {
                startForegroundService(false)
            } else if (listEmpty) {
                stopForegroundService(false)
            }

            if (checkInSleepTime() && checkAlarmActive() && checkForegroundStatus() && !checkAlarmFired() && !checkAlarmTempDisabled() && !listEmpty) {

                if (getFirstWakeup() != firstWakeupTemp) {
                    val calenderCalculation = TimeConverterUtil.getAlarmDate(getFirstWakeup() - 1800)
                    AlarmReceiver.startAlarmManager(
                        calenderCalculation[Calendar.DAY_OF_WEEK],
                        calenderCalculation[Calendar.HOUR_OF_DAY],
                        calenderCalculation[Calendar.MINUTE],
                        context,
                        AlarmReceiverUsage.START_WORKMANAGER_CALCULATION)
                }

                if (getLastWakeup() != lastWakeupTemp) {
                    val calendar = TimeConverterUtil.getAlarmDate(getLastWakeup())
                    AlarmClockReceiver.startAlarmManager(calendar.get(Calendar.DAY_OF_WEEK), calendar.get(Calendar.HOUR_OF_DAY), calendar.get(Calendar.MINUTE), context.applicationContext, AlarmClockReceiverUsage.LATEST_WAKEUP_ALARMCLOCK);
                }
            }

            lastWakeupTemp = getLastWakeup()
            firstWakeupTemp = getFirstWakeup()

        }
    }

    fun alarmClockRang(isScreenOn : Boolean) {

        scope.launch {
            if (isScreenOn) {
                AlarmClockAudio.getInstance().stopAlarm(false)

                //stopForegroundService(true)
                Toast.makeText(context, "Alarmclock stopped", Toast.LENGTH_LONG).show()
                val calendar = Calendar.getInstance()
                val pref: SharedPreferences = context.getSharedPreferences("AlarmClock", 0)
                var ed = pref.edit()
                ed.putInt("hour", calendar[Calendar.HOUR_OF_DAY])
                ed.putInt("minute", calendar[Calendar.MINUTE])
                ed.apply()
            } else {
                AlarmClockAudio.getInstance().stopAlarm(false)

                val calendarAlarm = TimeConverterUtil.getAlarmDate(getSleepTimeBeginValue())

                stopForegroundService(false)

                val calendar = Calendar.getInstance()
                var pref: SharedPreferences = context.getSharedPreferences("AlarmClock", 0)
                var ed = pref.edit()
                ed.putInt("hour", calendar[Calendar.HOUR_OF_DAY])
                ed.putInt("minute", calendar[Calendar.MINUTE])
                ed.apply()

                pref = context.getSharedPreferences("AlarmReceiver1", 0)
                ed = pref.edit()
                ed.putString("usage", "LockScreenAlarmActivity")
                ed.putInt("day", calendarAlarm[Calendar.DAY_OF_WEEK])
                ed.putInt("hour", calendarAlarm[Calendar.HOUR_OF_DAY])
                ed.putInt("minute", calendarAlarm[Calendar.MINUTE])
                ed.apply()
            }

            if (checkAlarmActive()) {
                dataBaseRepository.updateAlarmWasFired(true, dataBaseRepository.getNextActiveAlarm()!!.id)
            }


            /*if (checkAlarmActive()) {
                dataBaseRepository.updateAlarmWasFired(true, dataBaseRepository.getNextActiveAlarm()!!.id)
                dataBaseRepository.updateAlarmTempDisabled(true, dataBaseRepository.getNextActiveAlarm()!!.id)
            }*/

        }

    }

    fun stopForegroundService(inActivity : Boolean) {
        scope.launch {
            if (checkForegroundStatus()) {

                if (inActivity) {
                    val startForegroundIntent = Intent(context, ForegroundActivity::class.java)
                    startForegroundIntent.flags =
                        Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK
                    startForegroundIntent.putExtra("intent", 2)
                    context.startActivity(startForegroundIntent)
                } else {
                    ForegroundService.startOrStopForegroundService(Actions.STOP, context)
                }

                WorkManager.getInstance(context.applicationContext).cancelAllWorkByTag(context.getString(R.string.workmanager2_tag))
                AlarmClockReceiver.cancelAlarm(context.applicationContext, AlarmClockReceiverUsage.START_ALARMCLOCK);
                AlarmClockReceiver.cancelAlarm(context.applicationContext, AlarmClockReceiverUsage.LATEST_WAKEUP_ALARMCLOCK);

                //Cancel Alarm for starting Workmanager
                AlarmReceiver.cancelAlarm(context, AlarmReceiverUsage.START_WORKMANAGER_CALCULATION)
            }
        }
    }

    private suspend fun startForegroundService(inActivity : Boolean) {


            if (!checkForegroundStatus()) {
                if (inActivity) {
                    val startForegroundIntent = Intent(context, ForegroundActivity::class.java)
                    startForegroundIntent.flags =
                        Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK
                    startForegroundIntent.putExtra("intent", 1)
                    context.startActivity(startForegroundIntent)
                } else {
                    ForegroundService.startOrStopForegroundService(Actions.START, context)
                }


                //Set Alarm to start calculation
                if (checkAlarmActive()) {

                    if ((LocalTime.now().toSecondOfDay() < (getFirstWakeup() - 1800)) || (LocalTime.now().toSecondOfDay() > getSleepTimeBeginValue())) {
                        val calenderCalculation = TimeConverterUtil.getAlarmDate(getFirstWakeup() - 1800)
                        AlarmReceiver.startAlarmManager(
                            calenderCalculation[Calendar.DAY_OF_WEEK],
                            calenderCalculation[Calendar.HOUR_OF_DAY],
                            calenderCalculation[Calendar.MINUTE],
                            context,
                            AlarmReceiverUsage.START_WORKMANAGER_CALCULATION
                        )
                    } else {
                        WorkmanagerCalculation.startPeriodicWorkmanager(Constants.WORKMANAGER_CALCULATION_DURATION, context)
                    }

                    val calendar = TimeConverterUtil.getAlarmDate(getLastWakeup())
                    AlarmClockReceiver.startAlarmManager(calendar.get(Calendar.DAY_OF_WEEK), calendar.get(Calendar.HOUR_OF_DAY), calendar.get(Calendar.MINUTE), context.applicationContext, AlarmClockReceiverUsage.LATEST_WAKEUP_ALARMCLOCK);
                }
            }

            startWorkmanager()
    }

    fun startWorkmanager() {
        AlarmReceiver.cancelAlarm(context, AlarmReceiverUsage.START_FOREGROUND)

        //Start Workmanager at sleeptime and subscribe to SleepApi
        val periodicDataWork: PeriodicWorkRequest = PeriodicWorkRequest.Builder(Workmanager::class.java,
            Constants.WORKMANAGER_DURATION.toLong(),
            TimeUnit.MINUTES
        )
            .addTag(context.getString(R.string.workmanager1_tag)) //Tag is needed for canceling the periodic work
            .build()

        val workManager = WorkManager.getInstance(context)
        workManager.enqueueUniquePeriodicWork(context.getString(R.string.workmanager1_tag), ExistingPeriodicWorkPolicy.KEEP, periodicDataWork)

        Toast.makeText(context, "Workmanager started", Toast.LENGTH_LONG).show()

        sleepHandler.startSleepHandler()
        scope.launch {
            //Set AlarmManager to stop Workmanager at end of sleeptime
            val calendar = TimeConverterUtil.getAlarmDate(getSleepTimeEndValue())
            AlarmReceiver.startAlarmManager(
                calendar.get(Calendar.DAY_OF_WEEK),
                calendar.get(Calendar.HOUR_OF_DAY),
                calendar.get(Calendar.MINUTE), context, AlarmReceiverUsage.STOP_WORKMANAGER)
        }

    }

    fun beginOfSleepTime(inActivity: Boolean) {
        scope.launch {
            if (!checkAlarmFired() && checkAlarmActive() && !checkAlarmTempDisabled() && !checkForegroundStatus()) {

                if (!inActivity) {
                    startForegroundService(false)
                } else {
                    startForegroundService(true)
                }

                sleepCalculationHandler.checkIsUserSleeping(null)

            } else {
                startWorkmanager()
            }
        }
    }

    fun endOfSleepTime(inTheMorning : Boolean) {

        scope.launch {

            //Stop Workmanager at end of sleeptime and unsubscribe to SleepApi
            WorkManager.getInstance(context.applicationContext).cancelAllWorkByTag(context.getString(R.string.workmanager1_tag))
            Toast.makeText(context, "Workmanager stopped", Toast.LENGTH_LONG).show()

            sleepHandler.stopSleepHandler()
            defineNewUserWakeup(null, false)

            //Set AlarmManager to start Workmanager at begin of sleeptime
            val calendar = TimeConverterUtil.getAlarmDate(getSleepTimeBeginValue())
            AlarmReceiver.startAlarmManager(
                calendar.get(Calendar.DAY_OF_WEEK),
                calendar.get(Calendar.HOUR_OF_DAY),
                calendar.get(Calendar.MINUTE), context,
                AlarmReceiverUsage.START_FOREGROUND)

            if (checkAlarmActive()) {
                dataBaseRepository.updateAlarmWasFired(false, dataBaseRepository.getNextActiveAlarm()!!.id)
            }

            if (checkForegroundStatus()) {
                stopForegroundService(false)
            }

            if (inTheMorning) {
                if (checkAlarmActive()) {
                    resetTempDisabledAndWasFired()
                    dataStoreRepository.updateUserSleepTime(0)
                }
            }
        }
    }

    fun disableAlarmTemporaryInApp(fromApp : Boolean, reactivate : Boolean) {

        scope.launch {
            if (fromApp && !reactivate) {
                stopForegroundService(false)
                dataBaseRepository.updateAlarmTempDisabled(true, dataBaseRepository.getNextActiveAlarm()!!.id)
            } else if (!fromApp && !reactivate) {
                if (checkAlarmActive() && !checkAlarmTempDisabled()) {
                    dataBaseRepository.updateAlarmTempDisabled(true, dataBaseRepository.getNextActiveAlarm()!!.id)
                    val calendarStopForeground = Calendar.getInstance()
                    calendarStopForeground.add(Calendar.MINUTE, 2)
                    AlarmReceiver.startAlarmManager(calendarStopForeground.get(Calendar.DAY_OF_WEEK), calendarStopForeground.get(Calendar.HOUR_OF_DAY), calendarStopForeground.get(Calendar.MINUTE), context.applicationContext, AlarmReceiverUsage.STOP_FOREGROUND)
                    Toast.makeText(context.applicationContext,context.applicationContext.getString(R.string.disable_alarm_message), Toast.LENGTH_LONG).show()
                }
            } else if (!fromApp && reactivate) {
                if (checkAlarmActive() && checkAlarmTempDisabled()) {
                    dataBaseRepository.updateAlarmTempDisabled(false, dataBaseRepository.getNextActiveAlarm()!!.id)
                    AlarmReceiver.cancelAlarm(context.applicationContext, AlarmReceiverUsage.STOP_FOREGROUND)
                }
            } else if (fromApp && reactivate) {
                dataBaseRepository.updateAlarmTempDisabled(false, dataBaseRepository.getNextActiveAlarm()!!.id)
                startForegroundService(false)
            }
        }

    }

    fun chooseStateBeforeReboot() {

        scope.launch {
            Toast.makeText(context, "Choose", Toast.LENGTH_LONG).show()
            if (!checkInSleepTime() && (!checkAlarmActive() || !checkAlarmTempDisabled() || !checkAlarmFired()) && !checkForegroundStatus()) {
                executeStateAfterReboot(1)

                val calendar = Calendar.getInstance()
                val pref: SharedPreferences = context.getSharedPreferences("BootTime1", 0)
                val ed = pref.edit()
                ed.putInt("hour", calendar[Calendar.HOUR_OF_DAY])
                ed.putInt("minute", calendar[Calendar.MINUTE])
                ed.putInt("usage", 1)
                ed.apply()
            } else if (checkInSleepTime() && (!checkAlarmActive() || !checkAlarmTempDisabled() || !checkAlarmFired()) && !checkForegroundStatus()) {
                executeStateAfterReboot(2)

                val calendar = Calendar.getInstance()
                val pref: SharedPreferences = context.getSharedPreferences("BootTime1", 0)
                val ed = pref.edit()
                ed.putInt("hour", calendar[Calendar.HOUR_OF_DAY])
                ed.putInt("minute", calendar[Calendar.MINUTE])
                ed.putInt("usage", 2)
                ed.apply()
            } else if (checkInSleepTime() && checkAlarmActive() && !checkAlarmTempDisabled() && !checkAlarmFired() && checkForegroundStatus()) {
                Toast.makeText(context, "Alarm Detected", Toast.LENGTH_LONG).show()
                val time = LocalTime.now().toSecondOfDay()
                if (time > getFirstWakeup() && time < getLastWakeup()) {
                    executeStateAfterReboot(3)

                    val calendar = Calendar.getInstance()
                    val pref: SharedPreferences = context.getSharedPreferences("BootTime1", 0)
                    val ed = pref.edit()
                    ed.putInt("hour", calendar[Calendar.HOUR_OF_DAY])
                    ed.putInt("minute", calendar[Calendar.MINUTE])
                    ed.putInt("usage", 3)
                    ed.apply()
                } else if (time > (getFirstWakeup() - 1800) && time < getFirstWakeup()) {
                    executeStateAfterReboot(3)

                    val calendar = Calendar.getInstance()
                    val pref: SharedPreferences = context.getSharedPreferences("BootTime1", 0)
                    val ed = pref.edit()
                    ed.putInt("hour", calendar[Calendar.HOUR_OF_DAY])
                    ed.putInt("minute", calendar[Calendar.MINUTE])
                    ed.putInt("usage", 3)
                    ed.apply()
                } else if (time > getLastWakeup() && time < getSleepTimeBeginValue()) {
                    executeStateAfterReboot(5)

                    val calendar = Calendar.getInstance()
                    val pref: SharedPreferences = context.getSharedPreferences("BootTime1", 0)
                    val ed = pref.edit()
                    ed.putInt("hour", calendar[Calendar.HOUR_OF_DAY])
                    ed.putInt("minute", calendar[Calendar.MINUTE])
                    ed.putInt("usage", 5)
                    ed.apply()
                } else if ((time < (getFirstWakeup() - 1800)) || (time > getSleepTimeBeginValue())) {
                    executeStateAfterReboot(6)

                    val calendar = Calendar.getInstance()
                    val pref: SharedPreferences = context.getSharedPreferences("BootTime1", 0)
                    val ed = pref.edit()
                    ed.putInt("hour", calendar[Calendar.HOUR_OF_DAY])
                    ed.putInt("minute", calendar[Calendar.MINUTE])
                    ed.putInt("usage", 6)
                    ed.apply()
                }
            } else if (!checkInSleepTime() && (!checkAlarmActive() || !checkAlarmTempDisabled() || !checkAlarmFired()) && checkForegroundStatus()) {
                executeStateAfterReboot(4)

                val calendar = Calendar.getInstance()
                val pref: SharedPreferences = context.getSharedPreferences("BootTime1", 0)
                val ed = pref.edit()
                ed.putInt("hour", calendar[Calendar.HOUR_OF_DAY])
                ed.putInt("minute", calendar[Calendar.MINUTE])
                ed.putInt("usage", 4)
                ed.apply()
            } else if (!checkInSleepTime() && checkAlarmActive() && !checkAlarmTempDisabled() && !checkAlarmFired() && checkForegroundStatus()) {
                executeStateAfterReboot(4)

                val calendar = Calendar.getInstance()
                val pref: SharedPreferences = context.getSharedPreferences("BootTime1", 0)
                val ed = pref.edit()
                ed.putInt("hour", calendar[Calendar.HOUR_OF_DAY])
                ed.putInt("minute", calendar[Calendar.MINUTE])
                ed.putInt("usage", 4)
                ed.apply()
            } else if (!checkInSleepTime() && checkAlarmActive() && !checkAlarmTempDisabled() && !checkAlarmFired() && !checkForegroundStatus()) {
                executeStateAfterReboot(1)

                val calendar = Calendar.getInstance()
                val pref: SharedPreferences = context.getSharedPreferences("BootTime1", 0)
                val ed = pref.edit()
                ed.putInt("hour", calendar[Calendar.HOUR_OF_DAY])
                ed.putInt("minute", calendar[Calendar.MINUTE])
                ed.putInt("usage", 1)
                ed.apply()
            }
        }
    }

    private fun executeStateAfterReboot(state : Int) {

        Toast.makeText(context, "Execute", Toast.LENGTH_LONG).show()

        scope.launch {
            when (state) {
                1 -> {
                    val calendar = TimeConverterUtil.getAlarmDate(getSleepTimeBeginValue())
                    AlarmReceiver.startAlarmManager(
                        calendar.get(Calendar.DAY_OF_WEEK),
                        calendar.get(Calendar.HOUR_OF_DAY),
                        calendar.get(Calendar.MINUTE), context,
                        AlarmReceiverUsage.START_FOREGROUND)
                    WorkManager.getInstance(context.applicationContext).cancelAllWorkByTag(context.getString(R.string.workmanager1_tag))
                    resetTempDisabledAndWasFired()
                }
                2,6 -> {
                    if (state == 6) {
                        setForegroundStatus(false)
                    }
                    beginOfSleepTime(true)
                }
                3 -> {
                    setForegroundStatus(false)
                    startForegroundService(true)
                    AlarmReceiver.cancelAlarm(context, AlarmReceiverUsage.START_WORKMANAGER_CALCULATION)
                    WorkmanagerCalculation.startPeriodicWorkmanager(Constants.WORKMANAGER_CALCULATION_DURATION, context)
                }
                4 -> {
                    setForegroundStatus(false)
                    endOfSleepTime(true)
                }
                5 -> {
                    setForegroundStatus(false)
                    startWorkmanager()
                }
            }
        }
    }

    fun defineNewUserWakeup(localTime: LocalDateTime?, setAlarm:Boolean) {
        scope.launch {
            sleepCalculationHandler.defineUserWakeup(localTime, setAlarm)
        }
    }

    private suspend fun resetTempDisabledAndWasFired() {
        dataBaseRepository.resetAlarmTempDisabledWasFired()
        dataBaseRepository.resetActualWakeupTime(getFirstWakeup())
    }

    private suspend fun getSleepTimeBeginValue() =
        dataStoreRepository.getSleepTimeBegin()

    private suspend fun getSleepTimeEndValue() =
        dataStoreRepository.getSleepTimeEnd()

    private suspend fun getFirstWakeup() =
        (dataBaseRepository.getNextActiveAlarm()!!.wakeupEarly)

    private suspend fun getLastWakeup() =
        (dataBaseRepository.getNextActiveAlarm()!!.wakeupLate)

    private suspend fun checkForegroundStatus() =
        dataStoreRepository.backgroundServiceFlow.first().isForegroundActive

    private suspend fun checkAlarmActive(): Boolean =
        (dataBaseRepository.getNextActiveAlarm() != null)

    private suspend fun checkAlarmFired(): Boolean =
        (checkAlarmActive() && dataBaseRepository.getNextActiveAlarm()!!.wasFired)

    private suspend fun checkAlarmTempDisabled(): Boolean =
        (checkAlarmActive() && dataBaseRepository.getNextActiveAlarm()!!.tempDisabled)

    private suspend fun checkInSleepTime() : Boolean =
        dataStoreRepository.isInSleepTime(null)

    private suspend fun setForegroundStatus(status: Boolean) {
        dataStoreRepository.backgroundUpdateIsActive(status)
    }

    private suspend fun getAlarmId() : Int {
        if(checkAlarmActive()) {
            return dataBaseRepository.getNextActiveAlarm()!!.id
        }

        return -1
    }

    companion object {
        // For Singleton instantiation
        @Volatile
        private var INSTANCE: BackgroundAlarmTimeHandler? = null

        fun getHandler(context: Context): BackgroundAlarmTimeHandler {
            return INSTANCE ?: synchronized(this) {
                val instance = BackgroundAlarmTimeHandler(context)
                INSTANCE = instance
                // return instance
                instance
            }
        }
    }
}