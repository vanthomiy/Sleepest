package com.sleepestapp.sleepest.ui.history

import android.app.Application
import android.transition.TransitionManager
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.databinding.ObservableField
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.sleepestapp.sleepest.model.data.MobilePosition
import com.sleepestapp.sleepest.util.IconAnimatorUtil


class HistoryWeekViewModel() : ViewModel() {

    val actualExpand = MutableLiveData(-1)
    val goneState = MutableLiveData(View.GONE)
    val visibleState = MutableLiveData(View.VISIBLE)

    private var lastView: ImageView? = null
    fun onInfoClicked(view: View){
        updateInfoChanged(view.tag.toString())

        // Check if its an image view
        IconAnimatorUtil.animateView(view as ImageView)

        IconAnimatorUtil.resetView(lastView)

        lastView = if(lastView != view)
            view
        else
            null
    }

    private fun updateInfoChanged(value: String) {
        actualExpand.value = (if(actualExpand.value == value.toIntOrNull()) -1 else value.toIntOrNull())
    }
}