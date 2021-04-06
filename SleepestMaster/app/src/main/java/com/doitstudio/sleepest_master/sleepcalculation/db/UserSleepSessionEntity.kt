package com.doitstudio.sleepest_master.sleepcalculation.db

import androidx.room.Embedded
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.doitstudio.sleepest_master.sleepcalculation.model.algorithm.SleepTimes
import com.doitstudio.sleepest_master.sleepcalculation.model.algorithm.SleepUserType
import com.doitstudio.sleepest_master.sleepcalculation.model.userimprovement.UserCalculationRating
import com.doitstudio.sleepest_master.sleepcalculation.model.userimprovement.UserSleepRating


@Entity(tableName = "user_sleep_session_entity")
data class UserSleepSessionEntity(

        @PrimaryKey
        val id:Int,

        @Embedded(prefix = "sleepTimes") val sleepTimes: SleepTimes,
        @Embedded(prefix = "userType") val sleepUserType: SleepUserType,
        @Embedded(prefix = "sleepRating") val userSleepRating: UserSleepRating,
        @Embedded(prefix = "calcRating") val userCalculationRating: UserCalculationRating
)



