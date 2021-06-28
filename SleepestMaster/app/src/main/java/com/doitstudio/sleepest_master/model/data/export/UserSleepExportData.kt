package com.doitstudio.sleepest_master.model.data.export


import com.doitstudio.sleepest_master.model.data.MobilePosition
import com.doitstudio.sleepest_master.sleepcalculation.model.SleepTimes
import com.doitstudio.sleepest_master.sleepcalculation.model.UserCalculationRating
import com.doitstudio.sleepest_master.sleepcalculation.model.UserSleepRating
import com.doitstudio.sleepest_master.storage.db.SleepApiRawDataEntity


data class UserSleepExportData(

        val id:Int,

        val mobilePosition: MobilePosition = MobilePosition.UNIDENTIFIED,

        val sleepTimes: SleepTimes = SleepTimes(),
        val userSleepRating: UserSleepRating = UserSleepRating(),
        val userCalculationRating: UserCalculationRating = UserCalculationRating(),
        var sleepApiRawData: List<SleepApiRawDataEntity>
)




