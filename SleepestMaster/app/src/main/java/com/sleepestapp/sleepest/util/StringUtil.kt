package com.sleepestapp.sleepest.util

import android.app.Application

object StringUtil {

    fun getStringXml(id:Int, application: Application): String {
        return application.resources.getString(id)
    }
}