package com.sleepestapp.sleepest.ui.history

import android.view.View
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class HistoryMonthViewModel : ViewModel() {

    /**
     * Maintains the visibility of the information buttons and its text fields.
     */
    val actualExpand = MutableLiveData(-1)

    val goneState = MutableLiveData(View.GONE)

    val visibleState = MutableLiveData(View.VISIBLE)

    fun onInfoClicked(
        view: View
    ) {
        val value = view.tag.toString()
        actualExpand.value = if(actualExpand.value == value.toIntOrNull()) -1 else value.toIntOrNull()
    }
}