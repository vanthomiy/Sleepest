package com.doitstudio.sleepest_master.storage.db

import androidx.room.TypeConverter
import com.doitstudio.sleepest_master.model.data.*
import kotlin.collections.ArrayList


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

}