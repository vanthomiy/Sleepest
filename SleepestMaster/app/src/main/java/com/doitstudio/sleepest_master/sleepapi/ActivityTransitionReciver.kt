package com.doitstudio.sleepest_master.sleepapi

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.widget.Toast
import com.doitstudio.sleepest_master.MainApplication
import com.doitstudio.sleepest_master.storage.db.ActivityApiRawDataEntity
import com.google.android.gms.location.ActivityTransitionResult
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.launch
import java.time.LocalDateTime
import java.time.ZoneOffset
import java.util.*

class ActivityTransitionReciver : BroadcastReceiver() {

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
                    Toast.makeText(context, "Neue Transition gefunden: " + it.transitionEvents.count(), Toast.LENGTH_LONG

                    ).show()
                    // update the amount of data that is beeing recived
                    dataStoreRepository.updateActivityApiValuesAmount(
                        it.transitionEvents.count()
                    )

                }
            }
        }
    }
}