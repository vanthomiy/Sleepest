package com.sleepestapp.sleepest.ui.info

import android.app.Application
import android.util.LayoutDirection
import android.view.View
import android.widget.LinearLayout
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel


class InfoEntityViewModel : ViewModel() {


    //region Binding Values

    val textHeader = MutableLiveData("")
    val textDescription = MutableLiveData("")
    val orientation = MutableLiveData(LinearLayout.HORIZONTAL)
    val layoutFormat = MutableLiveData(LayoutDirection.LTR)

    val imageVisible = MutableLiveData(View.GONE)
    val lottieVisible = MutableLiveData(View.GONE)
    val headerVisible = MutableLiveData(View.GONE)
    val descrriptionVisible = MutableLiveData(View.GONE)

    //endregion

}


