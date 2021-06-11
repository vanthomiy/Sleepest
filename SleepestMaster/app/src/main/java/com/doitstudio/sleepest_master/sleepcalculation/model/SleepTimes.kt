package com.doitstudio.sleepest_master.sleepcalculation.model

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity
data class SleepTimes(

        // all in minutes!!
        var sleepTimeStart: Int = 0,
        var sleepTimeEnd: Int = 0,
        var sleepDuration: Int = 0,
        var lightSleepDuration: Int = 0,
        var deepSleepDuration: Int = 0,
        var remSleepDuration: Int = 0,
        var awakeTime: Int = 0,
)