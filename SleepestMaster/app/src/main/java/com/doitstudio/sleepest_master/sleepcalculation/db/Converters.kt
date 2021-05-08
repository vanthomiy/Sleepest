package com.doitstudio.sleepest_master.sleepcalculation.db

import androidx.room.TypeConverter
import com.doitstudio.sleepest_master.model.data.*
import java.time.DayOfWeek

class Converters {

    @TypeConverter
    fun fromSleepStatePattern(sleepStatePattern: SleepStatePattern) : Int {
        return sleepStatePattern.ordinal
    }

    @TypeConverter
    fun toSleepStatePattern(sleepStatePattern: Int) : SleepStatePattern {
        return SleepStatePattern.values()[sleepStatePattern]
    }

    @TypeConverter
    fun fromSleepTimePattern(sleepTimePattern: SleepTimePattern) : Int {
        return sleepTimePattern.ordinal
    }

    @TypeConverter
    fun toSleepTimePattern(sleepTimePattern: Int) : SleepTimePattern {
        return SleepTimePattern.values()[sleepTimePattern]
    }

    @TypeConverter
    fun fromArrayListOfSleepTimePattern(list: ArrayList<SleepTimePattern>): String {
        return list?.joinToString(separator = ";") { it.toString() } ?: ""
    }

    @TypeConverter
    fun toArrayListOfSleepTimePattern(string: String?): ArrayList<SleepTimePattern> {
        return ArrayList(string?.split(";")?.mapNotNull {
            SleepTimePattern.values()[SleepTimePattern.valueOf(it).ordinal]
        } ?: emptyList())
    }

    @TypeConverter
    fun fromUserStartPattern(userFactorPattern: UserFactorPattern) : Int {
        return userFactorPattern.ordinal
    }

    @TypeConverter
    fun toUserStartPattern(userFactorPattern: Int) : UserFactorPattern {
        return UserFactorPattern.values()[userFactorPattern]
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