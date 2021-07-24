package com.doitstudio.sleepest_master.storage.db

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.doitstudio.sleepest_master.model.data.LightConditions
import com.doitstudio.sleepest_master.model.data.MobilePosition
import com.doitstudio.sleepest_master.model.data.MobileUseFrequency
import com.doitstudio.sleepest_master.model.data.SleepState
import com.doitstudio.sleepest_master.sleepcalculation.model.ThresholdParams
import com.doitstudio.sleepest_master.sleepcalculation.model.TimeParams
import com.doitstudio.sleepest_master.storage.db.UserSleepSessionEntity
import com.google.android.material.transition.MaterialContainerTransform

/**
 * Stores the param values for the algorithm
 * It stores different values for each
 */
@Entity
data class AlgorithmParams(

        /**
         * The utc timestamp in seconds when the first user sleep of the sleep session is detected
         */
        var sleepStartBorder: TimeParams,

        /**
         * The utc timestamp in seconds when the first user sleep of the sleep session is detected
         */
        var sleepEndBorder: TimeParams,

        /**
         * The utc timestamp in seconds when the first user sleep of the sleep session is detected
         */
        var sleepCleanUp: TimeParams,

        /**
         * The utc timestamp in seconds when the first user sleep of the sleep session is detected
         */
        var generalThreshold: ThresholdParams,

        )
{

        fun mergeParameters(factorParams:AlgorithmParams){
                sleepStartBorder.mergeParameters(factorParams = factorParams.sleepStartBorder)
                sleepEndBorder.mergeParameters(factorParams = factorParams.sleepEndBorder)
                sleepCleanUp.mergeParameters(factorParams = factorParams.sleepCleanUp)
                generalThreshold.mergeParameters(factorParams = factorParams.generalThreshold)
        }

        companion object{
                fun createDefaultParams(mobilePosition: MobilePosition) : AlgorithmParams {
                        return AlgorithmParams(
                                sleepStartBorder = TimeParams.createSleepStartBorder(mobilePosition),
                                sleepEndBorder = TimeParams.createSleepEndBorder(mobilePosition),
                                sleepCleanUp = TimeParams.createCleanUp(mobilePosition),
                                generalThreshold = ThresholdParams.createGeneralThreshold(mobilePosition)
                        )
                }

                fun createLightConditionParams(lightCondition : LightConditions ) : AlgorithmParams {
                        return AlgorithmParams(
                                sleepStartBorder = TimeParams.createSleepStartBorder(lightCondition),
                                sleepEndBorder = TimeParams.createSleepEndBorder(lightCondition),
                                sleepCleanUp = TimeParams.createCleanUp(lightCondition),
                                generalThreshold = ThresholdParams.createGeneralThreshold(lightCondition)
                        )
                }

                fun createMobileUseFrequencyParams(mobileUseFrequency: MobileUseFrequency) : AlgorithmParams {
                        return AlgorithmParams(
                                sleepStartBorder = TimeParams.createSleepStartBorder(mobileUseFrequency),
                                sleepEndBorder = TimeParams.createSleepEndBorder(mobileUseFrequency),
                                sleepCleanUp = TimeParams.createCleanUp(mobileUseFrequency),
                                generalThreshold = ThresholdParams.createGeneralThreshold(mobileUseFrequency)
                        )
                }

        }
}