package com.doitstudio.sleepest_master.sleepcalculation.model

import com.doitstudio.sleepest_master.model.data.ActivityOnDay
import com.doitstudio.sleepest_master.model.data.MoodType
import com.doitstudio.sleepest_master.model.data.SleepDurationAdjustment

data class UserSleepRating (

        var sleepDurationAdjustment	: SleepDurationAdjustment = SleepDurationAdjustment.NONE,//	Zu wenig oder zu viel schlaf für den nutzer
        var moodAfterSleep	: MoodType = MoodType.NONE,//	Gefühl nach dem Schlaf
        var moodOnNextDay	:MoodType = MoodType.NONE,//	Gefühl am kompletten nächsten Tag
        var wakeTimes	:Int = 0,//	evtl. vorausgefüllt, und man kann es ändern
        var activityOnDay	: ActivityOnDay = ActivityOnDay.NONE,//	War man aktiv am vortag? (Vorausgefüllt)

)