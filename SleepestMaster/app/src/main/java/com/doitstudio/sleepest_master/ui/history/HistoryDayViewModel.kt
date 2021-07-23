package com.doitstudio.sleepest_master.ui.history

import android.app.Application
import android.content.Context
import androidx.databinding.ObservableField
import androidx.lifecycle.AndroidViewModel

class HistoryDayViewModel(application: Application) : AndroidViewModel(application) {
    var beginOfSleep = ObservableField("22:00")
    var endOfSeep = ObservableField("06:00")

    val context: Context by lazy { getApplication<Application>().applicationContext }

    init {

    }
}