package com.sleepestapp.sleepest.background

import android.content.Context
import android.content.SharedPreferences
import android.os.Build
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.work.*
import com.sleepestapp.sleepest.MainApplication
import com.sleepestapp.sleepest.model.data.NotificationUsage
import com.sleepestapp.sleepest.sleepcalculation.SleepCalculationHandler
import com.sleepestapp.sleepest.storage.DataStoreRepository
import com.sleepestapp.sleepest.storage.DatabaseRepository
import com.sleepestapp.sleepest.util.NotificationUtil
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import java.time.*
import java.util.*

/**
 * Thr Workmanager is called periodically. It checks the user sleep state and the correct receive of
 * sleep data
 */

class Workmanager(context: Context, workerParams: WorkerParameters) : Worker(context, workerParams) {

    @RequiresApi(Build.VERSION_CODES.P)
    override fun doWork(): Result {

        val scope: CoroutineScope = MainScope()

        val dataStoreRepository: DataStoreRepository by lazy {
            (applicationContext as MainApplication).dataStoreRepository
        }

        val dataBaseRepository: DatabaseRepository by lazy {
            (applicationContext as MainApplication).dataBaseRepository
        }

        val sleepCalculationHandler = SleepCalculationHandler(applicationContext)


        scope.launch {

            // Check if foreground is active
            if (dataStoreRepository.backgroundServiceFlow.first().isForegroundActive) {

                val pref: SharedPreferences = applicationContext.getSharedPreferences("ForegroundServiceTime", 0)
                val ed = pref.edit()
                ed.putInt("time", ForegroundService.getForegroundServiceTime())
                ed.apply()

                val endTime = dataStoreRepository.sleepParameterFlow.first().sleepTimeEnd

                //Get sleep data table
                val sleepApiRawDataEntity =
                    dataBaseRepository.getSleepApiRawDataFromDateLive(LocalDateTime.now(), endTime).first()
                        ?.sortedByDescending { x -> x.timestampSeconds }

                //Check if data are in table
                if (sleepApiRawDataEntity != null && sleepApiRawDataEntity.count() > 0) {
                    val lastTimestampInSeconds = sleepApiRawDataEntity.first().timestampSeconds
                    val actualTimestampSeconds = System.currentTimeMillis()/1000
                    Toast.makeText(applicationContext, (actualTimestampSeconds - lastTimestampInSeconds).toString(), Toast.LENGTH_LONG).show()
                    Toast.makeText(applicationContext, ForegroundService.getForegroundServiceTime().toString(), Toast.LENGTH_LONG).show()

                    // Check if data were received regularly
                    if (ForegroundService.getForegroundServiceTime() >= 1200 && ((actualTimestampSeconds - lastTimestampInSeconds) > 600) && dataStoreRepository.isInSleepTime(null)) {
                        val notificationsUtil =
                            NotificationUtil(
                                applicationContext,
                                NotificationUsage.NOTIFICATION_NO_API_DATA,
                                null
                            )
                        notificationsUtil.chooseNotification()
                        //Restarts the subscribing of data in case of an receiving error
                        Toast.makeText(applicationContext,"Restarted sleepdata tracking", Toast.LENGTH_LONG).show()
                        BackgroundAlarmTimeHandler.getHandler(applicationContext.applicationContext).startWorkmanager()
                    } else {
                        if (NotificationUtil.isNotificationActive(NotificationUsage.NOTIFICATION_NO_API_DATA, applicationContext)) {
                            NotificationUtil.cancelNotification(NotificationUsage.NOTIFICATION_NO_API_DATA, applicationContext)
                        }
                    }
                } else if (ForegroundService.getForegroundServiceTime() >= 1200 && (sleepApiRawDataEntity == null || sleepApiRawDataEntity.count() == 0)) {
                    val notificationsUtil =
                        NotificationUtil(
                            applicationContext,
                            NotificationUsage.NOTIFICATION_NO_API_DATA,
                            null
                        )
                    notificationsUtil.chooseNotification()
                    Toast.makeText(applicationContext,"Restarted sleepdata tracking", Toast.LENGTH_LONG).show()
                    BackgroundAlarmTimeHandler.getHandler(applicationContext.applicationContext).startWorkmanager()
                } else {
                    if (NotificationUtil.isNotificationActive(NotificationUsage.NOTIFICATION_NO_API_DATA, applicationContext)) {
                        NotificationUtil.cancelNotification(NotificationUsage.NOTIFICATION_NO_API_DATA, applicationContext)
                    }
                }
            }
        }

        val calendar: Calendar = Calendar.getInstance()

        val pref: SharedPreferences = applicationContext.getSharedPreferences("Workmanager", 0)
        val ed = pref.edit()
        ed.putInt("day", calendar.get(Calendar.DAY_OF_WEEK))
        ed.putInt("hour", calendar.get(Calendar.HOUR_OF_DAY))
        ed.putInt("minute", calendar.get(Calendar.MINUTE))
        ed.apply()

        scope.launch {
            sleepCalculationHandler.checkIsUserSleeping(null)
        }

        return Result.success()
    }

}