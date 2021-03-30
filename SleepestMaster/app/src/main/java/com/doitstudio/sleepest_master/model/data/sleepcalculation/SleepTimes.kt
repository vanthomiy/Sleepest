package com.doitstudio.sleepest_master.model.data.sleepcalculation

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "sleep_times")
class SleepTimes(

    @PrimaryKey
    @ColumnInfo(name = "sleep_time_start")
    var sleepTimeStart: Int,

    @ColumnInfo(name = "sleep_time_end")
    var sleepTimeEnd: Int,

    @ColumnInfo(name = "sleep_duration")
    var sleepDuration: Int,

    @ColumnInfo(name = "light_sleep_duration")
    var lightSleepDuration: Int,

    @ColumnInfo(name = "deep_sleep_duration")
    var deepSleepDuration: Int,

    @ColumnInfo(name = "rem_sleep_duration")
    var remSleepDuration: Int,

    @ColumnInfo(name = "awake_time")
    var awakeTime: Int,

){

    companion object{

        fun BuildDefault() : SleepTimes {
            return SleepTimes(0,0,0,0,0,0,0)
        }

    }

}