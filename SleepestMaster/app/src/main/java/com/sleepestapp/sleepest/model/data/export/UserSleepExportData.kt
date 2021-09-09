package com.sleepestapp.sleepest.model.data.export

import com.sleepestapp.sleepest.model.data.LightConditions
import com.sleepestapp.sleepest.model.data.MobilePosition
import com.sleepestapp.sleepest.sleepcalculation.model.SleepTimes
import com.sleepestapp.sleepest.sleepcalculation.model.UserCalculationRating
import com.sleepestapp.sleepest.sleepcalculation.model.UserSleepRating
import com.sleepestapp.sleepest.storage.db.SleepApiRawDataEntity
import com.sleepestapp.sleepest.storage.db.UserSleepSessionEntity

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





