package com.doitstudio.sleepest_master.model.data.sleepcalculation

import androidx.room.Entity

@Entity(tableName = "user_calculation_rating")
class UserCalculationRating() {

    companion object{

        fun BuildDefault() : UserCalculationRating {
            return UserCalculationRating()
        }

    }

}