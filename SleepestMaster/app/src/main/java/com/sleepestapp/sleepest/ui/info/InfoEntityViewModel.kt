package com.sleepestapp.sleepest.ui.info

import android.app.Application
import android.util.LayoutDirection
import android.view.View
import android.widget.LinearLayout
import androidx.databinding.ObservableField
import androidx.lifecycle.AndroidViewModel


class InfoEntityViewModel(application: Application) : AndroidViewModel(application) {


    //val backgroundColor = ObservableField(R.color.transparent_overlay_tertiary)
    val textHeader = ObservableField("")
    val textDescription = ObservableField("")
    val orientation = ObservableField(LinearLayout.HORIZONTAL)
    val layoutFormat = ObservableField(LayoutDirection.LTR)

    val imageVisible = ObservableField(View.GONE)
    val lottieVisible = ObservableField(View.GONE)
    val headerVisible = ObservableField(View.GONE)
    val descrriptionVisible = ObservableField(View.GONE)

    //endregion

}


