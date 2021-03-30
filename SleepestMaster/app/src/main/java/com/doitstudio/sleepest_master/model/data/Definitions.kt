package com.doitstudio.sleepest_master.model.data

/**
 * Defines the diffrent states of the sleep a user can be in
 */
enum class SleepState {
    AWAKE,
    LIGHT,
    DEEP,
    REM
}

// Enum actions for service start/stop
internal enum class Actions {
    START, STOP
}

/**
 * Defines where the mobile phone is places at sleep time
 */
enum class MobilePosition{
    INBED,
    ONTABLE,
    UNIDENTIFIED
}

