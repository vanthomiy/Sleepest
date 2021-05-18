package com.doitstudio.sleepest_master.background

import android.app.Activity
import android.os.Bundle
import com.doitstudio.sleepest_master.MainApplication
import com.doitstudio.sleepest_master.R
import com.doitstudio.sleepest_master.model.data.Actions
import com.doitstudio.sleepest_master.storage.DbRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

class ForegroundActivity : Activity() {

    private val scope: CoroutineScope = MainScope()
    private val dbRepository: DbRepository by lazy {
        (applicationContext as MainApplication).dbRepository
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
                    val alarm = dbRepository.getNextActiveAlarm()

                    if(alarm != null){
                        // start foreground if not null
                        ForegroundService.startOrStopForegroundService(Actions.START, applicationContext)
                    }
                }
            } else if (use == 2) {
                ForegroundService.startOrStopForegroundService(Actions.STOP, applicationContext)
            }
        }
        finish()

    }
}