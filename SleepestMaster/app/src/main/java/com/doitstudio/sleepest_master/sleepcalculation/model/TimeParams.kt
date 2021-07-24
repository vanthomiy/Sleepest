package com.doitstudio.sleepest_master.sleepcalculation.model

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.doitstudio.sleepest_master.model.data.LightConditions
import com.doitstudio.sleepest_master.model.data.MobilePosition
import com.doitstudio.sleepest_master.model.data.SleepState
import com.doitstudio.sleepest_master.storage.db.AlgorithmParams
import com.doitstudio.sleepest_master.storage.db.UserSleepSessionEntity

/**
 * Stores the param values for the algorithm
 * It stores different values for each
 */
data class TimeParams(

        /**
         * The utc timestamp in seconds when the first user sleep of the sleep session is detected
         */
        var before: Float = 0f,

        /**
         * The utc timestamp in seconds when the last user sleep of the sleep session is detected
         */
        var after: Float = 0f,

        /**
         * The utc timestamp in seconds when the last user sleep of the sleep session is detected
         */
        var threshold: ThresholdParams,

){
        fun mergeParameters(factorParams: TimeParams){
                before *= factorParams.before
                after *= factorParams.after
                threshold.mergeParameters(factorParams.threshold)
        }

        companion object{
                fun createSleepStartBorder(mobilePosition: MobilePosition) : TimeParams{

                        return TimeParams(
                                before = 4f,
                                after = 1f,
                                threshold = ThresholdParams.createSleepStartBorder(mobilePosition)
                        )
                }

                fun createSleepEndBorder(mobilePosition: MobilePosition) : TimeParams{

                        return TimeParams(
                                before = 4f,
                                after = 1f,
                                threshold = ThresholdParams.createSleepEndBorder(mobilePosition)
                        )
                }

                fun createCleanUp(mobilePosition: MobilePosition) : TimeParams{
                        return TimeParams(
                                before = 4f,
                                after = 4f,
                                threshold = ThresholdParams.createCleanUp(mobilePosition)
                        )
                }

                fun createSleepStartBorder(lightCondition: LightConditions) : TimeParams{

                        return TimeParams(
                                before = 1f,
                                after = 1f,
                                threshold = ThresholdParams.createSleepStartBorder(lightCondition)
                        )
                }

                fun createSleepEndBorder(lightCondition: LightConditions) : TimeParams{

                        return TimeParams(
                                before = 1f,
                                after = 1f,
                                threshold = ThresholdParams.createSleepEndBorder(lightCondition)
                        )
                }

                fun createCleanUp(lightCondition: LightConditions) : TimeParams{
                        return TimeParams(
                                before = 1f,
                                after = 1f,
                                threshold = ThresholdParams.createCleanUp(lightCondition)
                        )
                }
        }

}