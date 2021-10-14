package com.sleepestapp.sleepest.util

import android.content.Context
import android.content.res.Configuration
import android.graphics.Color

object DesignUtil {

    /**
     * Determines whether system dark mode is active or not.
     */
    fun checkDarkModeActive(context: Context, appDarkMode: Boolean, darkModeAuto: Boolean) : Boolean {
        val systemDarkMode = context.resources?.configuration?.uiMode?.and(Configuration.UI_MODE_NIGHT_MASK)
        return if (darkModeAuto) {
            systemDarkMode == Configuration.UI_MODE_NIGHT_YES
        } else {
            appDarkMode
        }
    }

    /**
     * Provides a accent color depending on the dark mode settings.
     */
    fun colorDarkMode(darkModeOn: Boolean) : Int {
        return when (darkModeOn) {
            true -> Color.WHITE
            else -> Color.BLACK
        }
    }

    /**
     * Provides the color for the hole of the [PieChart] depending on the current dark mode.
     */
    fun determineHoleColorPieChart(darkModeOn: Boolean) : Int {
        return when (darkModeOn) {
            true -> Color.parseColor("#1a1a1a")
            else -> Color.parseColor("#FFFFFF")
        }
    }
}