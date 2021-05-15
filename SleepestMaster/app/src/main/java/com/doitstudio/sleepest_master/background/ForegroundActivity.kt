package com.doitstudio.sleepest_master.background

import android.app.Activity
import android.os.Bundle
import com.doitstudio.sleepest_master.R
import com.doitstudio.sleepest_master.model.data.Actions

class ForegroundActivity : Activity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_foreground)

        val extras = intent.extras

        if (extras != null) {
            val use = extras.getInt("intent", 0)
            if (use == 1) {

                /**TODO: Abfrage, ob Alarm da is. Wenn ja, startForeground, wenn nicht, AlarmReceiver für 20:00 nächster Tag einstellen*/

                ForegroundService.startOrStopForegroundService(Actions.START, applicationContext)
            } else if (use == 2) {
                ForegroundService.startOrStopForegroundService(Actions.STOP, applicationContext)
            }
        }
        finish()

    }
}