package com.doitstudio.sleepest_master.model.data.sleepcalculation

import androidx.room.Entity

@Entity(tableName = "user_sleep_rating")
class UserSleepRating (){

    companion object{

        fun BuildDefault() : UserSleepRating {
            return UserSleepRating()
        }

    }

}