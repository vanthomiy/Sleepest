package com.sleepestapp.sleepest.util

import android.view.View
import android.view.ViewGroup
import androidx.databinding.BindingAdapter

class CostumBinding {
    companion object {
        @JvmStatic @BindingAdapter("layoutMarginBottom")
        fun setLayoutMarginBottom(view: View, dimen: Float) {
            val layoutParams = view.layoutParams as ViewGroup.MarginLayoutParams
            layoutParams.bottomMargin = dimen.toInt()
            view.layoutParams = layoutParams
        }
    }
}