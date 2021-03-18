package com.doitstudio.sleepest_master.sleepapi

import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import com.doitstudio.sleepest_master.MainApplication
import com.doitstudio.sleepest_master.storage.DbRepository
import com.doitstudio.sleepest_master.storage.db.SleepApiRawDataEntity
import com.google.android.gms.location.SleepClassifyEvent
import com.google.android.gms.location.SleepSegmentEvent
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.launch
import java.util.Date.from

/**
 * Saves Sleep Events to Database.
 */
class SleepReceiver : BroadcastReceiver() {

    // Used to launch coroutines (non-blocking way to insert data).
    private val scope: CoroutineScope = MainScope()

    override fun onReceive(context: Context, intent: Intent) {
        Log.d(TAG, "onReceive(): $intent")

        val repository: DbRepository = (context.applicationContext as MainApplication).dbRepository

       if (SleepClassifyEvent.hasEvents(intent)) {
            val sleepClassifyEvents: List<SleepClassifyEvent> =
                    SleepClassifyEvent.extractEvents(intent)
            Log.d(TAG, "SleepClassifyEvent List: $sleepClassifyEvents")
            addSleepClassifyEventsToDatabase(repository, sleepClassifyEvents)
        }
    }

    private fun addSleepClassifyEventsToDatabase(
            repository: DbRepository,
            sleepClassifyEvents: List<SleepClassifyEvent>
    ) {
        if (sleepClassifyEvents.isNotEmpty()) {
            scope.launch {
                val convertedToEntityVersion: List<SleepApiRawDataEntity> =
                        sleepClassifyEvents.map {
                            SleepApiRawDataEntity.from(it)
                        }
                repository.insertSleepApiRawData(convertedToEntityVersion)
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
