package com.doitstudio.sleepest_master.storage.db

import android.util.JsonReader
import androidx.room.TypeConverter
import com.doitstudio.sleepest_master.model.data.MobilePosition
import com.doitstudio.sleepest_master.model.data.SleepModelType
import com.doitstudio.sleepest_master.model.data.SleepState
import com.doitstudio.sleepest_master.sleepcalculation.model.algorithm.SleepModelValues
import java.io.StringReader
import java.util.*
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


}