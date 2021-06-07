package com.doitstudio.Activityest_master.sleepapi

import android.Manifest
import android.annotation.SuppressLint
import android.app.PendingIntent
import android.content.Context
import android.content.pm.PackageManager
import androidx.core.content.ContextCompat
import com.doitstudio.Activityest_master.Activityapi.ActivityReciver
import com.doitstudio.sleepest_master.MainApplication
import com.doitstudio.sleepest_master.sleepapi.ActivityTransitionUtil
import com.google.android.gms.location.ActivityRecognition
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.launch

/**
 * Needs to be created with [getHandler] and then nothing much is to do...
 * From Background we need to update unsubscribe and subscribe Activity Data and the handler will sub/unsubscripe the Activity data recive
 */
class ActivityHandler(private val context: Context) {

    private val scope: CoroutineScope = MainScope()

    private var activityPendingIntent: PendingIntent = ActivityReciver.createActivityReceiverPendingIntent(context = context)

    private val dataStoreRepository by lazy { (context.applicationContext as MainApplication).dataStoreRepository }

    companion object {
        // For Singleton instantiation
        @Volatile
        private var INSTANCE: ActivityHandler? = null

        fun getHandler(context: Context): ActivityHandler {
            return INSTANCE ?: synchronized(this) {
                val instance = ActivityHandler(context)
                INSTANCE = instance
                // return instance
                instance
            }
        }
    }

    /**
     * Listens to Activity data subscribed or not and subscribe or unsubscribe from it automatically
     */
    fun startActivityHandler() {
        subscribeToActivitySegmentUpdates(context, activityPendingIntent)
    }

    fun stopActivityHandler(){
        unsubscribeToActivitySegmentUpdates(context, activityPendingIntent)
    }

    /**
     * Subscribes to Activity data.
     * Note: Permission isn't missing, it's in the manifest but only for 29+ version. The lint
     * check is the 28 and below version of the activity recognition permission (needed for
     * accessing Activity data).
     */
    @SuppressLint("MissingPermission")
    private fun subscribeToActivitySegmentUpdates(context: Context, pendingIntent: PendingIntent) {
        if (activityRecognitionPermissionApproved(context)) {

            val request = ActivityTransitionUtil.getActivityTransitionRequest()

            val task =
                    ActivityRecognition.getClient(context).requestActivityTransitionUpdates(
                            //1800000, // 1/2 stunden
                            request,
                            pendingIntent
                    )

            task.addOnSuccessListener {
                scope.launch {
                    dataStoreRepository.updateActivityIsSubscribed(true)
                    dataStoreRepository.updateActivitySubscribeFailed(false)
                }            }
            task.addOnFailureListener { exception ->
                scope.launch {
                    dataStoreRepository.updateActivityIsSubscribed(false)
                    dataStoreRepository.updateActivitySubscribeFailed(true)
                }
            }
        } else {
            scope.launch {
                dataStoreRepository.updateActivityPermissionRemovedError(true)
                dataStoreRepository.updateActivityPermissionActive(false)
            }
        }
    }

    /**
     * Unsubscribes to Activity data.
     */
    private fun unsubscribeToActivitySegmentUpdates(context: Context, pendingIntent: PendingIntent) {
        val task = ActivityRecognition.getClient(context).removeActivityTransitionUpdates(pendingIntent)

        task.addOnSuccessListener {
            scope.launch {
                dataStoreRepository.updateActivityIsSubscribed(false)
                dataStoreRepository.updateActivityUnsubscribeFailed(false)
            }
        }
        task.addOnFailureListener { exception ->
            scope.launch {
                dataStoreRepository.updateActivityUnsubscribeFailed(true)
            }
        }
    }

    private fun activityRecognitionPermissionApproved(context: Context): Boolean {
        // Because this app targets 29 and above (recommendation for using the Activity APIs), we
        // don't need to check if this is on a device before runtime permissions, that is, a device
        // prior to 29 / Q.
        return PackageManager.PERMISSION_GRANTED == ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.ACTIVITY_RECOGNITION
        )
    }
}