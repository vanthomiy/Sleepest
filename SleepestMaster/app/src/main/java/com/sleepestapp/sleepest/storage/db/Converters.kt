package com.sleepestapp.sleepest.storage.db

import androidx.room.TypeConverter
import com.sleepestapp.sleepest.model.data.*
import java.time.DayOfWeek

/**
 * Converters are being used to convert complex datatype to single type
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
        if(string != null && string != "")
        {
            val split = string?.split(";")
            val map = split?.mapNotNull { DayOfWeek.valueOf(it) }
            return ArrayList(map ?: emptyList())

        }

        return ArrayList()
    }
}