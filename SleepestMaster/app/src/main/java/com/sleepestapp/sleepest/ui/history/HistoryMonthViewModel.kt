package com.sleepestapp.sleepest.ui.history

import android.app.Application
import android.transition.TransitionManager
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.databinding.ObservableField
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.ViewModel
import com.sleepestapp.sleepest.model.data.MobilePosition
import com.sleepestapp.sleepest.util.IconAnimatorUtil


class HistoryMonthViewModel() : ViewModel() {

    val actualExpand = ObservableField(-1)
    val goneState = ObservableField(View.GONE)
    val visibleState = ObservableField(View.VISIBLE)

    lateinit var transitionsContainer : ViewGroup

    init {

    }

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
        TransitionManager.beginDelayedTransition(transitionsContainer)
        actualExpand.set(if(actualExpand.get() == value.toIntOrNull()) -1 else value.toIntOrNull())
    }
}