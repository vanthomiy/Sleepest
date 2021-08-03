package com.doitstudio.sleepest_master.googleapi

import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.doitstudio.sleepest_master.MainApplication
import com.doitstudio.sleepest_master.storage.DataStoreRepository
import com.doitstudio.sleepest_master.storage.DatabaseRepository
import com.doitstudio.sleepest_master.storage.db.SleepApiRawDataEntity
import com.google.android.gms.location.SleepClassifyEvent
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.launch

/**
 * Saves Sleep Events to Database.
 */
class SleepReceiver : BroadcastReceiver() {

    // Used to launch coroutines (non-blocking way to insert data).
    private val scope: CoroutineScope = MainScope()

    override fun onReceive(context: Context, intent: Intent) {
        val repository = (context.applicationContext as MainApplication).dataBaseRepository
        val sleepCalculationStoreRepository = (context.applicationContext as MainApplication).dataStoreRepository

       if (SleepClassifyEvent.hasEvents(intent)) {
            val sleepClassifyEvents: List<SleepClassifyEvent> =
                    SleepClassifyEvent.extractEvents(intent)
            addSleepClassifyEventsToDatabase(repository,sleepCalculationStoreRepository, sleepClassifyEvents)
       }
    }

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
                // update the amount of data that is beeing recived
                sleepCalculationStoreRepository.updateSleepSleepApiValuesAmount(convertedToEntityVersion.size)
            }
        }
    }

    companion object {
        const val TAG = "SleepReceiver"
        fun createSleepReceiverPendingIntent(context: Context): PendingIntent {
            val sleepIntent = Intent(context, SleepReceiver::class.java)
            return PendingIntent.getBroadcast(
                    context,
                    0,
                    sleepIntent,
                    PendingIntent.FLAG_CANCEL_CURRENT
            )
        }
    }
}
