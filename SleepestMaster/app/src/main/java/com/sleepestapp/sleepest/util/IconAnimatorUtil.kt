package com.sleepestapp.sleepest.util

import android.content.Context
import android.content.res.Configuration
import android.content.res.Configuration.UI_MODE_NIGHT_YES
import android.graphics.drawable.AnimatedVectorDrawable
import android.widget.ImageView
import androidx.vectordrawable.graphics.drawable.AnimatedVectorDrawableCompat

object IconAnimatorUtil {

    fun Context.isDarkThemeOn(): Boolean {
        return resources.configuration.uiMode and
                Configuration.UI_MODE_NIGHT_MASK == UI_MODE_NIGHT_YES
    }

    fun animateView(view: ImageView) {
        when (val drawable = view.drawable) {
            is AnimatedVectorDrawableCompat -> {
                drawable.start()
            }
            is AnimatedVectorDrawable -> {
                drawable.start()
            }
        }
    }

    fun resetView(view: ImageView?) {
        when (val drawable = view?.drawable) {
            is AnimatedVectorDrawableCompat -> {
                drawable.stop()
            }
            is AnimatedVectorDrawable -> {
                drawable.reset()
            }
        }
    }


}