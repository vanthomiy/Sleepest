package com.sleepestapp.sleepest.util

object WeekDaysUtil {

    fun getWeekDayByNumber(value:Int) : String{

        return when(value){
            0 -> "Mo."
            1 -> "Di."
            2 -> "Mi."
            3 -> "Do."
            4 -> "Fr."
            5 -> "Sa."
            else -> "So."
        }

    }

}