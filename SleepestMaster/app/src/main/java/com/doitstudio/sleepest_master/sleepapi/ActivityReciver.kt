package com.doitstudio.Activityest_master.Activityapi

import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.doitstudio.sleepest_master.MainApplication
import com.doitstudio.sleepest_master.storage.DataStoreRepository
import com.doitstudio.sleepest_master.storage.DatabaseRepository
import com.doitstudio.sleepest_master.storage.db.ActivityApiRawDataEntity
import com.google.android.gms.location.ActivityRecognitionResult
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.launch

/**
 * Saves Activity Events to Database.
 */
class ActivityReciver : BroadcastReceiver() {

    // Used to launch coroutines (non-blocking way to insert data).
    private val scope: CoroutineScope = MainScope()

    override fun onReceive(context: Context, intent: Intent) {
        val repository = (context.applicationContext as MainApplication).dataBaseRepository
        val dataStoreRepository = (context.applicationContext as MainApplication).dataStoreRepository

        if (ActivityRecognitionResult.hasResult(intent)) {
            val activityRecognitionResult: ActivityRecognitionResult =
                    ActivityRecognitionResult.extractResult(intent)
            addActivityClassifyEventsToDatabase(repository,dataStoreRepository, activityRecognitionResult)
        }
    }

    private fun addActivityClassifyEventsToDatabase(
            repository: DatabaseRepository,
            ActivityCalculationStoreRepository: DataStoreRepository,
            ActivityClassifyEvents: ActivityRecognitionResult
    ) {

        scope.launch {
            val convertedToEntityVersion: ActivityApiRawDataEntity =
                    ActivityApiRawDataEntity(
                            ActivityClassifyEvents.time.toInt(),
                            ActivityClassifyEvents.mostProbableActivity.type
                    )

            // Update the raw Activity api data
            repository.insertActivityApiRawData(convertedToEntityVersion)

            // update the amount of data that is beeing recived
            ActivityCalculationStoreRepository.updateActivityApiValuesAmount(1)
        }


        /*if (ActivityClassifyEvents.isNotEmpty()) {
            scope.launch {
                val convertedToEntityVersion: List<ActivityApiRawDataEntity> =
                        ActivityClassifyEvents.map {
                            ActivityApiRawDataEntity(
                                    it.time.toInt(),
                                    it.mostProbableActivity.type
                            )
                        }
                // Update the raw Activity api data
                repository.insertActivityApiRawData(convertedToEntityVersion)
                // update the amount of data that is beeing recived
                ActivityCalculationStoreRepository.updateActivityApiValuesAmount(convertedToEntityVersion.size)
            }
        }*/
    }

    companion object {
        const val TAG = "ActivityReceiver"
        fun createActivityReceiverPendingIntent(context: Context): PendingIntent {
            val ActivityIntent = Intent(context, ActivityReciver::class.java)
            return PendingIntent.getBroadcast(
                    context,
                    0,
                    ActivityIntent,
                    PendingIntent.FLAG_CANCEL_CURRENT
            )
        }
    }
}

