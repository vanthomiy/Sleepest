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

class ForegroundActivity : Activity() {

    private val scope: CoroutineScope = MainScope()
    private val databaseRepository: DatabaseRepository by lazy {
        (applicationContext as MainApplication).dataBaseRepository
    }

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
                        // mach dein kak
                    }
                }
            } else if (use == 2) {
                ForegroundService.startOrStopForegroundService(Actions.STOP, applicationContext)
            }
        }
        finish()

    }
}