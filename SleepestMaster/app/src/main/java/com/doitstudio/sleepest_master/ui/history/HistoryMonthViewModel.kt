package com.doitstudio.sleepest_master.ui.history

import android.app.Application
import android.transition.TransitionManager
import android.view.View
import android.view.ViewGroup
import androidx.databinding.ObservableField
import androidx.lifecycle.AndroidViewModel


class HistoryMonthViewModel(application: Application) : AndroidViewModel(application) {

    val actualExpand = ObservableField(-1)
    val goneState = ObservableField(View.GONE)
    val visibleState = ObservableField(View.VISIBLE)

    lateinit var transitionsContainer : ViewGroup

    init {

    }

    fun onInfoClicked(view: View){
        updateInfoChanged(view.tag.toString(), true)
    }

    private fun updateInfoChanged(value: String, toggle: Boolean = false) {
        TransitionManager.beginDelayedTransition(transitionsContainer)
        actualExpand.set(if(actualExpand.get() == value.toIntOrNull()) -1 else value.toIntOrNull())
    }
}