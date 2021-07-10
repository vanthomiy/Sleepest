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
import com.doitstudio.sleepest_master.model.data.Actions
import com.doitstudio.sleepest_master.model.data.AlarmReceiverUsage
import com.doitstudio.sleepest_master.model.data.Constants
import com.doitstudio.sleepest_master.googleapi.SleepHandler
import com.doitstudio.sleepest_master.sleepcalculation.SleepCalculationHandler
import com.doitstudio.sleepest_master.storage.DataStoreRepository
import com.doitstudio.sleepest_master.storage.DatabaseRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
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


    fun changeSleepTime() {
        scope.launch {

            if (checkInSleepTime() && !checkAlarmFired() && !checkAlarmTempDisabled() && !checkForegroundStatus() && checkAlarmActive()) {
                beginOfSleepTime(false)
                AlarmReceiver.cancelAlarm(context, AlarmReceiverUsage.START_FOREGROUND)
            } else {
                if (checkForegroundStatus() && !checkInSleepTime()) {
                    val jsjs = checkInSleepTime()
                    val sdjsj = checkAlarmFired()
                    val sjhshs = checkAlarmTempDisabled()
                    val snoaspo = checkAlarmActive()
                    endOfSleepTime()
                    val a = jsjs
                    val b = sdjsj
                    val c = sjhshs
                    val d = snoaspo
                }

                if (getSleepTimeBeginValue() != sleepTimeBeginTemp) {
                    val calendarAlarm = AlarmReceiver.getAlarmDate(getSleepTimeBeginValue())
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

                    sleepTimeBeginTemp = getSleepTimeBeginValue()
                }




            }
        }
    }

    fun changeOfAlarmEntity(listEmpty : Boolean) {
        scope.launch {
            if (checkInSleepTime() && checkAlarmActive() && !checkForegroundStatus() && !checkAlarmFired() && !checkAlarmTempDisabled() && !listEmpty) {
                startForegroundService(false)
            } else if (listEmpty) {
                stopForegroundService(false)
            }
        }

    }

    fun alarmClockRang(isScreenOn : Boolean) {

        scope.launch {
            if (isScreenOn) {
                AlarmClockAudio.getInstance().stopAlarm(false)

                stopForegroundService(true)

                val calendar = Calendar.getInstance()
                var pref: SharedPreferences = context.getSharedPreferences("AlarmClock", 0)
                var ed = pref.edit()
                ed.putInt("hour", calendar[Calendar.HOUR_OF_DAY])
                ed.putInt("minute", calendar[Calendar.MINUTE])
                ed.apply()
            } else {
                AlarmClockAudio.getInstance().stopAlarm(false)

                val calendarAlarm = AlarmReceiver.getAlarmDate(getSleepTimeBeginValue())

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

    private suspend fun stopForegroundService(inActivity : Boolean) {

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

            //Cancel Alarm for starting Workmanager
            AlarmReceiver.cancelAlarm(context, AlarmReceiverUsage.START_WORKMANAGER_CALCULATION)
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
                    val calenderCalculation = AlarmReceiver.getAlarmDate(getFirstWakeup() - 1800)
                    AlarmReceiver.startAlarmManager(
                        calenderCalculation[Calendar.DAY_OF_WEEK],
                        calenderCalculation[Calendar.HOUR_OF_DAY],
                        calenderCalculation[Calendar.MINUTE],
                        context,
                        AlarmReceiverUsage.START_WORKMANAGER_CALCULATION
                    )
                }



            }

            startWorkmanager()

    }

    private suspend fun startWorkmanager() {
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

        //Set AlarmManager to stop Workmanager at end of sleeptime
        val calendar = AlarmReceiver.getAlarmDate(getSleepTimeEndValue())
        AlarmReceiver.startAlarmManager(
            calendar.get(Calendar.DAY_OF_WEEK),
            calendar.get(Calendar.HOUR_OF_DAY),
            calendar.get(Calendar.MINUTE), context, AlarmReceiverUsage.STOP_WORKMANAGER)
    }

    fun beginOfSleepTime(inActivity: Boolean) {
        scope.launch {
            if (!checkAlarmFired() && checkAlarmActive() && !checkAlarmTempDisabled() && !checkForegroundStatus()) {
                //Start foregroundservice with an activity
                /*val startForegroundIntent = Intent(context, ForegroundActivity::class.java)
                startForegroundIntent.flags = Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK
                startForegroundIntent.putExtra("intent", 1)
                context.startActivity(startForegroundIntent)*/
                if (!inActivity) {
                    startForegroundService(false)
                } else {
                    startForegroundService(true)
                }

                sleepCalculationHandler.checkIsUserSleeping(null)

            }

            startWorkmanager()

            /**AlarmReceiver.cancelAlarm(context, AlarmReceiverUsage.START_WORKMANAGER)

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

            //Set AlarmManager to stop Workmanager at end of sleeptime
            val calendar = AlarmReceiver.getAlarmDate(getSleepTimeEndValue())
            AlarmReceiver.startAlarmManager(
                calendar.get(Calendar.DAY_OF_WEEK),
                calendar.get(Calendar.HOUR_OF_DAY),
                calendar.get(Calendar.MINUTE), context, AlarmReceiverUsage.STOP_WORKMANAGER)**/

            /*if (LocalTime.now().toSecondOfDay() < getSleepTimeEndValue()) {
                AlarmReceiver.startAlarmManager(
                    calendar.get(Calendar.DAY_OF_WEEK),
                    calendar.get(Calendar.HOUR_OF_DAY),
                    calendar.get(
                        Calendar.MINUTE
                    ),
                    context,
                    AlarmReceiverUsage.STOP_FOREGROUND
                )
            } else {
                AlarmReceiver.startAlarmManager(
                    calendar.get(Calendar.DAY_OF_WEEK) + 1,
                    calendar.get(Calendar.HOUR_OF_DAY),
                    calendar.get(
                        Calendar.MINUTE
                    ),
                    context,
                    AlarmReceiverUsage.STOP_FOREGROUND
                )
            }*/
        }
    }

    fun endOfSleepTime() {

        scope.launch {

            //Stop Workmanager at end of sleeptime and unsubscribe to SleepApi
            //TODO: Überprüfen, ob der Workmanager noch richtig abgebrochen wird
            WorkManager.getInstance(context.applicationContext).cancelAllWorkByTag(context.getString(R.string.workmanager1_tag))


            sleepHandler.stopSleepHandler()
            sleepCalculationHandler.defineUserWakeup(null, false)

            //Set AlarmManager to start Workmanager at begin of sleeptime
            val calendar = AlarmReceiver.getAlarmDate(getSleepTimeBeginValue())
            AlarmReceiver.startAlarmManager(
                calendar.get(Calendar.DAY_OF_WEEK),
                calendar.get(Calendar.HOUR_OF_DAY),
                calendar.get(Calendar.MINUTE), context,
                AlarmReceiverUsage.START_FOREGROUND)

            if (checkAlarmActive()) {
                dataBaseRepository.updateAlarmWasFired(false, dataBaseRepository.getNextActiveAlarm()!!.id)
            }
        }

    }

    fun disableAlarmTemporaryInApp(fromApp : Boolean, reactivate : Boolean) {

        scope.launch {
            if (fromApp && !reactivate) {

            } else if (!fromApp && !reactivate) {
                if (checkAlarmActive() && !checkAlarmTempDisabled()) {
                    dataBaseRepository.updateAlarmTempDisabled(true, dataBaseRepository.getNextActiveAlarm()!!.id)
                    val calendarStopForeground = Calendar.getInstance()
                    AlarmReceiver.startAlarmManager(
                        calendarStopForeground.get(Calendar.DAY_OF_WEEK),
                        calendarStopForeground.get(
                            Calendar.HOUR_OF_DAY),
                        calendarStopForeground.get(Calendar.MINUTE) + 5,
                        context.applicationContext,
                        AlarmReceiverUsage.STOP_FOREGROUND)
                    Toast.makeText(
                        context.applicationContext,
                        context.applicationContext.getString(R.string.disable_alarm_message),
                        Toast.LENGTH_LONG
                    ).show()
                }
            } else if (!fromApp && reactivate) {

                if (checkAlarmActive() && checkAlarmTempDisabled()) {
                    dataBaseRepository.updateAlarmTempDisabled(false, dataBaseRepository.getNextActiveAlarm()!!.id)
                }

            } else if (fromApp && reactivate) {

            }
        }

    }

    fun deviceBoot() {
        scope.launch {
            if (checkForegroundStatus() && checkAlarmActive()) {
                if (!checkAlarmFired() && !checkAlarmTempDisabled() && checkInSleepTime()) {
                    startForegroundService(true)
                } else {
                    val calendar = AlarmReceiver.getAlarmDate(getSleepTimeBeginValue())
                    AlarmReceiver.startAlarmManager(calendar[Calendar.DAY_OF_WEEK],
                        calendar[Calendar.HOUR_OF_DAY], calendar[Calendar.MINUTE],
                        context.applicationContext,
                        AlarmReceiverUsage.START_FOREGROUND)
                    dataStoreRepository.backgroundUpdateIsActive(false)
                }
            } else if (!checkForegroundStatus() && checkAlarmActive()) {
                if (!checkAlarmFired() && !checkAlarmTempDisabled() && checkInSleepTime()) {
                    startForegroundService(true)
                } else {
                    if (!checkInSleepTime()) {
                        val calendar = AlarmReceiver.getAlarmDate(getSleepTimeBeginValue())
                        AlarmReceiver.startAlarmManager(calendar[Calendar.DAY_OF_WEEK],
                            calendar[Calendar.HOUR_OF_DAY], calendar[Calendar.MINUTE],
                            context.applicationContext,
                            AlarmReceiverUsage.START_FOREGROUND)
                    }
                }
            } else {
                if (!checkInSleepTime()) {
                    val calendar = AlarmReceiver.getAlarmDate(getSleepTimeBeginValue())
                    AlarmReceiver.startAlarmManager(
                        calendar[Calendar.DAY_OF_WEEK],
                        calendar[Calendar.HOUR_OF_DAY], calendar[Calendar.MINUTE],
                        context.applicationContext,
                        AlarmReceiverUsage.START_FOREGROUND
                    )
                }
            }

            val calendar = Calendar.getInstance()
            var pref: SharedPreferences = context.getSharedPreferences("BootTime1", 0)
            var ed = pref.edit()
            ed.putInt("hour", calendar[Calendar.HOUR_OF_DAY])
            ed.putInt("minute", calendar[Calendar.MINUTE])
            ed.apply()
        }

    }

    private suspend fun getSleepTimeBeginValue() =
        dataStoreRepository.getSleepTimeBegin()

    private suspend fun getSleepTimeEndValue() =
        dataStoreRepository.getSleepTimeEnd()

    private suspend fun getFirstWakeup() =
        (dataBaseRepository.getNextActiveAlarm()!!.wakeupEarly)

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