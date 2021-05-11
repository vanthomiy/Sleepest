package com.doitstudio.sleepest_master.model.data

import androidx.room.Entity


// Enum actions for service start/stop
enum class Actions {
    START, STOP
}

internal enum class AlarmReceiverUse {
    DEFAULT, START_FOREGROUND, STOP_FOREGROUND, DISABLE_ALARM, LAST_WAKEUP, START_CALCULATION
}

internal enum class AlarmClockUse {
    DEFAULT, START_ALARM, STOP_ALARM, SNOOZE_ALARM, LAST_WAKEUP_ALARM
}

/**
 * Defines the diffrent states of the sleep a user can be in
 */
enum class SleepState {
    AWAKE,
    LIGHT,
    DEEP,
    REM,
    SLEEPING,
    NONE
}


/**
 * Defines where the mobile phone is places at sleep time
 */
enum class MobilePosition{
    INBED,
    ONTABLE,
    UNIDENTIFIED
}

/**
 * Defines the mood of the user after/before the sleep
 */
enum class MoodType{
    NONE,
    BAD,
    GOOD,
    EXCELLENT,
    LAZY,
    TIRED,

}

/**
 * Defines the activity of the user the day before the sleep session
 */
enum class ActivityOnDay{
    NONE,
    NOACTIVITY,
    SMALLACTIVITY,
    NORMALACTIVITY,
    MUCHACTIVITY,
    EXTREMACTIVITY

}

/**
 * Defines the sleep time pattern of a user sleep session
 */
enum class SleepTimeAdjustment{
    NONE,
    WAKEUPTOLATE,
    WAKEUPTOEARLY,
    ASLEEPTOLATE,
    ASLEEPTOEARLY
}

/**
 * Defines the sleep time pattern of a user sleep session
 */
enum class SleepDurationAdjustment{
    NONE,
    PERFECT,
    TOLESS,
    TOMUCH,
    WAYTOLESS,
    WAYTOMUCH
}

/**
 * Defines how often the sleep api data is available
 */
enum class SleepDataFrequency{
    FIVE,
    TEN,
    THIRTY,
    NONE;

    companion object {
        fun getValue(type:SleepDataFrequency) : Int {
            return when (type) {
                SleepDataFrequency.FIVE -> 5
                SleepDataFrequency.TEN -> 10
                SleepDataFrequency.THIRTY -> 30
                else -> 1000 // Avoiding dividing by zero
            }
        }

        fun getCount(type:SleepDataFrequency) : Int {
            return when (type) {
                SleepDataFrequency.FIVE -> 24
                SleepDataFrequency.TEN -> 11
                SleepDataFrequency.THIRTY -> 3
                else -> 1000 // Avoiding dividing by zero
            }
        }
    }
}

/**
 * Defines the actual process that is to be done
 */
enum class ModelProcess{
    SLEEP04,
    SLEEP12,
    LIGHTAWAKE,
    TABLEBED;

    companion object {
        fun getString(type: ModelProcess): String {
            return type.toString().toLowerCase().capitalize()
        }
    }
}


