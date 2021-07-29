package com.doitstudio.sleepest_master.ui.history

import android.app.Application
import android.content.Context
import androidx.databinding.ObservableField
import androidx.lifecycle.AndroidViewModel

class HistoryDayViewModel(application: Application) : AndroidViewModel(application) {
    var beginOfSleep = ObservableField("22:00")
    var endOfSeep = ObservableField("06:00")
    var awakeTime = ObservableField("Awake: 1 hour 30 minutes")
    var lightSleepTime = ObservableField(" Light: 1 hour 30 minutes")
    var deepSleepTime = ObservableField("Deep: 1 hour 30 minutes")
    var sleepTime = ObservableField("Sleep: 1 hour 30 minutes")
    var activitySmiley = ObservableField("Activity")

    val context: Context by lazy { getApplication<Application>().applicationContext }

    init {

    }
}