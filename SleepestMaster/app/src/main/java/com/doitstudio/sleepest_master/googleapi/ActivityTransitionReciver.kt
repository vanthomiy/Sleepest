package com.doitstudio.sleepest_master.googleapi

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
 * Receives and saves activity events to database.
 */
class ActivityTransitionReciver : BroadcastReceiver() {


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
                                LocalDateTime.now().toEpochSecond(ZoneOffset.UTC).toInt(),
                                transition.activityType,
                                transition.transitionType
                            )
                        )

                    }

                    // Update the raw Activity api data
                    repository.insertActivityApiRawData(convertedToEntityVersion)

                    // update the amount of data that is beeing recived
                    dataStoreRepository.updateActivityApiValuesAmount(
                        it.transitionEvents.count()
                    )

                }
            }
        }
    }
}