package com.doitstudio.sleepest_master.model.data.export

import com.doitstudio.sleepest_master.model.data.LightConditions
import com.doitstudio.sleepest_master.model.data.MobilePosition
import com.doitstudio.sleepest_master.sleepcalculation.model.SleepTimes
import com.doitstudio.sleepest_master.sleepcalculation.model.UserCalculationRating
import com.doitstudio.sleepest_master.sleepcalculation.model.UserSleepRating
import com.doitstudio.sleepest_master.storage.db.SleepApiRawDataEntity
import com.doitstudio.sleepest_master.storage.db.UserSleepSessionEntity

/**
 * This class extends the [UserSleepSessionEntity] with the associated list of [SleepApiRawDataEntity].
 * This provides all data in one class for exporting it as .json file for the user
 * It also enables the import of the data from a .json file
 */
data class UserSleepExportData(
        val id:Int,
        val mobilePosition: MobilePosition = MobilePosition.UNIDENTIFIED,
        val lightConditions: LightConditions = LightConditions.UNIDENTIFIED,
        val sleepTimes: SleepTimes = SleepTimes(),
        val userSleepRating: UserSleepRating = UserSleepRating(),
        val userCalculationRating: UserCalculationRating = UserCalculationRating(),
        var sleepApiRawData: List<SleepApiRawDataEntity>
)




