package com.doitstudio.Activityest_master.Activityapi

import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.doitstudio.sleepest_master.MainApplication
import com.doitstudio.sleepest_master.storage.db.ActivityApiRawDataEntity
import com.google.android.gms.location.ActivityTransitionResult
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.launch
import java.time.LocalDateTime
import java.time.ZoneOffset

/**
 * Saves Activity Events to Database.
 */
class ActivityReciver : BroadcastReceiver(){

    // Used to launch coroutines (non-blocking way to insert data).
    private val scope: CoroutineScope = MainScope()

    override fun onReceive(context: Context, intent: Intent) {
        val repository = (context.applicationContext as MainApplication).dataBaseRepository
        val dataStoreRepository = (context.applicationContext as MainApplication).dataStoreRepository

        if (ActivityTransitionResult.hasResult(intent)) {
            val result = ActivityTransitionResult.extractResult(intent)
            scope.launch {

                result?.let {

                    val convertedToEntityVersion = mutableListOf<ActivityApiRawDataEntity>()
                    it.transitionEvents.forEach { transition ->

                        // update the amount of data that is beeing recived
                        dataStoreRepository.updateActivityApiValuesAmount(
                                1
                        )

                        convertedToEntityVersion.add(
                                ActivityApiRawDataEntity(
                                        LocalDateTime.now().toEpochSecond(ZoneOffset.UTC).toInt(),
                                        transition.activityType,
                                        (transition.elapsedRealTimeNanos / 60000000000).toInt()
                                )
                        )
                    }

                    // Update the raw Activity api data
                    repository.insertActivityApiRawData(convertedToEntityVersion)



                }
            }
        }
    }

    companion object {
        const val TAG = "ActivityReceiver"
        fun createActivityReceiverPendingIntent(context: Context): PendingIntent {
            val activityIntent = Intent(context, ActivityReciver::class.java)
            return PendingIntent.getBroadcast(
                    context,
                    0,
                    activityIntent,
                    PendingIntent.FLAG_CANCEL_CURRENT
            )
        }
    }


}

