package com.doitstudio.sleepest_master.sleepcalculation.model

import com.doitstudio.sleepest_master.model.data.SleepTimeAdjustment
import com.doitstudio.sleepest_master.storage.db.UserSleepSessionEntity

/**
 * Helper class that is implemented in the [UserSleepSessionEntity]
 * It contains the user calculation rating for each sleep-session.
 * With help of this we can provide functionality that allows the user to adjust the sleep on the next day.
 * The user can also rate the awake- and the sleep-detection algorithm. With this we can improve algorithms in the future.
 */
data class UserCalculationRating(

        /**
         * The user can choose [SleepTimeAdjustment.ASLEEPTOLATE] or [SleepTimeAdjustment.ASLEEPTOEARLY]
         */
        val sleepStartDetection	: SleepTimeAdjustment = SleepTimeAdjustment.NONE,
        /**
         * The user can choose [SleepTimeAdjustment.AWAKETOLATE] or [SleepTimeAdjustment.AWAKETOEARLY]
         */
        val sleepEndDetection	: SleepTimeAdjustment= SleepTimeAdjustment.NONE,
        /**
         * The user can rate the awake detection with a rating between 1 and 10 (best)
         */
        val awakeDetection	:Int = 0,
        /**
         * The user can rate the sleep detection with a rating between 1 and 10 (best)
         */
        val sleepCalcRating	:Int = 0

)