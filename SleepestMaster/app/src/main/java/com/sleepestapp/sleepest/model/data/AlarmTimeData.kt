package com.sleepestapp.sleepest.model.data

data class AlarmTimeData(
    // True = before, False = After or In
    val isBeforeSleepTime:Boolean,
    val isInSleepTime:Boolean,
    val sleepTimeOverTwoDays:Boolean,
    val alarmIsOnSameDay:Boolean
)
