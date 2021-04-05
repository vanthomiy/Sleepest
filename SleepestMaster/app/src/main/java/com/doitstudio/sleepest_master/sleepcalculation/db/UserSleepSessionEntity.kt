package com.doitstudio.sleepest_master.sleepcalculation.db

import androidx.room.ColumnInfo
import androidx.room.Embedded
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.doitstudio.sleepest_master.model.data.SleepState
import com.doitstudio.sleepest_master.sleepcalculation.model.algorithm.SleepTimes
import com.doitstudio.sleepest_master.sleepcalculation.model.algorithm.SleepUserType
import com.doitstudio.sleepest_master.sleepcalculation.model.userimprovement.UserCalculationRating
import com.doitstudio.sleepest_master.sleepcalculation.model.userimprovement.UserSleepRating


@Entity(tableName = "user_sleep_session_table")
data class UserSleepSessionEntity(

        @PrimaryKey
        val id:Int,

        @Embedded val sleepTimes: SleepTimes,
        @Embedded val sleepUserType: SleepUserType,
        @Embedded val userSleepRating: UserSleepRating,
        @Embedded val userCalculationRating: UserCalculationRating,
)



