package com.sleepestapp.sleepest.googleapi

import android.annotation.SuppressLint
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.sleepestapp.sleepest.MainApplication
import com.sleepestapp.sleepest.storage.db.ActivityApiRawDataEntity
import com.google.android.gms.location.ActivityTransitionResult
import com.sleepestapp.sleepest.model.data.ActivityTransitionUsage
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.launch
import java.time.LocalDateTime
import java.time.ZoneOffset

/**
 * Receives and saves activity events to database.
 */
class ActivityTransitionReceiver : BroadcastReceiver() {


    /**
     * [CoroutineScope] provides the ability to write and read from the database/datastore async
     */
    private val scope: CoroutineScope = MainScope()

    /**
     * Is called when new activity data is available
     */
    override fun onReceive(context: Context, intent: Intent) {

        val repository = (context.applicationContext as MainApplication).dataBaseRepository
        val dataStoreRepository = (context.applicationContext as MainApplication).dataStoreRepository

        if (ActivityTransitionResult.hasResult(intent)) {
            val result = ActivityTransitionResult.extractResult(intent)
            scope.launch {

                result?.let {

                    val convertedToEntityVersion = mutableListOf<ActivityApiRawDataEntity>()
                    it.transitionEvents.forEach { transition ->

                        convertedToEntityVersion.add(
                            ActivityApiRawDataEntity(
                                LocalDateTime.now().atZone(ZoneOffset.systemDefault()).toEpochSecond().toInt(),
                                transition.activityType,
                                transition.transitionType
                            )
                        )

                    }

                    // Update the raw Activity api data
                    repository.insertActivityApiRawData(convertedToEntityVersion)

                    // update the amount of data that is being received
                    dataStoreRepository.updateActivityApiValuesAmount(
                        it.transitionEvents.count()
                    )

                }
            }
        }
    }

    companion object {
        /**
         * The actual intent for the subscription
         */
        @SuppressLint("UnspecifiedImmutableFlag")
        fun createActivityTransitionReceiverPendingIntent(context: Context): PendingIntent {
            val intent = Intent(context, ActivityTransitionReceiver::class.java)
            return PendingIntent.getBroadcast(
                context,
                ActivityTransitionUsage.getCount(ActivityTransitionUsage.REQUEST_CODE),
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT
            )
        }
    }


}