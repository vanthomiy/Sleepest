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
import com.doitstudio.sleepest_master.model.data.*
import com.doitstudio.sleepest_master.sleepcalculation.SleepCalculationHandler
import com.doitstudio.sleepest_master.storage.DataStoreRepository
import com.doitstudio.sleepest_master.storage.DatabaseRepository
import com.doitstudio.sleepest_master.util.TimeConverterUtil
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import java.time.LocalDateTime
import java.time.LocalTime
import java.util.*
import java.util.concurrent.TimeUnit
import android.content.ComponentName

import android.app.ActivityManager
import android.app.ActivityManager.RunningTaskInfo

import android.content.Context.ACTIVITY_SERVICE




/**
 * This class handles the hole alarm cycle and is the interface to the data set.
 * It starts and stop all alarms or workmanager which are active in the background.
 * This class is singelton.
 */

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

    //Temporary variables to detect if this value is changed or not
    private var sleepTimeBeginTemp = 0
    private var sleepTimeEndTemp = 0
    private var firstWakeupTemp = 0
    private var lastWakeupTemp = 0

    // Init some variables
    init {
        scope.launch {
            sleepTimeBeginTemp = getSleepTimeBeginValue()
            sleepTimeEndTemp = getSleepTimeEndValue()
            if (checkAlarmActive()) {
                firstWakeupTemp = getFirstWakeup()
                lastWakeupTemp = getLastWakeup()
            }
        }
    }

    /**
     * If the user changes the sleeptime in the settings, this function will be called.
     */
    fun changeSleepTime() {
        scope.launch {

            //User changes sleep time begin and is in sleep time now
            if (checkInSleepTime() && (getSleepTimeBeginValue() != sleepTimeBeginTemp)) {
                beginOfSleepTime(false)
                AlarmReceiver.cancelAlarm(context, AlarmReceiverUsage.START_FOREGROUND)
            } else {
                //User changes the end of sleep time and is not in sleep time anymore
                if ((sleepTimeEndTemp != getSleepTimeEndValue()) && !checkInSleepTime()) {
                    endOfSleepTime(false)
                }
                //User changes sleep time end and is still in sleep time
                else if (checkInSleepTime() && (sleepTimeEndTemp != getSleepTimeEndValue())) {
                    //Change the end of sleep time alarm
                    val calendar = TimeConverterUtil.getAlarmDate(getSleepTimeEndValue())
                    AlarmReceiver.startAlarmManager(calendar.get(Calendar.DAY_OF_WEEK), calendar.get(Calendar.HOUR_OF_DAY), calendar.get(Calendar.MINUTE), context.applicationContext, AlarmReceiverUsage.STOP_WORKMANAGER);
                }

                //User changes sleep time begin and is not in sleep time
                if ((getSleepTimeBeginValue() != sleepTimeBeginTemp) && !checkInSleepTime()) {
                    //Change the start of sleep time alarm
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

                    endOfSleepTime(false)

                }
            }

            //Set new sleep time parameters
            sleepTimeBeginTemp = getSleepTimeBeginValue()
            sleepTimeEndTemp = getSleepTimeEndValue()
        }
    }

    /**
     * This function will be called if the user change the settings of the next existing alarm
     * @param listEmpty True, if alarm list is empty
     */
    fun changeOfAlarmEntity(listEmpty : Boolean) {
        scope.launch {
            //Starts the foreground service if the user is in sleeptime and an alarm is active
            if (checkInSleepTime() && checkAlarmActive() && !checkForegroundStatus() && !checkAlarmFired() && !checkAlarmTempDisabled() && !listEmpty) {
                startForegroundService(false)
            }
            //Stops the foreground service if no alarm is active for the next day
            else if (listEmpty) {
                stopForegroundService(false)
            }

            //Alarm is already active and user is already in sleep time
            if (checkInSleepTime() && checkAlarmActive() && checkForegroundStatus() && !checkAlarmFired() && !checkAlarmTempDisabled() && !listEmpty) {

                //User changes first wakeup time of the alarm
                if (getFirstWakeup() != firstWakeupTemp) {
                    val calenderCalculation = TimeConverterUtil.getAlarmDate(getFirstWakeup() - Constants.CALCULATION_START_DIFFERENCE)
                    AlarmReceiver.startAlarmManager(
                        calenderCalculation[Calendar.DAY_OF_WEEK],
                        calenderCalculation[Calendar.HOUR_OF_DAY],
                        calenderCalculation[Calendar.MINUTE],
                        context,
                        AlarmReceiverUsage.START_WORKMANAGER_CALCULATION)
                }

                //User changes the last wakeup time of the alarm
                if (getLastWakeup() != lastWakeupTemp) {
                    val calendar = TimeConverterUtil.getAlarmDate(getLastWakeup())
                    AlarmClockReceiver.startAlarmManager(calendar.get(Calendar.DAY_OF_WEEK), calendar.get(Calendar.HOUR_OF_DAY), calendar.get(Calendar.MINUTE), context.applicationContext, AlarmClockReceiverUsage.LATEST_WAKEUP_ALARMCLOCK);
                }
            }

            //Set the new times if an alarm is active
            if (checkAlarmActive()) {
                lastWakeupTemp = getLastWakeup()
                firstWakeupTemp = getFirstWakeup()
            }


        }
    }

    /**
     * If the alarmclock is canceled, this function will be called
     * @param isScreenOn User interactive = true
     */
    fun alarmClockRang(isScreenOn : Boolean) {

        scope.launch {
            //The screen is not locked
            if (isScreenOn) {
                //Stops the ringtone
                AlarmClockAudio.getInstance().stopAlarm(false, isScreenOn)

                //Stops the foreground service
                stopForegroundService(true)
                Toast.makeText(context, "Alarmclock stopped", Toast.LENGTH_LONG).show()
                val calendar = Calendar.getInstance()
                val pref: SharedPreferences = context.getSharedPreferences("AlarmClock", 0)
                var ed = pref.edit()
                ed.putInt("hour", calendar[Calendar.HOUR_OF_DAY])
                ed.putInt("minute", calendar[Calendar.MINUTE])
                ed.apply()
            } else {
                //Stops the ringtone
                AlarmClockAudio.getInstance().stopAlarm(false, false)

                //Stops the foreground service
                stopForegroundService(false)

                val calendarAlarm = TimeConverterUtil.getAlarmDate(getSleepTimeBeginValue())

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

            //Updated the alarm that it was fired. So it can not be called again
            if (checkAlarmActive()) {
                dataBaseRepository.updateAlarmWasFired(true, dataBaseRepository.getNextActiveAlarm()!!.id)
            }
        }
    }

    /**
     * This function stops the foreground service and do all things to restart it on a specific time
     * @param inActivity true, if user is not in app
     */
    fun stopForegroundService(inActivity : Boolean) = runBlocking{

            if (checkForegroundStatus()) {

                //Stops the calculation and all alarm clocks
                WorkmanagerCalculation.stopPeriodicWorkmanager()
                AlarmClockReceiver.cancelAlarm(context.applicationContext, AlarmClockReceiverUsage.START_ALARMCLOCK);
                AlarmClockReceiver.cancelAlarm(context.applicationContext, AlarmClockReceiverUsage.LATEST_WAKEUP_ALARMCLOCK);

                //Cancel Alarm for starting Workmanager
                AlarmReceiver.cancelAlarm(context, AlarmReceiverUsage.START_WORKMANAGER_CALCULATION)

                //Stops the foreground service depending on the screen status (on/off)
                if (inActivity) {
                    val startForegroundIntent = Intent(context, ForegroundActivity::class.java)
                    startForegroundIntent.flags =
                        Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK
                    startForegroundIntent.putExtra("intent", 2)
                    context.startActivity(startForegroundIntent)
                } else {
                    ForegroundService.startOrStopForegroundService(Actions.STOP, context)
                }
            }
    }

    /**
     * This function starts the foreground service
     * @param inActivity true, if user is not in app
     */
    private suspend fun startForegroundService(inActivity : Boolean) {


            if (!checkForegroundStatus()) {
                //Starts the foreground service depending on the screen status (on/off)

                val am = context.getSystemService(ACTIVITY_SERVICE) as ActivityManager
                val tasks = am.getRunningTasks(1)
                val task = tasks[0] // current task

                val rootActivity = task.baseActivity


                val currentPackageName = rootActivity!!.packageName
                if (currentPackageName == "com.doitstudio.sleepest_master") {
                    ForegroundService.startOrStopForegroundService(Actions.START, context)
                } else {
                    val startForegroundIntent = Intent(context, ForegroundActivity::class.java)
                    startForegroundIntent.flags =
                        Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK
                    startForegroundIntent.putExtra("intent", 1)
                    context.startActivity(startForegroundIntent)
                }

                /*if (inActivity) {
                    val startForegroundIntent = Intent(context, ForegroundActivity::class.java)
                    startForegroundIntent.flags =
                        Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK
                    startForegroundIntent.putExtra("intent", 1)
                    context.startActivity(startForegroundIntent)
                } else {
                    ForegroundService.startOrStopForegroundService(Actions.START, context)
                }*/


                //Set Alarm to start calculation or start it immediately
                if (checkAlarmActive()) {

                    val alarmCycleState = AlarmCycleState(context)

                    if (alarmCycleState.getState() == AlarmCycleStates.BETWEEN_SLEEPTIME_START_AND_CALCULATION) {
                        val calenderCalculation = TimeConverterUtil.getAlarmDate(getFirstWakeup() - Constants.CALCULATION_START_DIFFERENCE)
                        AlarmReceiver.startAlarmManager(
                            calenderCalculation[Calendar.DAY_OF_WEEK],
                            calenderCalculation[Calendar.HOUR_OF_DAY],
                            calenderCalculation[Calendar.MINUTE],
                            context,
                            AlarmReceiverUsage.START_WORKMANAGER_CALCULATION
                        )
                    } else if ((alarmCycleState.getState() == AlarmCycleStates.BETWEEN_FIRST_AND_LAST_WAKEUP) || (alarmCycleState.getState() == AlarmCycleStates.BETWEEN_CALCULATION_AND_FIRST_WAKEUP)) {
                        WorkmanagerCalculation.startPeriodicWorkmanager(Constants.WORKMANAGER_CALCULATION_DURATION, context)
                    }

                    //Set the alarm clock for the latest wakeup
                    val calendar = TimeConverterUtil.getAlarmDate(getLastWakeup())
                    AlarmClockReceiver.startAlarmManager(calendar.get(Calendar.DAY_OF_WEEK), calendar.get(Calendar.HOUR_OF_DAY), calendar.get(Calendar.MINUTE), context.applicationContext, AlarmClockReceiverUsage.LATEST_WAKEUP_ALARMCLOCK);
                }
            }

            //Start the workmanager and cancel alarm for starting foregroundservice
            startWorkmanager()
            AlarmClockReceiver.cancelAlarm(context, AlarmClockReceiverUsage.START_ALARMCLOCK)
    }

    /**
     * This function starts the workmanager
     */
    fun startWorkmanager() {
        AlarmReceiver.cancelAlarm(context, AlarmReceiverUsage.START_FOREGROUND)

        //Start Workmanager at sleeptime
        val periodicDataWork: PeriodicWorkRequest = PeriodicWorkRequest.Builder(Workmanager::class.java,
            Constants.WORKMANAGER_DURATION.toLong(),
            TimeUnit.MINUTES)
            .addTag(context.getString(R.string.workmanager1_tag)) //Tag is needed for canceling the periodic work
            .build()

        val workManager = WorkManager.getInstance(context)
        workManager.enqueueUniquePeriodicWork(context.getString(R.string.workmanager1_tag), ExistingPeriodicWorkPolicy.KEEP, periodicDataWork)

        Toast.makeText(context, "Workmanager started", Toast.LENGTH_LONG).show()

        //Subscribe to SleepApi
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

    /**
     * Sequence at the start of sleep time
     * @param inActivity true, if user is not in app
     */
    fun beginOfSleepTime(inActivity: Boolean) {
        scope.launch {
            //In sleep time and foreground status is not true
            if (!checkAlarmFired() && checkAlarmActive() && !checkAlarmTempDisabled() && !checkForegroundStatus()) {

                //Start the foregroundservice
                if (!inActivity) {
                    startForegroundService(false)
                } else {
                    startForegroundService(true)
                }

                sleepCalculationHandler.checkIsUserSleeping(null)

            } else {
                //If no alarm is active, it starts only the workmanager to track data
                startWorkmanager()
            }
        }
    }

    /**
     * Sequence at the end of sleep time
     * @param inTheMorning true, if alarm clock calls this function
     */
    fun endOfSleepTime(inTheMorning : Boolean) {

        scope.launch {

            //Stop Workmanager at end of sleeptime
            WorkManager.getInstance(context.applicationContext).cancelAllWorkByTag(context.getString(R.string.workmanager1_tag))
            Toast.makeText(context, "Workmanager stopped", Toast.LENGTH_LONG).show()

            //Unsubscribe to SleepApi
            sleepHandler.stopSleepHandler()
            defineNewUserWakeup(null, false)

            //Set AlarmManager to start Workmanager at begin of sleeptime
            val calendar = TimeConverterUtil.getAlarmDate(getSleepTimeBeginValue())
            AlarmReceiver.startAlarmManager(
                calendar.get(Calendar.DAY_OF_WEEK),
                calendar.get(Calendar.HOUR_OF_DAY),
                calendar.get(Calendar.MINUTE), context,
                AlarmReceiverUsage.START_FOREGROUND)

            //Reset alarm fired
            if (checkForegroundStatus()) {
                stopForegroundService(false)
            }

            //Reset alarm temporary disabled and alarm fired property
            if (inTheMorning) {
                if (checkAlarmActive()) {
                    resetTempDisabledAndWasFired()
                    dataStoreRepository.updateUserSleepTime(0)
                }
            }
        }
    }

    /**
     * Sequence if alarm is disabled temporary
     * @param fromApp true, if button was detected in alarm fragment, false if from banner
     * @param reactivate Is already disabled and should be reactivated?
     */
    fun disableAlarmTemporaryInApp(fromApp : Boolean, reactivate : Boolean) {

        scope.launch {
            //Alarm was disabled in AlarmFragment
            if (fromApp && !reactivate) {
                stopForegroundService(false)
                dataBaseRepository.updateAlarmTempDisabled(true, dataBaseRepository.getNextActiveAlarm()!!.id)
            }
            //Alarm was disabled in notification
            else if (!fromApp && !reactivate) {
                if (checkAlarmActive() && !checkAlarmTempDisabled()) {
                    dataBaseRepository.updateAlarmTempDisabled(true, dataBaseRepository.getNextActiveAlarm()!!.id)
                    val calendarStopForeground = Calendar.getInstance()
                    calendarStopForeground.add(Calendar.MINUTE, Constants.DISABLE_ALARM_DELAY)
                    AlarmReceiver.startAlarmManager(calendarStopForeground.get(Calendar.DAY_OF_WEEK), calendarStopForeground.get(Calendar.HOUR_OF_DAY), calendarStopForeground.get(Calendar.MINUTE), context.applicationContext, AlarmReceiverUsage.STOP_FOREGROUND)
                    Toast.makeText(context.applicationContext,context.applicationContext.getString(R.string.disable_alarm_message), Toast.LENGTH_LONG).show()
                }
            }
            //Alarm was reactivated in notification
            else if (!fromApp && reactivate) {
                if (checkAlarmActive() && checkAlarmTempDisabled()) {
                    dataBaseRepository.updateAlarmTempDisabled(false, dataBaseRepository.getNextActiveAlarm()!!.id)
                    AlarmReceiver.cancelAlarm(context.applicationContext, AlarmReceiverUsage.STOP_FOREGROUND)
                }
            }
            //Alarm was reactivated in AlarmFragment
            else if (fromApp && reactivate) {
                dataBaseRepository.updateAlarmTempDisabled(false, dataBaseRepository.getNextActiveAlarm()!!.id)
                startForegroundService(false)
            }
        }

    }

    /**
     * This function will be called after a reboot of the device. It detects the state before the
     * reboot and decides which the right state is to execute.
     * Matrix of states is in Excel File.
     */
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
                } else if (time > (getFirstWakeup() - Constants.CALCULATION_START_DIFFERENCE) && time < getFirstWakeup()) {
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
                } else if ((time < (getFirstWakeup() - Constants.CALCULATION_START_DIFFERENCE)) || (time > getSleepTimeBeginValue())) {
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

    /**
     * Execute the chosen state, states 1-6 are in Excel file.
     * @param state The state which was chosen and should be executed
     */
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

    /**
     * Reset temporary disabled and alarm fired property
     */
    private suspend fun resetTempDisabledAndWasFired() {
        dataBaseRepository.resetAlarmTempDisabledWasFired()
        dataBaseRepository.resetActualWakeupTime(getFirstWakeup())
    }

    /**
     * Get the sleep time begin
     */
    private suspend fun getSleepTimeBeginValue() =
        dataStoreRepository.getSleepTimeBegin()

    /**
     * Get the sleep time end
     */
    private suspend fun getSleepTimeEndValue() =
        dataStoreRepository.getSleepTimeEnd()

    /**
     * Get the first wakeup of the next active alarm
     */
    private suspend fun getFirstWakeup() =
        (dataBaseRepository.getNextActiveAlarm()!!.wakeupEarly)

    /**
     * Get the last wakeup of the next active alarm
     */
    private suspend fun getLastWakeup() =
        (dataBaseRepository.getNextActiveAlarm()!!.wakeupLate)

    /**
     * Get the status of the foreground service
     */
    private suspend fun checkForegroundStatus() =
        dataStoreRepository.backgroundServiceFlow.first().isForegroundActive

    /**
     * Check, if an alarm is active for the next day
     */
    private suspend fun checkAlarmActive(): Boolean =
        (dataBaseRepository.getNextActiveAlarm() != null)

    /**
     * Get alarm fired status
     */
    private suspend fun checkAlarmFired(): Boolean =
        (checkAlarmActive() && dataBaseRepository.getNextActiveAlarm()!!.wasFired)

    /**
     * Get alarm temporary disabled status
     */
    private suspend fun checkAlarmTempDisabled(): Boolean =
        (checkAlarmActive() && dataBaseRepository.getNextActiveAlarm()!!.tempDisabled)

    /**
     * Checks if the user is in sleep time
     */
    private suspend fun checkInSleepTime() : Boolean =
        dataStoreRepository.isInSleepTime(null)

    /**
     * Get foreground service status
     */
    private suspend fun setForegroundStatus(status: Boolean) {
        dataStoreRepository.backgroundUpdateIsActive(status)
    }

    /**
     * Singleton instantiation
     */
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