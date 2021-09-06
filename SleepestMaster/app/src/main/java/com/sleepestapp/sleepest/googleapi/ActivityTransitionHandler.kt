package com.sleepestapp.sleepest.googleapi

import android.annotation.SuppressLint
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import com.sleepestapp.sleepest.MainApplication
import com.sleepestapp.sleepest.model.data.ActivityTransitionUsage
import com.sleepestapp.sleepest.util.PermissionsUtil.isActivityRecognitionPermissionGranted
import com.google.android.gms.location.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.launch


/**
 * Needs to be created with [getHandler] and then nothing much is to do...
 * From Background we need to update unsubscribe and subscribe Activity Data and the handler will sub/unsubscripe the Activity data recive
 */
class ActivityTransitionHandler(private val context: Context) {

    /**
     * [CoroutineScope] provides the ability to write and read from the database/datastore async
     */
    private val scope: CoroutineScope = MainScope()

    /**
     * The actual datastore
     */
    private val dataStoreRepository by lazy { (context.applicationContext as MainApplication).dataStoreRepository }

    companion object {
        // For Singleton instantiation
        @Volatile
        private var INSTANCE: ActivityTransitionHandler? = null

        /**
         * Get a new or the actual instance of the [ActivityTransitionHandler]
         */
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
     * Subscribes to activity data and listens to it automatically
     */
    fun startActivityHandler() {
        subscribeToActivitySegmentUpdates()
    }

    /**
     * Unsubscribes from the activity data
     */
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
        if(isActivityRecognitionPermissionGranted(context)){

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

    /**
     * The actual intent for the subscription
     */
    private fun getPendingIntent(): PendingIntent {
        val intent = Intent(context, ActivityTransitionReciver::class.java)
        return PendingIntent.getBroadcast(
                context,
                ActivityTransitionUsage.getCount(ActivityTransitionUsage.REQUEST_CODE),
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT
        )
    }
}