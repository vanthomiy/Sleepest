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
         * User can define
         */
        var sleepDurationAdjustment	: SleepDurationAdjustment = SleepDurationAdjustment.NONE,//	Zu wenig oder zu viel schlaf für den nutzer
        var moodAfterSleep	: MoodType = MoodType.NONE,//	Gefühl nach dem Schlaf
        var moodOnNextDay	:MoodType = MoodType.NONE,//	Gefühl am kompletten nächsten Tag
        var wakeTimes	:Int = 0,//	evtl. vorausgefüllt, und man kann es ändern
        var activityOnDay	: ActivityOnDay = ActivityOnDay.NONE,//	War man aktiv am vortag? (Vorausgefüllt)

)