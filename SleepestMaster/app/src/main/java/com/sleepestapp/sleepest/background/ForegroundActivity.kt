package com.sleepestapp.sleepest.background

import android.app.Activity
import android.os.Bundle
import android.widget.Toast
import com.sleepestapp.sleepest.MainApplication
import com.sleepestapp.sleepest.R
import com.sleepestapp.sleepest.model.data.Actions
import com.sleepestapp.sleepest.model.data.AlarmReceiverUsage
import com.sleepestapp.sleepest.storage.DataStoreRepository
import com.sleepestapp.sleepest.storage.DatabaseRepository
import com.sleepestapp.sleepest.util.TimeConverterUtil
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import java.util.*

/**
 * This Activity is a workaround. If the app is not opened and only in the background, the foreground service
 * was not started or stopped correctly. With the help of this overlay invisible Activity it starts and
 * stops the service correctly.
 */

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

                        }
                    }
                }

                2 -> {
                    //Stops the foreground service2
                    ForegroundService.startOrStopForegroundService(Actions.STOP, applicationContext)
                    val calendarAlarm = TimeConverterUtil.getAlarmDate(dataStoreRepository.getSleepTimeBeginJob())
                    AlarmReceiver.startAlarmManager(
                        calendarAlarm[Calendar.DAY_OF_WEEK],
                        calendarAlarm[Calendar.HOUR_OF_DAY],
                        calendarAlarm[Calendar.MINUTE], applicationContext, AlarmReceiverUsage.START_FOREGROUND
                    )

                }

                else -> {
                    //nothing should happen
                }
            }
        }

        //End Activity
        finish()

    }



}