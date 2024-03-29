package com.sleepestapp.sleepest.background

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.widget.Toast
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.PeriodicWorkRequest
import androidx.work.WorkManager
import com.sleepestapp.sleepest.MainApplication
import com.sleepestapp.sleepest.R
import com.sleepestapp.sleepest.alarmclock.AlarmClockAudio
import com.sleepestapp.sleepest.alarmclock.AlarmClockReceiver
import com.sleepestapp.sleepest.googleapi.SleepHandler
import com.sleepestapp.sleepest.model.data.*
import com.sleepestapp.sleepest.sleepcalculation.SleepCalculationHandler
import com.sleepestapp.sleepest.storage.DataStoreRepository
import com.sleepestapp.sleepest.storage.DatabaseRepository
import com.sleepestapp.sleepest.util.TimeConverterUtil
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import java.time.LocalDateTime
import java.time.LocalTime
import java.util.*
import java.util.concurrent.TimeUnit
import android.app.ActivityManager
import android.app.ActivityManager.RunningAppProcessInfo.IMPORTANCE_FOREGROUND
import android.content.Context.ACTIVITY_SERVICE
import com.sleepestapp.sleepest.model.data.Constants
import com.sleepestapp.sleepest.util.SleepTimeValidationUtil
import kotlinx.coroutines.flow.first
import java.time.LocalDate


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
        SleepHandler(context)
    }
    private val sleepCalculationHandler : SleepCalculationHandler by lazy {
        SleepCalculationHandler(context)
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
                    val calendar = TimeConverterUtil.getAlarmDate(getSleepTimeEndValue() + 60)
                    AlarmReceiver.startAlarmManager(calendar.get(Calendar.DAY_OF_WEEK), calendar.get(Calendar.HOUR_OF_DAY), calendar.get(Calendar.MINUTE), context.applicationContext, AlarmReceiverUsage.STOP_WORKMANAGER)
                    beginOfSleepTime(false)
                }

                //User changes sleep time begin and is not in sleep time
                if ((getSleepTimeBeginValue() != sleepTimeBeginTemp) && !checkInSleepTime()) {
                    //Change the start of sleep time alarm
                    val calendarAlarm = TimeConverterUtil.getAlarmDate(getSleepTimeBeginValue())
                    AlarmReceiver.startAlarmManager(
                        calendarAlarm[Calendar.DAY_OF_WEEK],
                        calendarAlarm[Calendar.HOUR_OF_DAY],
                        calendarAlarm[Calendar.MINUTE], context, AlarmReceiverUsage.START_FOREGROUND)

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
                startForegroundService()
            }
            //Stops the foreground service if no alarm is active for the next day
            else if (listEmpty) {
                stopForegroundService()
            }

            //Alarm is already active and user is already in sleep time
            if (checkInSleepTime() && checkAlarmActive() && checkForegroundStatus() && !checkAlarmFired() && !checkAlarmTempDisabled() && !listEmpty) {

                val alarmCycleState = AlarmCycleState(context)

                //User changes first wakeup time of the alarm
                if (getFirstWakeup() != firstWakeupTemp && alarmCycleState.getState() == AlarmCycleStates.BETWEEN_SLEEPTIME_START_AND_CALCULATION) {
                    val calenderCalculation = TimeConverterUtil.getAlarmDate(getFirstWakeup() - Constants.CALCULATION_START_DIFFERENCE)
                    AlarmReceiver.startAlarmManager(
                        calenderCalculation[Calendar.DAY_OF_WEEK],
                        calenderCalculation[Calendar.HOUR_OF_DAY],
                        calenderCalculation[Calendar.MINUTE],
                        context,
                        AlarmReceiverUsage.START_WORKMANAGER_CALCULATION)
                } else if (getFirstWakeup() != firstWakeupTemp && alarmCycleState.getState() == AlarmCycleStates.BETWEEN_CALCULATION_AND_FIRST_WAKEUP ||
                    alarmCycleState.getState() == AlarmCycleStates.BETWEEN_FIRST_AND_LAST_WAKEUP) {
                    WorkmanagerCalculation.startPeriodicWorkmanager(Constants.WORK_MANAGER_CALCULATION_DURATION, context)
                }

                //User changes the last wakeup time of the alarm
                if (getLastWakeup() != lastWakeupTemp) {
                    val calendar = TimeConverterUtil.getAlarmDate(getLastWakeup())
                    AlarmClockReceiver.startAlarmManager(calendar.get(Calendar.DAY_OF_WEEK), calendar.get(Calendar.HOUR_OF_DAY), calendar.get(Calendar.MINUTE), context.applicationContext, AlarmClockReceiverUsage.LATEST_WAKEUP_ALARMCLOCK)
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

            //Stops the ringtone
            AlarmClockAudio.getInstance().stopAlarm(false, isScreenOn)

            //Stops the foreground service
            stopForegroundService()

            //Updated the alarm that it was fired. So it can not be called again
            if (checkAlarmActive()) {
                dataBaseRepository.updateAlarmWasFired(true, dataBaseRepository.getNextActiveAlarm(dataStoreRepository)!!.id)
            }
        }
    }

    /**
     * This function stops the foreground service and do all things to restart it on a specific time
     */
    fun stopForegroundService() = runBlocking{

            if (checkForegroundStatus()) {

                //Stops the calculation and all alarm clocks


                //Cancel periodic work by tag
                WorkManager.getInstance(context.applicationContext).cancelAllWorkByTag(context.getString(R.string.workmanager2_tag))
                AlarmClockReceiver.cancelAlarm(context.applicationContext, AlarmClockReceiverUsage.START_ALARMCLOCK)
                AlarmClockReceiver.cancelAlarm(context.applicationContext, AlarmClockReceiverUsage.LATEST_WAKEUP_ALARMCLOCK)

                //Cancel Alarm for starting Workmanager
                AlarmReceiver.cancelAlarm(context, AlarmReceiverUsage.START_WORKMANAGER_CALCULATION)

                //Stops the foreground service depending on the screen status (on/off)
                if (!isUserInApp()) {
                    val startForegroundIntent = Intent(context, ForegroundActivity::class.java)
                    startForegroundIntent.flags = Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK
                    startForegroundIntent.putExtra("intent", 2)
                    context.startActivity(startForegroundIntent)
                } else {
                    ForegroundService.startOrStopForegroundService(Actions.STOP, context)
                }
            }
    }

    /**
     * This function starts the foreground service
     */
    private suspend fun startForegroundService() {


            if (!checkForegroundStatus()) {
                //Starts the foreground service depending on the screen status (on/off)

                if (isUserInApp()) {
                    ForegroundService.startOrStopForegroundService(Actions.START, context)
                } else {
                    val startForegroundIntent = Intent(context, ForegroundActivity::class.java)
                    startForegroundIntent.flags =
                        Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK
                    startForegroundIntent.putExtra("intent", 1)
                    context.startActivity(startForegroundIntent)
                }

                //Don't delete this for future development!

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
                        WorkmanagerCalculation.startPeriodicWorkmanager(
                            Constants.WORK_MANAGER_CALCULATION_DURATION, context)
                    }

                    //Set the alarm clock for the latest wakeup
                    val calendar = TimeConverterUtil.getAlarmDate(getLastWakeup())
                    AlarmClockReceiver.startAlarmManager(calendar.get(Calendar.DAY_OF_WEEK), calendar.get(Calendar.HOUR_OF_DAY), calendar.get(Calendar.MINUTE), context.applicationContext, AlarmClockReceiverUsage.LATEST_WAKEUP_ALARMCLOCK)
                }
            }

            //Start the workmanager and cancel alarm for starting foregroundservice
            startWorkmanager()
            AlarmClockReceiver.cancelAlarm(context, AlarmClockReceiverUsage.START_ALARMCLOCK)
    }

    private fun isUserInApp() : Boolean {

        val activityManager = context.getSystemService(ACTIVITY_SERVICE) as ActivityManager
        val processInfo = activityManager.runningAppProcesses

        if (processInfo.size > 0) {
            for (i in processInfo.indices) {
                if (processInfo[i].processName == context.packageName && processInfo[i].importance == IMPORTANCE_FOREGROUND) {
                    return true
                }
            }
        }

        return false
    }

    /**
     * This function starts the workmanager
     */
    fun startWorkmanager() {
        AlarmReceiver.cancelAlarm(context, AlarmReceiverUsage.START_FOREGROUND)

        //Start Workmanager at sleeptime
        val periodicDataWork: PeriodicWorkRequest = PeriodicWorkRequest.Builder(Workmanager::class.java,
            Constants.WORK_MANAGER_DURATION.toLong(),
            TimeUnit.MINUTES)
            .addTag(context.getString(R.string.workmanager1_tag)) //Tag is needed for canceling the periodic work
            .build()

        val workManager = WorkManager.getInstance(context)
        workManager.enqueueUniquePeriodicWork(context.getString(R.string.workmanager1_tag), ExistingPeriodicWorkPolicy.KEEP, periodicDataWork)

        //Subscribe to SleepApi
        sleepHandler.startSleepHandler()
        scope.launch {
            //Set AlarmManager to stop Workmanager at end of sleeptime
            val calendar = TimeConverterUtil.getAlarmDate(getSleepTimeEndValue() + 60)
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
                    startForegroundService()
                } else {
                    startForegroundService()
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

            //Unsubscribe to SleepApi
            sleepHandler.stopSleepHandler()
            val date = LocalDate.now()
            val time = LocalTime.ofSecondOfDay(getSleepTimeEndValue().toLong()).minusMinutes(5)

            defineNewUserWakeup(date.atTime(time), false)

            //Set AlarmManager to start Workmanager at begin of sleeptime
            val calendar = TimeConverterUtil.getAlarmDate(getSleepTimeBeginValue())
            AlarmReceiver.startAlarmManager(
                calendar.get(Calendar.DAY_OF_WEEK),
                calendar.get(Calendar.HOUR_OF_DAY),
                calendar.get(Calendar.MINUTE), context,
                AlarmReceiverUsage.START_FOREGROUND)

            //Reset alarm fired
            if (checkForegroundStatus()) {
                stopForegroundService()
            }

            //Reset alarm temporary disabled and alarm fired property
            if (inTheMorning) {
                if (checkAlarmActive()) {
                    resetTempAlarmProperties()
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
                stopForegroundService()
                dataBaseRepository.updateAlarmTempDisabled(true, dataBaseRepository.getNextActiveAlarm(dataStoreRepository)!!.id)
            }
            //Alarm was disabled in notification
            else if (!fromApp && !reactivate) {
                if (checkAlarmActive() && !checkAlarmTempDisabled()) {
                    dataBaseRepository.updateAlarmTempDisabled(true, dataBaseRepository.getNextActiveAlarm(dataStoreRepository)!!.id)
                    val calendarStopForeground = Calendar.getInstance()
                    calendarStopForeground.add(Calendar.MINUTE, Constants.DISABLE_ALARM_DELAY)
                    AlarmReceiver.startAlarmManager(calendarStopForeground.get(Calendar.DAY_OF_WEEK), calendarStopForeground.get(Calendar.HOUR_OF_DAY), calendarStopForeground.get(Calendar.MINUTE), context.applicationContext, AlarmReceiverUsage.STOP_FOREGROUND)
                    Toast.makeText(context.applicationContext,context.applicationContext.getString(R.string.disable_alarm_message), Toast.LENGTH_LONG).show()
                }
            }
            //Alarm was reactivated in notification
            else if (!fromApp && reactivate) {
                if (checkAlarmActive() && checkAlarmTempDisabled()) {
                    dataBaseRepository.updateAlarmTempDisabled(false, dataBaseRepository.getNextActiveAlarm(dataStoreRepository)!!.id)
                    AlarmReceiver.cancelAlarm(context.applicationContext, AlarmReceiverUsage.STOP_FOREGROUND)
                }
            }
            //Alarm was reactivated in AlarmFragment
            else if (fromApp && reactivate) {
                dataBaseRepository.updateAlarmTempDisabled(false, dataBaseRepository.getNextActiveAlarm(dataStoreRepository)!!.id)
                startForegroundService()
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
            if (!checkInSleepTime() && (!checkAlarmActive() || !checkAlarmTempDisabled() || !checkAlarmFired()) && !checkForegroundStatus()) {
                executeStateAfterReboot(1)
            } else if (checkInSleepTime() && (!checkAlarmActive() || !checkAlarmTempDisabled() || !checkAlarmFired()) && !checkForegroundStatus()) {
                executeStateAfterReboot(2)
            } else if (checkInSleepTime() && checkAlarmActive() && !checkAlarmTempDisabled() && !checkAlarmFired() && checkForegroundStatus()) {
                val time = LocalTime.now().toSecondOfDay()
                if (time > getFirstWakeup() && time < getLastWakeup()) {
                    executeStateAfterReboot(3)
                } else if (time > (getFirstWakeup() - Constants.CALCULATION_START_DIFFERENCE) && time < getFirstWakeup()) {
                    executeStateAfterReboot(3)
                } else if (time > getLastWakeup() && time < getSleepTimeBeginValue()) {
                    executeStateAfterReboot(5)
                } else if ((time < (getFirstWakeup() - Constants.CALCULATION_START_DIFFERENCE)) || (time > getSleepTimeBeginValue())) {
                    executeStateAfterReboot(6)
                }
            } else if (!checkInSleepTime() && (!checkAlarmActive() || !checkAlarmTempDisabled() || !checkAlarmFired()) && checkForegroundStatus()) {
                executeStateAfterReboot(4)
            } else if (!checkInSleepTime() && checkAlarmActive() && !checkAlarmTempDisabled() && !checkAlarmFired() && checkForegroundStatus()) {
                executeStateAfterReboot(4)
            } else if (!checkInSleepTime() && checkAlarmActive() && !checkAlarmTempDisabled() && !checkAlarmFired() && !checkForegroundStatus()) {
                executeStateAfterReboot(1)
            }
        }
    }

    /**
     * Execute the chosen state, states 1-6 are in Excel file.
     * @param state The state which was chosen and should be executed
     */
    private fun executeStateAfterReboot(state : Int) {

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
                    resetTempAlarmProperties()
                }
                2,6 -> {
                    if (state == 6) {
                        setForegroundStatus(false)
                    }
                    beginOfSleepTime(true)
                }
                3 -> {
                    setForegroundStatus(false)
                    startForegroundService()
                    AlarmReceiver.cancelAlarm(context, AlarmReceiverUsage.START_WORKMANAGER_CALCULATION)
                    WorkmanagerCalculation.startPeriodicWorkmanager(
                        Constants.WORK_MANAGER_CALCULATION_DURATION, context)
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
    private suspend fun resetTempAlarmProperties() {
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
    private suspend fun getFirstWakeup() : Int {
        return (dataBaseRepository.getNextActiveAlarm(dataStoreRepository)!!.wakeupEarly)
    }

    /**
     * Get the last wakeup of the next active alarm
     */
    private suspend fun getLastWakeup() : Int {
        return (dataBaseRepository.getNextActiveAlarm(
            dataStoreRepository
        )!!.wakeupLate)
    }
    /**
     * Get the status of the foreground service
     */
    private suspend fun checkForegroundStatus() =
        dataStoreRepository.backgroundServiceFlow.first().isForegroundActive

    /**
     * Check, if an alarm is active for the next day
     */
    private suspend fun checkAlarmActive(): Boolean {
        return (dataBaseRepository.getNextActiveAlarm(dataStoreRepository) != null)
    }

    /**
     * Get alarm fired status
     */
    private suspend fun checkAlarmFired(): Boolean {
        return (checkAlarmActive() && dataBaseRepository.getNextActiveAlarm(dataStoreRepository)!!.wasFired)
    }

    /**
     * Get alarm temporary disabled status
     */
    private suspend fun checkAlarmTempDisabled(): Boolean {
        return (checkAlarmActive() && dataBaseRepository.getNextActiveAlarm(dataStoreRepository)!!.tempDisabled)
    }


    /**
     * Checks if the user is in sleep time
     */
    private suspend fun checkInSleepTime() : Boolean =
        SleepTimeValidationUtil.getActualAlarmTimeData(dataStoreRepository).isInSleepTime
        //dataStoreRepository.isInSleepTime(null)

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
        @SuppressLint("StaticFieldLeak")
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