package com.doitstudio.sleepest_master.storage.db

import androidx.room.Entity
import com.doitstudio.sleepest_master.model.data.LightConditions
import com.doitstudio.sleepest_master.model.data.MobilePosition
import com.doitstudio.sleepest_master.model.data.MobileUseFrequency
import com.doitstudio.sleepest_master.sleepcalculation.model.ThresholdParams

/**
 * Stores the param values for the algorithm
 * It stores different values for each
 */
@Entity
data class SleepingParams(

        /**
         * It is used to create a [ThresholdParams] to detect a users sleep started
         */
        var sleepStartBorder: ThresholdParams,

        /**
         * It is used to create a [ThresholdParams] to detect a users sleep started by comparing the data before and after the actual time
         */
        var sleepStartThreshold: ThresholdParams,

        /**
         * It is used to create a [ThresholdParams] to check if the data over a specific time is over a specific threshold
         */
        var sleepCleanUp: ThresholdParams,

        /**
         * It is used to create a [ThresholdParams] to check if the actual data is over a specific threshold
         */
        var generalThreshold: ThresholdParams,

        )
{
        /**
         * Merge this [SleepingParams] with another factor [SleepingParams] by multiplying
         */
        fun mergeParameters(factorParams:SleepingParams){
                sleepStartBorder.mergeParameters(factorParams = factorParams.sleepStartBorder)
                sleepStartThreshold.mergeParameters(factorParams = factorParams.sleepStartThreshold)
                sleepCleanUp.mergeParameters(factorParams = factorParams.sleepCleanUp)
                generalThreshold.mergeParameters(factorParams = factorParams.generalThreshold)
        }

        companion object{

                /**
                 * Helper function to create default [SleepingParams]
                 */
                fun createDefaultParams(mobilePosition: MobilePosition) : SleepingParams {
                        return SleepingParams(
                                sleepStartBorder = ThresholdParams.createSleepStartBorder(mobilePosition),
                                sleepStartThreshold = ThresholdParams.createSleepStartThreshold(mobilePosition),
                                sleepCleanUp = ThresholdParams.createCleanUp(mobilePosition),
                                generalThreshold = ThresholdParams.createGeneralThreshold(mobilePosition)
                        )
                }

                /**
                 * Helper function to create light condition factor [SleepingParams]
                 */
                fun createLightConditionParams(lightCondition : LightConditions ) : SleepingParams {
                        return SleepingParams(
                                sleepStartBorder = ThresholdParams.createSleepStartBorder(lightCondition),
                                sleepStartThreshold = ThresholdParams.createSleepStartThreshold(lightCondition),
                                sleepCleanUp = ThresholdParams.createCleanUp(lightCondition),
                                generalThreshold = ThresholdParams.createGeneralThreshold(lightCondition)
                        )
                }

                /**
                 * Helper function to create mobile use frequency factor [SleepingParams]
                 */
                fun createMobileUseFrequencyParams(mobileUseFrequency: MobileUseFrequency) : SleepingParams {
                        return SleepingParams(
                                sleepStartBorder = ThresholdParams.createSleepStartBorder(mobileUseFrequency),
                                sleepStartThreshold = ThresholdParams.createSleepStartThreshold(mobileUseFrequency),
                                sleepCleanUp = ThresholdParams.createCleanUp(mobileUseFrequency),
                                generalThreshold = ThresholdParams.createGeneralThreshold(mobileUseFrequency)
                        )
                }



        }
}