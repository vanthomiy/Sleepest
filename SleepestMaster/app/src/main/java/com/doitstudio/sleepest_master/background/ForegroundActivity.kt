package com.doitstudio.sleepest_master.background

import android.app.Activity
import android.os.Bundle
import android.widget.Toast
import com.doitstudio.sleepest_master.MainApplication
import com.doitstudio.sleepest_master.R
import com.doitstudio.sleepest_master.model.data.Actions
import com.doitstudio.sleepest_master.model.data.AlarmReceiverUsage
import com.doitstudio.sleepest_master.storage.DataStoreRepository
import com.doitstudio.sleepest_master.storage.DatabaseRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import java.util.*

class ForegroundActivity : Activity() {

    private val scope: CoroutineScope = MainScope()
    private val databaseRepository: DatabaseRepository by lazy {
        (applicationContext as MainApplication).dataBaseRepository
    }
    private val dataStoreRepository: DataStoreRepository by lazy {
        (applicationContext as MainApplication).dataStoreRepository
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_foreground)

        val extras = intent.extras

        if (extras != null) {
            when (extras.getInt("intent", 0)) {
                1 -> {
                    scope.launch {

                        // next alarm or null
                        if (databaseRepository.isAlarmActiv()) {
                            // start foreground if not null

                            if (!dataStoreRepository.backgroundServiceFlow.first().isForegroundActive) {
                                ForegroundService.startOrStopForegroundService(
                                    Actions.START,
                                    applicationContext
                                )
                            } else {
                                Toast.makeText(
                                    applicationContext,
                                    application.getString(R.string.error_start_foreground),
                                    Toast.LENGTH_LONG
                                ).show()
                            }

                        } else {
                            //Next foreground start at next day
                            val calendarAlarm = Calendar.getInstance()
                            calendarAlarm[Calendar.HOUR_OF_DAY] = 0
                            calendarAlarm[Calendar.MINUTE] = 0
                            calendarAlarm[Calendar.SECOND] = 0
                            calendarAlarm.add(
                                Calendar.SECOND,
                                dataStoreRepository.getSleepTimeBeginJob()
                            )
                            calendarAlarm.add(Calendar.DAY_OF_YEAR, 1)

                            //Start a alarm for the new foregroundservice start time
                            AlarmReceiver.startAlarmManager(
                                calendarAlarm[Calendar.DAY_OF_WEEK],
                                calendarAlarm[Calendar.HOUR_OF_DAY],
                                calendarAlarm[Calendar.MINUTE],
                                applicationContext,
                                AlarmReceiverUsage.START_FOREGROUND
                            )
                            val pref = getSharedPreferences("AlarmReceiver1", 0)
                            val ed = pref.edit()
                            ed.putString("usage", "ForegroundActivity")
                            ed.putInt("day", calendarAlarm[Calendar.DAY_OF_WEEK])
                            ed.putInt("hour", calendarAlarm[Calendar.HOUR_OF_DAY])
                            ed.putInt("minute", calendarAlarm[Calendar.MINUTE])
                            ed.apply()
                        }
                    }
                }

                2 -> {
                    ForegroundService.startOrStopForegroundService(Actions.STOP, applicationContext)
                }

                else -> {
                    //nothing should happen
                }
            }
        }

        /*if (extras != null) {
            val use = extras.getInt("intent", 0)
            if (use == 1) {

                // get the next alarm async
                scope.launch {

                    // next alarm or null
                    if(databaseRepository.isAlarmActiv()){
                        // start foreground if not null
                            /**TODO: Abfrage, ob Background schon aktive ist*/

                        if (!dataStoreRepository.backgroundServiceFlow.first().isForegroundActive) {
                            ForegroundService.startOrStopForegroundService(Actions.START, applicationContext)
                        }

                    } else {
                        //Next foreground start at next day
                        val calendarAlarm = AlarmReceiver.getAlarmDate(Calendar.getInstance()[Calendar.DAY_OF_WEEK] + 1, times.startForegroundHour, times.startForegroundMinute)
                        AlarmReceiver.startAlarmManager(calendarAlarm[Calendar.DAY_OF_WEEK], calendarAlarm[Calendar.HOUR_OF_DAY], calendarAlarm[Calendar.MINUTE], applicationContext, 1)
                    }
                }
            } else if (use == 2) {
                ForegroundService.startOrStopForegroundService(Actions.STOP, applicationContext)
            }
        }*/

        finish()

    }
}