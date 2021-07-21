package com.doitstudio.sleepest_master.background

import android.app.Notification
import android.app.NotificationManager
import android.content.Context
import android.content.SharedPreferences
import android.os.Build
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.work.*
import com.doitstudio.sleepest_master.MainApplication
import com.doitstudio.sleepest_master.R
import com.doitstudio.sleepest_master.model.data.NotificationUsage
import com.doitstudio.sleepest_master.sleepcalculation.SleepCalculationHandler
import com.doitstudio.sleepest_master.sleepcalculation.SleepCalculationHandler.Companion.getHandler
import com.doitstudio.sleepest_master.storage.DataStoreRepository
import com.doitstudio.sleepest_master.storage.DatabaseRepository
import com.doitstudio.sleepest_master.util.NotificationUtil
import com.doitstudio.sleepest_master.util.SleepUtil
import com.doitstudio.sleepest_master.util.SmileySelectorUtil
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import java.time.LocalTime
import java.util.*
import java.util.concurrent.TimeUnit


class Workmanager(appcontext: Context, workerParams: WorkerParameters) : Worker(appcontext, workerParams) {

    private val sleepCalculationHandler: SleepCalculationHandler by lazy { getHandler(applicationContext) }

    @RequiresApi(Build.VERSION_CODES.P)
    override fun doWork(): Result {

        val scope: CoroutineScope = MainScope()

        val dataStoreRepository: DataStoreRepository by lazy {
            (applicationContext as MainApplication).dataStoreRepository
        }

        scope.launch {
           /* if (dataStoreRepository.isInSleepTime() && (dataBaseRepository.getNextActiveAlarm() != null)) {
                // alarm should be active else set active
                if(!dataStoreRepository.backgroundServiceFlow.first().isForegroundActive) {

                    if (!dataBaseRepository.getNextActiveAlarm()!!.wasFired ||
                        ((LocalTime.now().toSecondOfDay() > dataBaseRepository.getNextActiveAlarm()!!.actualWakeup) &&
                                (dataStoreRepository.getSleepTimeBegin() < LocalTime.now().toSecondOfDay()))){
                        ForegroundService.startOrStopForegroundService(Actions.START, applicationContext);
                    }
                }
            }*/

            if (LocalTime.now().toSecondOfDay() >= dataStoreRepository.getSleepTimeBegin() &&
                ((LocalTime.now().toSecondOfDay() - ForegroundService.getForegroundServiceStartTime()) >= 60) && (dataStoreRepository.sleepApiDataFlow.first().sleepApiValuesAmount <= 3) &&
                dataStoreRepository.backgroundServiceFlow.first().isForegroundActive) {
                val notificationsUtil = NotificationUtil(applicationContext, NotificationUsage.NOTIFICATION_NO_API_DATA,null)
                notificationsUtil.chooseNotification()
            } else if (LocalTime.now().toSecondOfDay() > ForegroundService.getForegroundServiceStartTime() &&
                ((ForegroundService.getForegroundServiceStartTime() - LocalTime.now().toSecondOfDay()) >= 60) && (dataStoreRepository.sleepApiDataFlow.first().sleepApiValuesAmount <= 3) &&
                dataStoreRepository.backgroundServiceFlow.first().isForegroundActive) {
                val notificationsUtil = NotificationUtil(applicationContext, NotificationUsage.NOTIFICATION_NO_API_DATA,null)
                notificationsUtil.chooseNotification()
            }
        }

        sleepCalculationHandler.checkIsUserSleeping(null)

        val calendar: Calendar = Calendar.getInstance()

        val pref: SharedPreferences = applicationContext.getSharedPreferences("Workmanager", 0)
        val ed = pref.edit()
        ed.putInt("hour", calendar.get(Calendar.HOUR_OF_DAY))
        ed.putInt("minute", calendar.get(Calendar.MINUTE))
        ed.apply()

        return Result.success()
    }



    /*companion object {

        /**
         * Start the workmanager with a specific duration
         * @param duration Number >= 15 stands for duration in minutes
         */
        fun startPeriodicWorkmanager(duration: Int, context1: Context) {


            val periodicDataWork = PeriodicWorkRequest.Builder(Workmanager::class.java, duration.toLong(), TimeUnit.MINUTES)
                .addTag("Workmanager 1") //Tag is needed for canceling the periodic work
                .build()

            WorkManager.getInstance(context1).enqueueUniquePeriodicWork("Workmanager 1",
                ExistingPeriodicWorkPolicy.KEEP, periodicDataWork)


            Toast.makeText(context1, "Workmanager started", Toast.LENGTH_LONG).show()
        }

        fun stopPeriodicWorkmanager() {

            //Cancel periodic work by tag
            WorkManager.getInstance(applicationContext).cancelAllWorkByTag("Workmanager 1")
        }


    }*/

}