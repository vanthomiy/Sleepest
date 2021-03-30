package com.doitstudio.sleepest_master.model.data.sleepcalculation

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.doitstudio.sleepest_master.model.data.MobilePosition

@Entity(tableName = "sleep_user_type")
class SleepUserType(

    @PrimaryKey
    @ColumnInfo(name = "mobile_position")
    var mobilePosition: MobilePosition,


){

    companion object{

        fun BuildDefault() : SleepUserType {
            return SleepUserType(MobilePosition.UNIDENTIFIED)
        }

    }

}