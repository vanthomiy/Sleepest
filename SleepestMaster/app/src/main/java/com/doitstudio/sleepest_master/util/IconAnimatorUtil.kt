package com.doitstudio.sleepest_master.util

import android.content.Context
import android.content.res.Configuration
import android.content.res.Configuration.UI_MODE_NIGHT_YES
import android.content.res.Resources
import android.graphics.drawable.AnimatedVectorDrawable
import android.graphics.drawable.Drawable
import android.widget.ImageView
import androidx.core.content.res.ResourcesCompat
import androidx.vectordrawable.graphics.drawable.AnimatedVectorDrawableCompat
import com.doitstudio.sleepest_master.R

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

    fun changeImageAndStartAnimation(view: ImageView, imageId:Int, resources:Resources) {

        val drawable: Drawable? = ResourcesCompat.getDrawable(resources, imageId, null)
        view.setImageDrawable(drawable)

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