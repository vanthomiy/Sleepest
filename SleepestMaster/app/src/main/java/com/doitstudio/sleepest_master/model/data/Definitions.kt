package com.doitstudio.sleepest_master.model.data



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
    REM
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
    WAKEUPTOLATE,
    WAKEUPTOEARLY,
    ASLEEPTOLATE,
    ASLEEPTOEARLY

}

/**
 * Defines the sleep state pattern of a user sleep session
 */
enum class SleepStatePattern{
    TOMANYSLEEP,
    TOMANYLIGHT,
    TOMANYDEEP,
    TOMANYREM,
    TOLESSSLEEP,
    TOLESSLIGHT,
    TOLESSDEEP,
    TOLESSREM

}

/**
 * Defines the possible sleep start pattern of the user
 */
enum class UserStartPattern{
    SUPERLIGHT,
    LIGHT,
    NORMAL,
    HEAVY,
    SUPERHEAVY

}

/**
 * Defines the definitions of each model
 */
enum class SleepModelType{
    MAXSCHLAF,
    MINSCHLAF,
    MAXLICHT,
    MINLICHT,
    MAXMOTION,
    MINMOTION

}