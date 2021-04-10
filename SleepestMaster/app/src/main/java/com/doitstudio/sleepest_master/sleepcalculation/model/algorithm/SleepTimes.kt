package com.doitstudio.sleepest_master.sleepcalculation.model.algorithm

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity
data class SleepTimes(

        val sleepTimeStart: Int,
        val sleepTimeEnd: Int,
        val sleepDuration: Int,
        val lightSleepDuration: Int,
        val deepSleepDuration: Int,
        val remSleepDuration: Int,
        val awakeTime: Int,

)