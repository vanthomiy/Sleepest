package com.doitstudio.sleepest_master.storage.db

import androidx.room.TypeConverter
import com.doitstudio.sleepest_master.model.data.SleepState

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

}