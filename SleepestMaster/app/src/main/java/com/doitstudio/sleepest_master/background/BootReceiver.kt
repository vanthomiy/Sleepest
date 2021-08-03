package com.doitstudio.sleepest_master.background

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import com.doitstudio.sleepest_master.MainApplication
import com.doitstudio.sleepest_master.googleapi.ActivityTransitionHandler
import com.doitstudio.sleepest_master.model.data.Actions
import com.doitstudio.sleepest_master.model.data.AlarmReceiverUsage
import com.doitstudio.sleepest_master.storage.DataStoreRepository
import com.doitstudio.sleepest_master.storage.DatabaseRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import java.time.LocalTime
import java.util.*

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