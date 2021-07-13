package com.doitstudio.sleepest_master.ui.history

import android.app.Application
import android.view.View
import androidx.databinding.ObservableField
import androidx.lifecycle.AndroidViewModel
import com.doitstudio.sleepest_master.storage.DatabaseRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.MainScope

class HistoryViewModel(application: Application) : AndroidViewModel(application) {
    /** TODO Description */
    private lateinit var sleepDbRepository: DatabaseRepository

    /** Used for access suspend functions and database. */
    private val scope: CoroutineScope = MainScope()

    val activityPermissionDescription = ObservableField("View.GONE")

    fun onClick(view: View) {
        activityPermissionDescription.set("Hi")
    }
}