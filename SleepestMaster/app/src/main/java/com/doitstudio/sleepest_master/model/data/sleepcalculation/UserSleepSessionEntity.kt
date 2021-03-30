package com.doitstudio.sleepest_master.model.data.sleepcalculation

import androidx.room.ColumnInfo
import androidx.room.Embedded
import androidx.room.Entity
import androidx.room.PrimaryKey


/**
 * Entity class (table version of the class) for [UserSleepSessionEntity] which represents a sleep segment
 * containing additional information of the sleep of the user
 */
@Entity(tableName = "user_sleep_session_table")
data class UserSleepSessionEntity(

    @PrimaryKey
    @Embedded val sleepTimes: SleepTimes,

    @Embedded val sleepUserType: SleepUserType?,

    @Embedded val userSleepRating: UserSleepRating?,

    @Embedded val userCalculationRating: UserCalculationRating?,
)  {

    companion object{

        fun BuildDefault() : UserSleepSessionEntity {

            val sleepTimes = SleepTimes.BuildDefault()
            val sleepUserType = SleepUserType.BuildDefault()
            val userSleepRating = UserSleepRating.BuildDefault()
            val userCalculationRating = UserCalculationRating.BuildDefault()

            return UserSleepSessionEntity(sleepTimes, sleepUserType,userSleepRating,userCalculationRating)
        }

    }

}