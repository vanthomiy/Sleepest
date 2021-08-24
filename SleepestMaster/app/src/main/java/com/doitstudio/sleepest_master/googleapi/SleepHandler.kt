package com.doitstudio.sleepest_master.googleapi

import android.Manifest
import android.annotation.SuppressLint
import android.app.PendingIntent
import android.content.Context
import android.content.pm.PackageManager
import androidx.core.content.ContextCompat
import com.doitstudio.sleepest_master.MainApplication
import com.doitstudio.sleepest_master.util.PermissionsUtil
import com.google.android.gms.location.ActivityRecognition
import com.google.android.gms.location.SleepSegmentRequest
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.launch

/**
 * Needs to be created with [getHandler] and then nothing much is to do...
 * From Background we need to update unsubscribe and subscribe Sleep Data and the handler will sub/unsubscripe the sleep data recive
 */
class SleepHandler(private val context: Context) {

    /**
     * [CoroutineScope] provides the ability to write and read from the database/datastore async
     */
    private val scope: CoroutineScope = MainScope()

    private var sleepPendingIntent: PendingIntent = SleepReceiver.createSleepReceiverPendingIntent(context = context)

    /**
     * The actual datastore
     */
    private val dataStoreRepository by lazy { (context.applicationContext as MainApplication).dataStoreRepository }

    companion object {
        // For Singleton instantiation
        @Volatile
        private var INSTANCE: SleepHandler? = null

        /**
         * Get a new or the actual instance of the [SleepHandler]
         */
        fun getHandler(context: Context): SleepHandler {
            return INSTANCE ?: synchronized(this) {
                val instance = SleepHandler(context)
                INSTANCE = instance
                // return instance
                instance
            }
        }
    }

    /**
     * Subscribes to sleep api data and listens to it automatically
     */
    fun startSleepHandler() {
        subscribeToSleepSegmentUpdates(context, sleepPendingIntent)
    }

    /**
     * Unsubscribes from the sleep api data
     */
    fun stopSleepHandler(){
        unsubscribeToSleepSegmentUpdates(context, sleepPendingIntent)
    }

    /**
     * Subscribes to sleep data.
     * Note: Permission isn't missing, it's in the manifest but only for 29+ version. The lint
     * check is the 28 and below version of the activity recognition permission (needed for
     * accessing sleep data).
     */
    @SuppressLint("MissingPermission")
    private fun subscribeToSleepSegmentUpdates(context: Context, pendingIntent: PendingIntent) {
        if (PermissionsUtil.isActivityRecognitionPermissionGranted(context)) {
            val task =
                    ActivityRecognition.getClient(context).requestSleepSegmentUpdates(
                            pendingIntent,
                            SleepSegmentRequest(SleepSegmentRequest.CLASSIFY_EVENTS_ONLY)
                    )

            task.addOnSuccessListener {
                scope.launch {
                    dataStoreRepository.updateSleepIsSubscribed(true)
                    dataStoreRepository.updateSleepSubscribeFailed(false)
                }            }
            task.addOnFailureListener { exception ->
                scope.launch {
                    dataStoreRepository.updateSleepIsSubscribed(false)
                    dataStoreRepository.updateSleepSubscribeFailed(true)
                }
            }
        } else {
            scope.launch {
                dataStoreRepository.updateSleepPermissionRemovedError(true)
                dataStoreRepository.updateSleepPermissionActive(false)
            }
        }
    }

    /**
     * Unsubscribes to sleep data.
     */
    private fun unsubscribeToSleepSegmentUpdates(context: Context, pendingIntent: PendingIntent) {
        val task = ActivityRecognition.getClient(context).removeSleepSegmentUpdates(pendingIntent)

        task.addOnSuccessListener {
            scope.launch {
                dataStoreRepository.updateSleepIsSubscribed(false)
                dataStoreRepository.updateSleepUnsubscribeFailed(false)
            }
        }
        task.addOnFailureListener { exception ->
            scope.launch {
                dataStoreRepository.updateSleepUnsubscribeFailed(true)
            }
        }
    }

}

