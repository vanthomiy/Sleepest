package com.doitstudio.sleepest_master.sleepapi

import android.Manifest
import android.annotation.SuppressLint
import android.app.PendingIntent
import android.content.Context
import android.content.pm.PackageManager
import android.util.Log
import androidx.core.content.ContextCompat
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.asLiveData
import com.doitstudio.sleepest_master.MainApplication
import com.doitstudio.sleepest_master.sleepcalculation.SleepCalculationHandler
import com.doitstudio.sleepest_master.storage.DataStoreRepository
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

    private val scope: CoroutineScope = MainScope()

    private var sleepPendingIntent: PendingIntent = SleepReceiver.createSleepReceiverPendingIntent(context = context)

    private val repository by lazy { (context.applicationContext as MainApplication).dataStoreRepository }

    companion object {
        // For Singleton instantiation
        @Volatile
        private var INSTANCE: SleepHandler? = null

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
     * Listens to sleep data subscribed or not and subscribe or unsubscribe from it automatically
     */
    fun startSleepHandler() {
        subscribeToSleepSegmentUpdates(context, sleepPendingIntent)
    }

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
        if (activityRecognitionPermissionApproved(context)) {
            val task =
                    ActivityRecognition.getClient(context).requestSleepSegmentUpdates(
                            pendingIntent,
                            SleepSegmentRequest(SleepSegmentRequest.CLASSIFY_EVENTS_ONLY)
                    )

            task.addOnSuccessListener {
                scope.launch {
                    repository.updateIsSubscribed(true)
                    repository.updateSubscribeFailed(false)
                }            }
            task.addOnFailureListener { exception ->
                scope.launch {
                    repository.updateIsSubscribed(false)
                    repository.updateSubscribeFailed(true)
                }
            }
        } else {
            scope.launch {
                repository.updateSubscribeToSleepData(false)
                repository.updatePermissionRemovedError(true)
                repository.updatePermissionActive(false)
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
                repository.updateIsSubscribed(false)
                repository.updateUnsubscribeFailed(false)
            }
        }
        task.addOnFailureListener { exception ->
            scope.launch {
                repository.updateUnsubscribeFailed(true)
            }
        }
    }

    private fun activityRecognitionPermissionApproved(context: Context): Boolean {
        // Because this app targets 29 and above (recommendation for using the Sleep APIs), we
        // don't need to check if this is on a device before runtime permissions, that is, a device
        // prior to 29 / Q.
        return PackageManager.PERMISSION_GRANTED == ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.ACTIVITY_RECOGNITION
        )
    }

}