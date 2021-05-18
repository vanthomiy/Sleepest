package com.doitstudio.sleepest_master.background

import android.app.Activity
import android.os.Bundle
import com.doitstudio.sleepest_master.MainApplication
import com.doitstudio.sleepest_master.R
import com.doitstudio.sleepest_master.model.data.Actions
import com.doitstudio.sleepest_master.storage.DatabaseRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.launch
import java.util.*

class ForegroundActivity : Activity() {

    private val scope: CoroutineScope = MainScope()
    private val databaseRepository: DatabaseRepository by lazy {
        (applicationContext as MainApplication).dataBaseRepository
    }
    private val times : Times = Times()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_foreground)

        val extras = intent.extras

        if (extras != null) {
            val use = extras.getInt("intent", 0)
            if (use == 1) {

                /**TODO: Abfrage, ob Alarm da is. Wenn ja, startForeground, wenn nicht, AlarmReceiver für 20:00 nächster Tag einstellen*/

                // get the next alarm async
                scope.launch {

                    // next alarm or null
                    if(databaseRepository.isAlarmActiv()){
                        // start foreground if not null
                        ForegroundService.startOrStopForegroundService(Actions.START, applicationContext)
                    }else{
                        //Next foreground start next day
                        val calendarAlarm = AlarmReceiver.getAlarmDate(Calendar.getInstance()[Calendar.DAY_OF_WEEK] + 1, times.startForegroundHour, times.startForegroundMinute)
                        AlarmReceiver.startAlarmManager(calendarAlarm[Calendar.DAY_OF_WEEK], calendarAlarm[Calendar.HOUR_OF_DAY], calendarAlarm[Calendar.MINUTE], applicationContext, 1)
                    }
                }
            } else if (use == 2) {
                ForegroundService.startOrStopForegroundService(Actions.STOP, applicationContext)
            }
        }
        finish()

    }
}