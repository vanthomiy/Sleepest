package com.sleepestapp.sleepest.util

import android.app.Application

object StringUtil {

    /**
     * Provides the requested string from the resources.
     */
    fun getStringXml(id:Int, application: Application): String {
        return application.resources.getString(id)
    }
}