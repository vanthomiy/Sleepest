package com.doitstudio.sleepest_master.sleepcalculation.db

import androidx.room.TypeConverter
import com.doitstudio.sleepest_master.model.data.*

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
    fun fromArrayListOfFloats(list: ArrayList<Int>?): String {
        return list?.joinToString(separator = ";") { it.toString() } ?: ""
    }

    @TypeConverter
    fun toArrayListOfFloats(string: String?): ArrayList<Int> {
        return ArrayList(string?.split(";")?.mapNotNull { it.toIntOrNull() } ?: emptyList())
    }

    @TypeConverter
    fun fromUserStartPattern(userStartPattern: UserStartPattern) : Int {
        return userStartPattern.ordinal
    }

    @TypeConverter
    fun toUserStartPattern(userStartPattern: Int) : UserStartPattern {
        return UserStartPattern.values()[userStartPattern]
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