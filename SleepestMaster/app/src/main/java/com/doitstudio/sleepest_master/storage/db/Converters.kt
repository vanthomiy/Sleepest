package com.doitstudio.sleepest_master.storage.db

import androidx.room.TypeConverter
import com.doitstudio.sleepest_master.model.data.*
import com.google.android.gms.location.DetectedActivity
import java.time.DayOfWeek

/**
 * Converters are beeing used to convert complex datatypes to single types
 * E.g you have to implicit convert a enum to a int and back, to store the value in the DB
 */
class Converters {

    @TypeConverter
    fun fromSleepStatePattern(sleepStatePattern: SleepDurationAdjustment) : Int {
        return sleepStatePattern.ordinal
    }

    @TypeConverter
    fun toSleepStatePattern(sleepStatePattern: Int) : SleepDurationAdjustment {
        return SleepDurationAdjustment.values()[sleepStatePattern]
    }

    @TypeConverter
    fun fromSleepTimePattern(sleepTimePattern: SleepTimeAdjustment) : Int {
        return sleepTimePattern.ordinal
    }

    @TypeConverter
    fun toSleepTimePattern(sleepTimePattern: Int) : SleepTimeAdjustment {
        return SleepTimeAdjustment.values()[sleepTimePattern]
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

    @TypeConverter
    fun fromSleepState(sleepState: SleepState) : Int {
        return sleepState.ordinal
    }

    @TypeConverter
    fun toSleepState(sleepState: Int) : SleepState {
        return SleepState.values()[sleepState]
    }

    @TypeConverter
    fun fromDayOfWeekList(dayOfWeek: ArrayList<DayOfWeek>?) : String {
        return dayOfWeek?.joinToString(";"){it.toString()}?:""
    }

    @TypeConverter
    fun toDayOfWeekList(string: String?) : ArrayList<DayOfWeek> {
        return ArrayList(string?.split(";")?.mapNotNull { DayOfWeek.valueOf(it) } ?: emptyList())
    }
}