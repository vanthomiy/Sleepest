package com.doitstudio.sleepest_master.model.data

import androidx.room.Entity


// Enum actions for service start/stop
internal enum class Actions {
    START, STOP
}

/**
 * Defines the diffrent states of the sleep a user can be in
 */
enum class SleepState {
    AWAKE,
    LIGHT,
    DEEP,
    REM,
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
enum class SleepTimePattern{
    NONE,
    WAKEUPTOLATE,
    WAKEUPTOEARLY,
    ASLEEPTOLATE,
    ASLEEPTOEARLY,
    STANDARD

}

/**
 * Defines the sleep state pattern of a user sleep session
 */
enum class SleepStatePattern{
    NONE,
    TOMANYSLEEP,
    TOMANYLIGHT,
    TOMANYDEEP,
    TOMANYREM,
    TOLESSSLEEP,
    TOLESSLIGHT,
    TOLESSDEEP,
    TOLESSREM,
    STANDARD

}

/**
 * Defines the possible sleep start pattern of the user
 */
enum class UserFactorPattern{
    NONE,
    SUPERLIGHT,
    LIGHT,
    NORMAL,
    HEAVY,
    SUPERHEAVY

}
