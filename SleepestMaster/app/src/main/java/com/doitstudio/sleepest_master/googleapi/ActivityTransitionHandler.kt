package com.doitstudio.sleepest_master.googleapi

import android.Manifest
import android.annotation.SuppressLint
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import androidx.core.content.ContextCompat
import com.doitstudio.sleepest_master.googleapi.ActivityTransitionHandler.Companion.getHandler
import com.doitstudio.sleepest_master.MainApplication
import com.doitstudio.sleepest_master.model.data.ActivityTransitionUsage
import com.google.android.gms.location.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.launch


/**
 * Needs to be created with [getHandler] and then nothing much is to do...
 * From Background we need to update unsubscribe and subscribe Activity Data and the handler will sub/unsubscripe the Activity data recive
 */
class ActivityTransitionHandler(private val context: Context) {

    private val scope: CoroutineScope = MainScope()

    private val dataStoreRepository by lazy { (context.applicationContext as MainApplication).dataStoreRepository }

    companion object {
        // For Singleton instantiation
        @Volatile
        private var INSTANCE: ActivityTransitionHandler? = null

        fun getHandler(context: Context): ActivityTransitionHandler {
            return INSTANCE ?: synchronized(this) {
                val instance = ActivityTransitionHandler(context)
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
        subscribeToActivitySegmentUpdates()
    }

    fun stopActivityHandler(){
        unsubscribeToActivitySegmentUpdates()
    }


    /**
     * Subscribes to Activity data.
     * Note: Permission isn't missing, it's in the manifest but only for 29+ version. The lint
     * check is the 28 and below version of the activity recognition permission (needed for
     * accessing Activity data).
     */
    @SuppressLint("MissingPermission")
    private fun subscribeToActivitySegmentUpdates() {
        if(activityRecognitionPermissionApproved(context)){

            val request = ActivityTransitionUtil.getActivityTransitionRequest()

            // myPendingIntent is the instance of PendingIntent where the app receives callbacks.
            val task = ActivityRecognition.getClient(context)
                    .requestActivityTransitionUpdates(request, getPendingIntent())

            task.addOnSuccessListener {
                scope.launch {
                    dataStoreRepository.updateActivityIsSubscribed(true)
                    dataStoreRepository.updateActivitySubscribeFailed(false)
                }                 }

            task.addOnFailureListener { e: Exception ->
                scope.launch {
                    dataStoreRepository.updateActivityPermissionRemovedError(true)
                    dataStoreRepository.updateActivityPermissionActive(false)
                }            }

        }
    }

    /**
     * Unsubscribe to Activity data.
     */
    private fun unsubscribeToActivitySegmentUpdates() {
        val request = ActivityTransitionUtil.getActivityTransitionRequest()

        val task = ActivityRecognition.getClient(context)
                .requestActivityTransitionUpdates(request, getPendingIntent())

        task.addOnSuccessListener {
            scope.launch {
                dataStoreRepository.updateActivityIsSubscribed(false)
                dataStoreRepository.updateActivityUnsubscribeFailed(false)
            }        }

        task.addOnFailureListener { e: Exception ->
            scope.launch {
                dataStoreRepository.updateActivityUnsubscribeFailed(true)

            }        }
    }


    private fun getPendingIntent(): PendingIntent {
        val intent = Intent(context, ActivityTransitionReciver::class.java)
        return PendingIntent.getBroadcast(
                context,
                ActivityTransitionUsage.getCount(ActivityTransitionUsage.REQUEST_CODE),
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT
        )
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