package com.sleepestapp.sleepest.googleapi

import android.annotation.SuppressLint
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build
import com.sleepestapp.sleepest.MainApplication
import com.sleepestapp.sleepest.model.data.SleepApiUsage
import com.sleepestapp.sleepest.storage.DataStoreRepository
import com.sleepestapp.sleepest.storage.DatabaseRepository
import com.sleepestapp.sleepest.storage.db.SleepApiRawDataEntity
import com.google.android.gms.location.SleepClassifyEvent
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.launch

/**
 * Receives and saves sleep events to database.
 */
class SleepReceiver : BroadcastReceiver() {

    /**
     * [CoroutineScope] provides the ability to write and read from the database/datastore async
     */
    private val scope: CoroutineScope = MainScope()

    /**
     * Is called when new sleep data is available
     */
    override fun onReceive(context: Context, intent: Intent) {
        val repository = (context.applicationContext as MainApplication).dataBaseRepository
        val sleepCalculationStoreRepository = (context.applicationContext as MainApplication).dataStoreRepository

       if (SleepClassifyEvent.hasEvents(intent)) {
            val sleepClassifyEvents: List<SleepClassifyEvent> =
                    SleepClassifyEvent.extractEvents(intent)
            addSleepClassifyEventsToDatabase(repository,sleepCalculationStoreRepository, sleepClassifyEvents)
       }
    }
    /**
     * Add the data to the database
     */
    private fun addSleepClassifyEventsToDatabase(
        repository: DatabaseRepository,
        sleepCalculationStoreRepository: DataStoreRepository,
        sleepClassifyEvents: List<SleepClassifyEvent>
    ) {
        if (sleepClassifyEvents.isNotEmpty()) {
            scope.launch {
                val convertedToEntityVersion: List<SleepApiRawDataEntity> =
                        sleepClassifyEvents.map {
                            SleepApiRawDataEntity.from(it)
                        }
                // Update the raw sleep api data
                repository.insertSleepApiRawData(convertedToEntityVersion)
                // update the amount of data that is being received
                sleepCalculationStoreRepository.updateSleepSleepApiValuesAmount(convertedToEntityVersion.size)
            }
        }
    }

    companion object {
        /**
         * The actual intent for the subscription
         */
        //@SuppressLint("UnspecifiedImmutableFlag")
        fun createSleepReceiverPendingIntent(context: Context): PendingIntent {
            var flags = PendingIntent.FLAG_CANCEL_CURRENT
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                flags = flags or PendingIntent.FLAG_MUTABLE // <-- FLAG_IMMUTABLE breaks the code!
            }

            val sleepIntent = Intent(context, SleepReceiver::class.java)
            return PendingIntent.getBroadcast(
                    context,
                    SleepApiUsage.getCount(SleepApiUsage.REQUEST_CODE),
                    sleepIntent,
                flags
            )
        }
    }
}
