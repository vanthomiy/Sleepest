package com.doitstudio.sleepest_master.ui.history

import android.app.Application
import android.view.View
import androidx.databinding.ObservableField
import androidx.lifecycle.AndroidViewModel

class HistoryViewModel(application: Application) : AndroidViewModel(application) {

    val activityPermissionDescription = ObservableField("View.GONE")

    fun onClick(view: View) {
        activityPermissionDescription.set("Hi")
    }
}