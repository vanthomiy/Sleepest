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
import com.doitstudio.sleepest_master.model.data.MobilePosition
import com.doitstudio.sleepest_master.model.data.NotificationUsage
import com.doitstudio.sleepest_master.sleepcalculation.SleepCalculationHandler
import com.doitstudio.sleepest_master.sleepcalculation.SleepCalculationHandler.Companion.getHandler
import com.doitstudio.sleepest_master.storage.DataStoreRepository
import com.doitstudio.sleepest_master.storage.DatabaseRepository
import com.doitstudio.sleepest_master.storage.db.SleepApiRawDataEntity
import com.doitstudio.sleepest_master.util.NotificationUtil
import com.doitstudio.sleepest_master.util.SleepUtil
import com.doitstudio.sleepest_master.util.SmileySelectorUtil
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import java.time.*
import java.util.*
import java.util.concurrent.TimeUnit

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

        val sleepCalculationHandler : SleepCalculationHandler = getHandler(applicationContext)


        scope.launch {

            // Check if foreground is active
            if (dataStoreRepository.backgroundServiceFlow.first().isForegroundActive) {

                val pref: SharedPreferences = applicationContext.getSharedPreferences("ForegroundServiceTime", 0)
                val ed = pref.edit()
                ed.putInt("time", ForegroundService.getForegroundServiceTime())
                ed.apply()

                //Get sleep data table
                val sleepApiRawDataEntity =
                    dataBaseRepository.getSleepApiRawDataFromDateLive(LocalDateTime.now()).first()
                        ?.sortedByDescending { x -> x.timestampSeconds }

                //Check if data are in table
                if (sleepApiRawDataEntity != null && sleepApiRawDataEntity.count() > 0) {
                    val lastTimestampInSeconds = sleepApiRawDataEntity.first().timestampSeconds
                    val actualTimestampSeconds = System.currentTimeMillis()/1000
                    Toast.makeText(applicationContext, (actualTimestampSeconds - lastTimestampInSeconds).toString(), Toast.LENGTH_LONG).show()
                    Toast.makeText(applicationContext, ForegroundService.getForegroundServiceTime().toString(), Toast.LENGTH_LONG).show()

                    // Check if data were received regularly
                    if (ForegroundService.getForegroundServiceTime() >= 1200 && ((actualTimestampSeconds - lastTimestampInSeconds) > 600) && dataStoreRepository.isInSleepTime(null)) {
                        val notificationsUtil = NotificationUtil(applicationContext, NotificationUsage.NOTIFICATION_NO_API_DATA,null)
                        notificationsUtil.chooseNotification()
                    }
                } else if (ForegroundService.getForegroundServiceTime() >= 1200 && (sleepApiRawDataEntity == null || sleepApiRawDataEntity.count() == 0)) {
                    val notificationsUtil = NotificationUtil(applicationContext, NotificationUsage.NOTIFICATION_NO_API_DATA,null)
                    notificationsUtil.chooseNotification()
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

        /*scope.launch {
            sleepCalculationHandler.checkIsUserSleeping(null)
        }*/

        sleepCalculationHandler.checkIsUserSleepingJob(null)

        return Result.success()
    }

}