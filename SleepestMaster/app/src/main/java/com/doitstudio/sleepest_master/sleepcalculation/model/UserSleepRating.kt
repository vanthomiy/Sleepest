package com.doitstudio.sleepest_master.sleepcalculation.model

import com.doitstudio.sleepest_master.model.data.ActivityOnDay
import com.doitstudio.sleepest_master.model.data.MoodType
import com.doitstudio.sleepest_master.model.data.SleepDurationAdjustment
import com.doitstudio.sleepest_master.storage.db.UserSleepSessionEntity

/**
 * Helper class that is implemented in the [UserSleepSessionEntity]
 * It contains the user sleep rating for each sleep-session.
 * The user can save information about it's mood and other things.
 */
data class UserSleepRating (

        /**
         * User can define if he slept to much or to less in his opinion
         */
        var sleepDurationAdjustment	: SleepDurationAdjustment = SleepDurationAdjustment.NONE,

        /**
         * User can set the mood on the next day direct after a sleep
         */
        var moodAfterSleep	: MoodType = MoodType.NONE,

        /**
         * User can set the mood on the complete next day after a sleep
         */
        var moodOnNextDay	:MoodType = MoodType.NONE,

        /**
         * TODO(Not implemented yet)
         * User can define wakeup times
         */
        var wakeTimes	:Int = 0,

        /**
         * Is set by the activity transition api data
         * Default is [ActivityOnDay.NONE]
         * Values from [ActivityOnDay.NOACTIVITY] to [ActivityOnDay.EXTREMACTIVITY]
         */
        var activityOnDay	: ActivityOnDay = ActivityOnDay.NONE,

)