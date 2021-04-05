package com.doitstudio.sleepest_master.sleepcalculation.model.userimprovement

import com.doitstudio.sleepest_master.model.data.ActivityOnDay
import com.doitstudio.sleepest_master.model.data.MoodType
import com.doitstudio.sleepest_master.model.data.SleepStatePattern

data class UserSleepRating (

    val sleepDuration	: SleepStatePattern,//	Zu wenig oder zu viel schlaf
    val moodAfterSleep	: MoodType,//	Gefühl nach dem Schlaf
    val moodOnNextDay	:MoodType,//	Gefühl am kompletten nächsten Tag
    val wakeTimes	:Int,//	evtl. vorausgefüllt, und man kann es ändern
    val activityOnDay	: ActivityOnDay,//	War man aktiv am vortag? (Vorausgefüllt)

)