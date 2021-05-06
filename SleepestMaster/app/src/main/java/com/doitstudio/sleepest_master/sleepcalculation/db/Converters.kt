package com.doitstudio.sleepest_master.sleepcalculation.db

import androidx.room.TypeConverter
import com.doitstudio.sleepest_master.model.data.*

class Converters {


    @TypeConverter
    fun fromSleepState(sleepState: SleepState) : Int {
        return sleepState.ordinal
    }

    @TypeConverter
    fun toSleepState(sleepState: Int) : SleepState {
        return SleepState.values()[sleepState]
    }

    @TypeConverter
    fun fromMobilePosition(mobilePosition: MobilePosition) : Int {
        return mobilePosition.ordinal
    }

    @TypeConverter
    fun toMobilePosition(mobilePosition: Int) : MobilePosition {
        return MobilePosition.values()[mobilePosition]
    }

    @TypeConverter
    fun fromMoodType(moodType: MoodType) : Int {
        return moodType.ordinal
    }

    @TypeConverter
    fun toMoodType(moodType: Int) : MoodType {
        return MoodType.values()[moodType]
    }

    @TypeConverter
    fun fromActivityOnDay(activityOnDay: ActivityOnDay) : Int {
        return activityOnDay.ordinal
    }

    @TypeConverter
    fun toActivityOnDay(activityOnDay: Int) : ActivityOnDay {
        return ActivityOnDay.values()[activityOnDay]
    }

}