package com.sleepestapp.sleepest.background

import android.content.Context
import android.content.SharedPreferences
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.work.*
import com.sleepestapp.sleepest.MainApplication
import com.sleepestapp.sleepest.model.data.NotificationUsage
import com.sleepestapp.sleepest.sleepcalculation.SleepCalculationHandler
import com.sleepestapp.sleepest.storage.DataStoreRepository
import com.sleepestapp.sleepest.storage.DatabaseRepository
import com.sleepestapp.sleepest.tools.SpotifyHandler
import com.sleepestapp.sleepest.util.NotificationUtil
import com.sleepestapp.sleepest.util.SleepTimeValidationUtil
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

        //region spotify

        scope.launch {

            if (dataStoreRepository.liveUserSleepActivityFlow.first().isUserSleeping && dataStoreRepository.getSpotifyEnabled()) {

                val spotifyHandler = SpotifyHandler()
                if (spotifyHandler.isConnected() == true) {
                    spotifyHandler.stopPlayer()
                    spotifyHandler.disconnect()

                    if (dataStoreRepository.liveUserSleepActivityFlow.first().userSleepTime < 13) {
                        val time = """  ${LocalDateTime.now().hour}:${LocalDateTime.now().minute} """.trimIndent()

                        val pref: SharedPreferences = applicationContext.getSharedPreferences("SpotifyStopTime", 0)
                        val ed = pref.edit()
                        ed.putString("time", time)
                        ed.apply()
                    }


                } else {
                    try {
                        spotifyHandler.connect(applicationContext)
                        spotifyHandler.stopPlayer()
                        spotifyHandler.disconnect()

                        if (dataStoreRepository.liveUserSleepActivityFlow.first().userSleepTime < 13) {
                            val time = """  ${LocalDateTime.now().hour}:${LocalDateTime.now().minute} """.trimIndent()

                            val pref: SharedPreferences = applicationContext.getSharedPreferences("SpotifyStopTime", 0)
                            val ed = pref.edit()
                            ed.putString("time", time)
                            ed.apply()
                        }

                    } catch (error: Throwable) {

                        val pref: SharedPreferences = applicationContext.getSharedPreferences("SpotifyStopTime", 0)
                        val ed = pref.edit()
                        ed.putString("time", error.toString())
                        ed.apply()
                    }
                }
            }

            //endregion

            // Check if foreground is active
            if (dataStoreRepository.backgroundServiceFlow.first().isForegroundActive) {

                val endTime = dataStoreRepository.sleepParameterFlow.first().sleepTimeEnd

                //Get sleep data table
                val sleepApiRawDataEntity =
                    dataBaseRepository.getSleepApiRawDataFromDateLive(LocalDateTime.now(), endTime).first()
                        ?.sortedByDescending { x -> x.timestampSeconds }

                //Check if data are in table
                if (sleepApiRawDataEntity != null && sleepApiRawDataEntity.count() > 0) {
                    val lastTimestampInSeconds = sleepApiRawDataEntity.first().timestampSeconds
                    val actualTimestampSeconds = System.currentTimeMillis()/1000

                    // Check if data were received regularly
                    if (ForegroundService.getForegroundServiceTime(applicationContext) >= 1200 && ((actualTimestampSeconds - lastTimestampInSeconds) > 600) && SleepTimeValidationUtil.getActualAlarmTimeData(dataStoreRepository).isInSleepTime) {
                        val notificationsUtil =
                            NotificationUtil(
                                applicationContext,
                                NotificationUsage.NOTIFICATION_NO_API_DATA,
                                null
                            )
                        notificationsUtil.chooseNotification()
                        //Restarts the subscribing of data in case of an receiving error
                        BackgroundAlarmTimeHandler.getHandler(applicationContext.applicationContext).startWorkmanager()
                    } else {
                        if (NotificationUtil.isNotificationActive(NotificationUsage.NOTIFICATION_NO_API_DATA, applicationContext)) {
                            NotificationUtil.cancelNotification(NotificationUsage.NOTIFICATION_NO_API_DATA, applicationContext)
                        }
                    }
                } else if (ForegroundService.getForegroundServiceTime(applicationContext) >= 1200 && (sleepApiRawDataEntity == null || sleepApiRawDataEntity.count() == 0)) {
                    val notificationsUtil =
                        NotificationUtil(
                            applicationContext,
                            NotificationUsage.NOTIFICATION_NO_API_DATA,
                            null
                        )
                    notificationsUtil.chooseNotification()
                    BackgroundAlarmTimeHandler.getHandler(applicationContext.applicationContext).startWorkmanager()
                } else {
                    if (NotificationUtil.isNotificationActive(NotificationUsage.NOTIFICATION_NO_API_DATA, applicationContext)) {
                        NotificationUtil.cancelNotification(NotificationUsage.NOTIFICATION_NO_API_DATA, applicationContext)
                    }
                }
            }
        }

        scope.launch {
            sleepCalculationHandler.checkIsUserSleeping(null)
        }

        return Result.success()
    }

}