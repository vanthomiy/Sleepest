package com.sleepestapp.sleepest.background

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.sleepestapp.sleepest.MainApplication
import com.sleepestapp.sleepest.googleapi.ActivityTransitionHandler
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.launch
import java.util.*

/**
 * This receiver is called after a reboot of the device. Sometimes there are some battery optimization
 * settings necessary depending on different device models.
 */

class BootReceiver : BroadcastReceiver() {

    private val scope: CoroutineScope = MainScope()

    override fun onReceive(context: Context, intent: Intent?) {

        val dataStoreRepository = (context.applicationContext as MainApplication).dataStoreRepository

        //Check if boot is finished and in which state the alarm cycle was

        val action = intent!!.action

        if (action != null && action == Intent.ACTION_BOOT_COMPLETED) {

            BackgroundAlarmTimeHandler.getHandler(context).chooseStateBeforeReboot()

            scope.launch {
                if (dataStoreRepository.getActivitySubscribeStatus()) {
                    ActivityTransitionHandler.getHandler(context.applicationContext).startActivityHandler()
                }
            }
        }
    }
}