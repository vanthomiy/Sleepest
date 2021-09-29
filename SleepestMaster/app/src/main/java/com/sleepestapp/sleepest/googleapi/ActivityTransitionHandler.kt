package com.sleepestapp.sleepest.googleapi

import android.annotation.SuppressLint
import android.app.PendingIntent
import android.content.Context
import com.sleepestapp.sleepest.MainApplication
import com.sleepestapp.sleepest.util.PermissionsUtil.isActivityRecognitionPermissionGranted
import com.google.android.gms.location.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.launch


/*** From Background we need to update unsubscribe and subscribe Activity Data and the handler will sub/unsubscribe the Activity data receive
 */
class ActivityTransitionHandler(private val context: Context) {

    /**
     * [CoroutineScope] provides the ability to write and read from the database/datastore async
     */
    private val scope: CoroutineScope = MainScope()

    private var activityTransitionPendingIntent: PendingIntent = ActivityTransitionReceiver.createActivityTransitionReceiverPendingIntent(context = context)

    /**
     * The actual datastore
     */
    private val dataStoreRepository by lazy { (context.applicationContext as MainApplication).dataStoreRepository }

    /**
     * Subscribes to activity data and listens to it automatically
     */
    fun startActivityHandler() {
        subscribeToActivitySegmentUpdates(context, activityTransitionPendingIntent)
    }

    /**
     * Unsubscribes from the activity data
     */
    fun stopActivityHandler(){
        unsubscribeToActivitySegmentUpdates(context, activityTransitionPendingIntent)
    }


    /**
     * Subscribes to Activity data.
     * Note: Permission isn't missing, it's in the manifest but only for 29+ version. The lint
     * check is the 28 and below version of the activity recognition permission (needed for
     * accessing Activity data).
     */
    @SuppressLint("MissingPermission")
    private fun subscribeToActivitySegmentUpdates(context: Context, pendingIntent: PendingIntent) {
        if(isActivityRecognitionPermissionGranted(context)){

            val request = ActivityTransitionUtil.getActivityTransitionRequest()

            // myPendingIntent is the instance of PendingIntent where the app receives callbacks.
            val task = ActivityRecognition.getClient(context)
                    .requestActivityTransitionUpdates(request, pendingIntent)

            task.addOnSuccessListener {
                scope.launch {
                    dataStoreRepository.updateActivityIsSubscribed(true)
                    dataStoreRepository.updateActivitySubscribeFailed(false)
                }
            }

            task.addOnFailureListener {
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
     * Unsubscribe to Activity data.
     */
    private fun unsubscribeToActivitySegmentUpdates(context: Context, pendingIntent: PendingIntent) {

        val task = ActivityRecognition.getClient(context)
                .removeActivityTransitionUpdates(pendingIntent)

        task.addOnSuccessListener {
            scope.launch {
                dataStoreRepository.updateActivityIsSubscribed(false)
                dataStoreRepository.updateActivityUnsubscribeFailed(false)
            }
        }

        task.addOnFailureListener {
            scope.launch {
                dataStoreRepository.updateActivityUnsubscribeFailed(true)

            }
        }
    }

}