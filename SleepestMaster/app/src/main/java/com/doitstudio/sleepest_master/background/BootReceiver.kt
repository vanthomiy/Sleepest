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

        val dataBaseRepository = (context.applicationContext as MainApplication).dataBaseRepository
        val dataStoreRepository = (context.applicationContext as MainApplication).dataStoreRepository

        //Check if boot is finished and foreground service was started before reboot

        val action = intent!!.action

        if (action != null) {
            if (action == Intent.ACTION_BOOT_COMPLETED) {

                BackgroundAlarmTimeHandler.getHandler(context).chooseStateBeforeReboot()

                scope.launch {
                    if (dataStoreRepository.getActivitySubscribeStatus()) {
                        ActivityTransitionHandler.getHandler(context.applicationContext).startActivityHandler()
                    }
                }

                    /*if(dataStoreRepository.backgroundServiceFlow.first().isForegroundActive && dataBaseRepository.getNextActiveAlarm() != null) {
                        if (!dataBaseRepository.getNextActiveAlarm()!!.wasFired && !dataBaseRepository.getNextActiveAlarm()!!.tempDisabled && ((LocalTime.now().toSecondOfDay() < dataBaseRepository.getNextActiveAlarm()!!.actualWakeup) ||
                                    (dataStoreRepository.getSleepTimeBegin() < LocalTime.now().toSecondOfDay())) && dataStoreRepository.isInSleepTime() && (dataBaseRepository.getNextActiveAlarm() != null)) {
                            ForegroundService.startOrStopForegroundService(Actions.START, context.applicationContext)
                            val calendarFirstCalc = AlarmReceiver.getAlarmDate(dataBaseRepository.getNextActiveAlarm()!!.wakeupEarly - 1800)
                            AlarmReceiver.startAlarmManager(calendarFirstCalc[Calendar.DAY_OF_WEEK], calendarFirstCalc[Calendar.HOUR_OF_DAY], calendarFirstCalc[Calendar.MINUTE], context.applicationContext,AlarmReceiverUsage.START_WORKMANAGER_CALCULATION)
                        } else {
                            if (dataStoreRepository.getSleepTimeBegin() > LocalTime.now().toSecondOfDay() && dataStoreRepository.getSleepTimeEnd() < LocalTime.now().toSecondOfDay()) {
                                val calendar = AlarmReceiver.getAlarmDate(dataStoreRepository.getSleepTimeBegin())
                                AlarmReceiver.startAlarmManager(calendar[Calendar.DAY_OF_WEEK], calendar[Calendar.HOUR_OF_DAY], calendar[Calendar.MINUTE], context.applicationContext,
                                    AlarmReceiverUsage.START_FOREGROUND)
                                AlarmReceiver.startAlarmManager(calendar[Calendar.DAY_OF_WEEK], calendar[Calendar.HOUR_OF_DAY], calendar[Calendar.MINUTE], context.applicationContext,
                                    AlarmReceiverUsage.START_WORKMANAGER)
                                dataStoreRepository.backgroundUpdateIsActive(false)
                            }
                        }
                    } else if (!dataStoreRepository.backgroundServiceFlow.first().isForegroundActive && dataBaseRepository.getNextActiveAlarm() != null) {
                        if (!dataBaseRepository.getNextActiveAlarm()!!.wasFired && !dataBaseRepository.getNextActiveAlarm()!!.tempDisabled && ((LocalTime.now().toSecondOfDay() < dataBaseRepository.getNextActiveAlarm()!!.actualWakeup) ||
                                    (dataStoreRepository.getSleepTimeBegin() < LocalTime.now().toSecondOfDay())) && dataStoreRepository.isInSleepTime() && (dataBaseRepository.getNextActiveAlarm() != null)) {
                            ForegroundService.startOrStopForegroundService(Actions.START, context.applicationContext)
                            val calendarFirstCalc = AlarmReceiver.getAlarmDate(dataBaseRepository.getNextActiveAlarm()!!.wakeupEarly - 1800)
                            AlarmReceiver.startAlarmManager(calendarFirstCalc[Calendar.DAY_OF_WEEK], calendarFirstCalc[Calendar.HOUR_OF_DAY], calendarFirstCalc[Calendar.MINUTE], context.applicationContext,AlarmReceiverUsage.START_WORKMANAGER_CALCULATION)
                            dataStoreRepository.backgroundUpdateIsActive(true)
                        } else {
                            if (dataStoreRepository.getSleepTimeBegin() > LocalTime.now()
                                    .toSecondOfDay() && dataStoreRepository.getSleepTimeEnd() < LocalTime.now()
                                    .toSecondOfDay()
                            ) {
                                val calendar =
                                    AlarmReceiver.getAlarmDate(dataStoreRepository.getSleepTimeBegin())
                                AlarmReceiver.startAlarmManager(
                                    calendar[Calendar.DAY_OF_WEEK],
                                    calendar[Calendar.HOUR_OF_DAY],
                                    calendar[Calendar.MINUTE],
                                    context.applicationContext,
                                    AlarmReceiverUsage.START_FOREGROUND
                                )
                                AlarmReceiver.startAlarmManager(
                                    calendar[Calendar.DAY_OF_WEEK],
                                    calendar[Calendar.HOUR_OF_DAY],
                                    calendar[Calendar.MINUTE],
                                    context.applicationContext,
                                    AlarmReceiverUsage.START_WORKMANAGER
                                )
                            }
                        }

                    } else {
                        val calendar = AlarmReceiver.getAlarmDate(dataStoreRepository.getSleepTimeBegin())
                        AlarmReceiver.startAlarmManager(calendar[Calendar.DAY_OF_WEEK], calendar[Calendar.HOUR_OF_DAY], calendar[Calendar.MINUTE], context.applicationContext,
                            AlarmReceiverUsage.START_WORKMANAGER)
                        dataStoreRepository.backgroundUpdateIsActive(false)
                    }*/
                }

        }
    }
}