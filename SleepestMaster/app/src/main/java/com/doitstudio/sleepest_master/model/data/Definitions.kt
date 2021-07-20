package com.doitstudio.sleepest_master.model.data

import androidx.room.Entity


// Enum actions for service start/stop
enum class Actions {
    START, STOP
}

interface IAlarmReceiverUsage {
    fun getAlarmReceiverUsageValue() : Int
}
enum class AlarmReceiverUsage : IAlarmReceiverUsage {
    DEFAULT {
        override fun getAlarmReceiverUsageValue(): Int = 0
    },
    START_FOREGROUND{
        override fun getAlarmReceiverUsageValue(): Int = 1
    },
    STOP_FOREGROUND{
        override fun getAlarmReceiverUsageValue(): Int = 2
    },
    DISABLE_ALARM{
        override fun getAlarmReceiverUsageValue(): Int = 3
    },
    NOT_SLEEPING{
        override fun getAlarmReceiverUsageValue(): Int = 4
    },
    START_WORKMANAGER_CALCULATION{
        override fun getAlarmReceiverUsageValue(): Int = 5
    },
    START_WORKMANAGER{
        override fun getAlarmReceiverUsageValue(): Int = 6
    },
    STOP_WORKMANAGER{
        override fun getAlarmReceiverUsageValue(): Int = 7
    },
    CURRENTLY_NOT_SLEEPING{
        override fun getAlarmReceiverUsageValue(): Int = 8
    }
}
interface IAlarmClockReceiverUsage {
    fun getAlarmClockReceiverUsageValue() : Int
}
enum class AlarmClockReceiverUsage : IAlarmClockReceiverUsage{
    DEFAULT {
        override fun getAlarmClockReceiverUsageValue(): Int = 0
    },
    START_ALARMCLOCK {
        override fun getAlarmClockReceiverUsageValue(): Int = 1
    },
    STOP_ALARMCLOCK {
        override fun getAlarmClockReceiverUsageValue(): Int = 2
    },
    SNOOZE_ALARMCLOCK {
        override fun getAlarmClockReceiverUsageValue(): Int = 3
    },
    LATEST_WAKEUP_ALARMCLOCK {
        override fun getAlarmClockReceiverUsageValue(): Int = 4
    }
}

interface  INotificationUsage {
    fun getNotificationUsageValue() : Int
}

enum class NotificationUsage : INotificationUsage {
    NOTIFICATION_FOREGROUND_SERVICE {
        override fun getNotificationUsageValue(): Int = 1
    },
    NOTIFICATION_USER_SHOULD_SLEEP {
        override fun getNotificationUsageValue(): Int = 2
    },
    NOTIFICATION_NO_API_DATA {
        override fun getNotificationUsageValue(): Int = 3
    },
    NOTIFICATION_ALARM_CLOCK {
        override fun getNotificationUsageValue(): Int = 4
    }
}

/**
 * Defines the different states of the sleep a user can be in
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
    UNIDENTIFIED;

    companion object {
        /**
         * Takes an [Int] and will return the associated [MobilePosition] (0 to 2)
         */
        fun getCount(type: Int): MobilePosition {
            return when (type) {
                0 -> INBED
                1 -> ONTABLE
                else -> UNIDENTIFIED // Avoiding dividing by zero
            }
        }
    }
}

/**
 * Defines where the light conditions is places at sleep time
 */
enum class LightConditions{
    DARK,
    LIGHT,
    UNIDENTIFIED;

    companion object {
        /**
         * Takes an [Int] and will return the associated [LightConditions] (0 to 2)
         */
        fun getCount(type: Int): LightConditions {
            return when (type) {
                0 -> DARK
                1 -> LIGHT
                else -> UNIDENTIFIED // Avoiding dividing by zero
            }
        }
    }
}

/**
 * Defines how often the user uses his phone
 */
enum class MobileUseFrequency{
    VERYLESS,
    LESS,
    NONE,
    OFTEN,
    VERYOFTEN;

    companion object {
        /**
         * Takes an [Int] and will return the associated [MobileUseFrequency] (0 to 5)
         */
        fun getCount(type: Int): MobileUseFrequency {
            return when (type) {
                0 -> MobileUseFrequency.VERYLESS
                1 -> MobileUseFrequency.LESS
                3 -> MobileUseFrequency.OFTEN
                4 -> MobileUseFrequency.VERYOFTEN
                else -> MobileUseFrequency.NONE // Avoiding dividing by zero
            }
        }

        /**
         * Takes an [MobileUseFrequency] and will return the associated [Int] value (0 to 4)
         */
        fun getValue(type: MobileUseFrequency): Int {
            return when (type) {
               MobileUseFrequency.VERYLESS -> 0
                MobileUseFrequency.LESS -> 1
                MobileUseFrequency.OFTEN-> 3
                MobileUseFrequency.VERYOFTEN -> 4
                else ->  2// Avoiding dividing by zero
            }
        }
    }

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
    EXTREMACTIVITY;

    companion object {
        /**
         * Takes an [ActivityOnDay] and will return the associated [Float] value for the sleep calculation factor
         * TODO(Define factors for the activity)
         */
        fun getFactor(type: ActivityOnDay): Float {
            return when (type) {
                NOACTIVITY -> 0.9f
                SMALLACTIVITY -> 0.95f
                NORMALACTIVITY -> 1f
                MUCHACTIVITY -> 1.05f
                EXTREMACTIVITY -> 1.1f
                else -> 1f
            }
        }
    }

}

/**
 * Defines the sleep time adjustment of a user sleep session
 */
enum class SleepTimeAdjustment{
    NONE,
    WAKEUPTOLATE,
    WAKEUPTOEARLY,
    ASLEEPTOLATE,
    ASLEEPTOEARLY
}

/**
 * Defines the sleep time duration adjustment of a user sleep session
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
 * It is used for the algorithms only
 */
enum class SleepDataFrequency{
    FIVE,
    TEN,
    THIRTY,
    NONE;

    companion object {
        /**
         * Takes an [SleepDataFrequency] and will return the associated [Int] value for the sleep calculation
         */
        fun getValue(type:SleepDataFrequency) : Int {
            return when (type) {
                SleepDataFrequency.FIVE -> 5
                SleepDataFrequency.TEN -> 10
                SleepDataFrequency.THIRTY -> 30
                else -> 1000 // Avoiding dividing by zero
            }
        }

        /**
         * Takes an [SleepDataFrequency] and will return the associated [Int] value for the sleep calculation input patterns for the machine learning model
         */
        fun getCount(type:SleepDataFrequency) : Int {
            return when (type) {
                SleepDataFrequency.FIVE -> 24
                SleepDataFrequency.TEN -> 12
                SleepDataFrequency.THIRTY -> 4
                else -> 1000 // Avoiding dividing by zero
            }
        }
    }
}

/**
 * Defines the actual model that is used in the process
 */
enum class ModelProcess{
    SLEEP04,
    SLEEP12,
    LIGHTAWAKE,
    TABLEBED;

    companion object {
        /**
         * Takes an [ModelProcess] and will return the associated [String] value for the sleep calculation
         */
        fun getString(type: ModelProcess): String {
            return type.toString().toLowerCase().capitalize()
        }
    }
}


