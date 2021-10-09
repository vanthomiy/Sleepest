package com.sleepestapp.sleepest.util

import android.content.Context
import android.content.res.Configuration
import android.graphics.Color

object DesignUtil {
    fun checkDarkModeActive(context: Context, appDarkMode: Boolean, darkModeAuto: Boolean) : Boolean {
        val systemDarkMode = context.resources?.configuration?.uiMode?.and(Configuration.UI_MODE_NIGHT_MASK)
        return if (darkModeAuto) {
            systemDarkMode == Configuration.UI_MODE_NIGHT_YES
        } else {
            appDarkMode
        }
    }

    fun colorDarkMode(darkModeOn: Boolean) : Int {
        return when (darkModeOn) {
            true -> Color.WHITE
            else -> Color.BLACK
        }
    }

    fun determineHoleColorPieChart(darkModeOn: Boolean) : Int {
        return when (darkModeOn) {
            true -> Color.parseColor("#1a1a1a")
            else -> Color.parseColor("#FFFFFF")
        }
    }
}