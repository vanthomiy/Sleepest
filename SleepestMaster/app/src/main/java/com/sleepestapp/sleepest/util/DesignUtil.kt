package com.sleepestapp.sleepest.util

import android.content.Context
import android.content.res.Configuration
import android.graphics.Color

object DesignUtil {
    fun checkDarkModeActive(context: Context) : Boolean {
        return when (context.resources?.configuration?.uiMode?.and(Configuration.UI_MODE_NIGHT_MASK)) {
            Configuration.UI_MODE_NIGHT_NO -> {
                false
            }
            else -> true
        }
    }

    fun colorDarkMode(darkModeOn: Boolean) : Int {
        return when (darkModeOn) {
            true -> Color.WHITE
            else -> Color.BLACK
        }
    }
}