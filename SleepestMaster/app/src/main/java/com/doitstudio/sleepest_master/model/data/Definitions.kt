package com.doitstudio.sleepest_master.model.data

import android.content.Context
import androidx.room.Entity
import com.doitstudio.sleepest_master.R


// Enum actions for service start/stop
enum class Actions {
    START, STOP
}

enum class AlarmReceiverUsage {
    START_FOREGROUND,
    STOP_FOREGROUND,
    DISABLE_ALARM,
    NOT_SLEEPING,
    START_WORKMANAGER_CALCULATION,
    START_WORKMANAGER,
    STOP_WORKMANAGER,
    CURRENTLY_NOT_SLEEPING ,
    SOLVE_API_PROBLEM,
    GO_TO_SLEEP;

    companion object {
        fun getCount(type : AlarmReceiverUsage) : Int {
            return when (type) {
                START_FOREGROUND -> 31
                STOP_FOREGROUND -> 32
                DISABLE_ALARM -> 33
                NOT_SLEEPING -> 34
                START_WORKMANAGER_CALCULATION -> 35
                START_WORKMANAGER -> 36
                STOP_WORKMANAGER -> 37
                CURRENTLY_NOT_SLEEPING -> 38
                SOLVE_API_PROBLEM -> 39
                GO_TO_SLEEP -> 40
            }
        }
    }
}

enum class AlarmClockReceiverUsage {

    START_ALARMCLOCK,
    STOP_ALARMCLOCK,
    SNOOZE_ALARMCLOCK,
    LATEST_WAKEUP_ALARMCLOCK;


    companion object {
        fun getCount(type : AlarmClockReceiverUsage) : Int {
            return when (type) {
                START_ALARMCLOCK -> 11
                STOP_ALARMCLOCK -> 12
                SNOOZE_ALARMCLOCK -> 13
                LATEST_WAKEUP_ALARMCLOCK -> 14
            }
        }
    }
}

enum class NotificationUsage {
    NOTIFICATION_FOREGROUND_SERVICE,
    NOTIFICATION_USER_SHOULD_SLEEP,
    NOTIFICATION_NO_API_DATA,
    NOTIFICATION_ALARM_CLOCK;

    companion object {
        fun getCount(type : NotificationUsage) : Int {
            return when (type) {
                NOTIFICATION_FOREGROUND_SERVICE -> 1
                NOTIFICATION_USER_SHOULD_SLEEP -> 2
                NOTIFICATION_NO_API_DATA -> 3
                NOTIFICATION_ALARM_CLOCK -> 4
            }
        }
    }
}

enum class ActivityIntentUsage {
    MAIN_ACTIVITY,
    LOCKSCREEN_ACTIVITY;

    companion object {
        fun getCount(type : ActivityIntentUsage) : Int {
            return when (type) {
                MAIN_ACTIVITY -> 50
                LOCKSCREEN_ACTIVITY -> 51

            }
        }
    }
}

enum class ActivityTransitionUsage {
    REQUEST_CODE;

    companion object {
        fun getCount(type : ActivityTransitionUsage) : Int {
            return when (type) {
                REQUEST_CODE -> 60
            }
        }
    }
}

enum class SleepApiUsage {
    REQUEST_CODE;

    companion object {
        fun getCount(type : SleepApiUsage) : Int {
            return when (type) {
                REQUEST_CODE -> 70
            }
        }
    }
}

enum class AlarmCycleStates {
    NO_STATE_DETECTED,
    BETWEEN_SLEEPTIME_START_AND_CALCULATION,
    BETWEEN_CALCULATION_AND_FIRST_WAKEUP,
    BETWEEN_FIRST_AND_LAST_WAKEUP,
    BETWEEN_LAST_WAKEUP_AND_SLEEPtIME_END,
    BETWEEN_SLEEPTIME_END_AND_SLEEPTIME_START;

    companion object {
        fun getCount(type : AlarmCycleStates) : Int {
            return when (type) {
                NO_STATE_DETECTED -> 80
                BETWEEN_SLEEPTIME_START_AND_CALCULATION -> 81
                BETWEEN_CALCULATION_AND_FIRST_WAKEUP -> 82
                BETWEEN_FIRST_AND_LAST_WAKEUP -> 83
                BETWEEN_LAST_WAKEUP_AND_SLEEPtIME_END -> 84
                BETWEEN_SLEEPTIME_END_AND_SLEEPTIME_START -> 85

            }
        }
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
    EMPOWERED,
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
    }
}

/**
 * Helper for checking sleep times for alarms
 */
enum class AlarmSleepChangeFrom {
    DURATION,
    WAKEUPEARLYLY,
    WAKEUPLATE
}

/**
 * Helper for checking sleep times for alarms
 */
enum class SleepSleepChangeFrom {
    DURATION,
    SLEEPTIMESTART,
    SLEEPTIMEEND
}

/**
 * Helper for different credits websites
 */
enum class Websites {
    FLATICON,
    LOTTIEFILES;

    companion object {

        fun getWebsite(type:Websites) : String {
            return when (type) {
                Websites.FLATICON -> "https://flaticon.com/"
                Websites.LOTTIEFILES -> "https://lottiefiles.com/"
                else -> ""
            }
        }

        fun getName(type:Websites) : String {
            return when (type) {
                Websites.FLATICON -> "Flaticon"
                Websites.LOTTIEFILES -> "Lottifiles"
                else -> ""
            }
        }
    }
}


/**
 * Helper for different info types
 */
enum class Info {
    SLEEP,
    DAY_HISTORY,
    WEEK_HISTORY,
    MONTH_HISTORY,
    SETTINGS,
    ALARM;
    companion object{

        fun getById(id:Int) : Info{
            return when(id){
                0 -> SLEEP
                1 -> DAY_HISTORY
                1 -> WEEK_HISTORY
                1 -> MONTH_HISTORY
                2 -> SETTINGS
                else -> ALARM
            }
        }

        fun getName(type:Info, context:Context) : String {
            return when (type) {
                SLEEP -> context.resources.getString(R.string.sleep_sleep_header)
                SETTINGS -> context.resources.getString(R.string.profile_header)
                DAY_HISTORY -> context.resources.getString(R.string.history_day_title)
                WEEK_HISTORY -> context.resources.getString(R.string.history_week_title)
                MONTH_HISTORY -> context.resources.getString(R.string.history_month_title)
                ALARM -> context.resources.getString(R.string.sleep_alarm_header)
                else -> ""
            }
        }

    }
}

/**
 * Helper for different info types style
 */
enum class InfoEntityStlye {
    PICTURE_LEFT,
    PICTURE_RIGHT,
    PICTURE_TOP,
    PICTURE_BOTTOM,
    RANDOM;

    companion object{

        fun getById(id:Int) : InfoEntityStlye{
            return when(id){
                0 -> PICTURE_LEFT
                1 -> PICTURE_RIGHT
                2 -> PICTURE_TOP
                else -> PICTURE_BOTTOM
            }
        }

    }
}
