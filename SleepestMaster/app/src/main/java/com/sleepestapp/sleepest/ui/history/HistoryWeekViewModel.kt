package com.sleepestapp.sleepest.ui.history

import android.app.Application
import android.transition.TransitionManager
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.databinding.ObservableField
import androidx.lifecycle.AndroidViewModel
import com.sleepestapp.sleepest.util.IconAnimatorUtil


class HistoryWeekViewModel(application: Application) : AndroidViewModel(application) {

    val actualExpand = ObservableField(-1)
    val goneState = ObservableField(View.GONE)
    val visibleState = ObservableField(View.VISIBLE)

    lateinit var transitionsContainer : ViewGroup

    private var lastView: ImageView? = null
    fun onInfoClicked(view: View){
        updateInfoChanged(view.tag.toString(), true)

        // Check if its an image view
        IconAnimatorUtil.animateView(view as ImageView)

        IconAnimatorUtil.resetView(lastView)

        lastView = if(lastView != view)
            view
        else
            null
    }

    @Suppress("UNUSED_PARAMETER")
    private fun updateInfoChanged(value: String, toggle: Boolean = false) {
        TransitionManager.beginDelayedTransition(transitionsContainer)
        actualExpand.set(if(actualExpand.get() == value.toIntOrNull()) -1 else value.toIntOrNull())
    }
}