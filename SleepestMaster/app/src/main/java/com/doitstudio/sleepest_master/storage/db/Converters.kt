package com.doitstudio.sleepest_master.storage.db

import androidx.room.TypeConverter
import com.doitstudio.sleepest_master.model.data.MobilePosition
import com.doitstudio.sleepest_master.model.data.SleepState
import java.time.DayOfWeek

/**
 * Converters are beeing used to convert complex datatypes to single types
 * E.g you have to implicit convert a enum to a int and back, to store the value in the DB
 */
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
    fun fromDayOfWeekList(dayOfWeek: ArrayList<DayOfWeek>?) : String {
        return dayOfWeek?.joinToString(";"){it.toString()}?:""
    }

    @TypeConverter
    fun toDayOfWeekList(string: String?) : ArrayList<DayOfWeek> {
        return ArrayList(string?.split(";")?.mapNotNull { DayOfWeek.valueOf(it) } ?: emptyList())
    }

}