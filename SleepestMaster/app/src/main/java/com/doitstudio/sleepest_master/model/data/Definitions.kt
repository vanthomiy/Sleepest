package com.doitstudio.sleepest_master.model.data

/**
 * Defines the diffrent states of the sleep a user can be in
 */
enum class SleepState {
    awake,
    light,
    deep,
    rem
}

// Enum actions for service start/stop
internal enum class Actions {
    START, STOP
}
