package com.doitstudio.sleepest_master.sleepcalculation.model.algorithm

import androidx.room.Embedded
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.doitstudio.sleepest_master.model.data.MobilePosition
import com.doitstudio.sleepest_master.model.data.SleepStatePattern
import com.doitstudio.sleepest_master.model.data.SleepTimePattern
import com.doitstudio.sleepest_master.model.data.UserFactorPattern


@Entity(tableName = "sleep_user_type")
data class SleepUserType(

       // @PrimaryKey
       // val userId:Int,

        var mobilePosition: MobilePosition = MobilePosition.UNIDENTIFIED,
)