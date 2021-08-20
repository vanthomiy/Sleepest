package com.doitstudio.sleepest_master.sleepcalculation.model

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.doitstudio.sleepest_master.model.data.SleepState
import com.doitstudio.sleepest_master.storage.db.UserSleepSessionEntity

/**
 * Helper class that is implemented in the [UserSleepSessionEntity]
 * It contains the detected times and states of a users sleep
 * Every time is stored as minutes
 */
@Entity
data class SleepTimes(
        /**
         * The utc timestamp in seconds when the first user sleep of the sleep session is detected
         */
        var sleepTimeStart: Int = 0,
        /**
         * The utc timestamp in seconds when the last user sleep of the sleep session is detected
         */
        var sleepTimeEnd: Int = 0,
        /**
         * The sleep time of the user in minutes. ([SleepState.LIGHT], [SleepState.DEEP], [SleepState.REM] or [SleepState.SLEEPING] phases are counted)
         */
        var sleepDuration: Int = 0,
        /**
         * The [SleepState.LIGHT] time of the user in minutes.
         */
        var lightSleepDuration: Int = 0,
        /**
         * The [SleepState.DEEP] time of the user in minutes.
         */
        var deepSleepDuration: Int = 0,
        /**
         * The [SleepState.REM] time of the user in minutes.
         */
        var remSleepDuration: Int = 0,
        /**
         * The [SleepState.AWAKE] of the user in the sleep-session in minutes. It's the time that the user is awake between sleeping-states.
         */
        var awakeTime: Int = 0,
)