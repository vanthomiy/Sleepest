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
         * The utc timestamp in seconds when the first user sleep of the sleep session is detected
         */
        var sleepStartBorder: ThresholdParams,

        /**
         * The utc timestamp in seconds when the first user sleep of the sleep session is detected
         */
        var sleepStartThreshold: ThresholdParams,

        /**
         * The utc timestamp in seconds when the first user sleep of the sleep session is detected
         */
        var sleepCleanUp: ThresholdParams,

        /**
         * The utc timestamp in seconds when the first user sleep of the sleep session is detected
         */
        var generalThreshold: ThresholdParams,

        )
{

        fun mergeParameters(factorParams:SleepingParams){
                sleepStartBorder.mergeParameters(factorParams = factorParams.sleepStartBorder)
                sleepStartThreshold.mergeParameters(factorParams = factorParams.sleepStartThreshold)
                sleepCleanUp.mergeParameters(factorParams = factorParams.sleepCleanUp)
                generalThreshold.mergeParameters(factorParams = factorParams.generalThreshold)
        }

        companion object{
                fun createDefaultParams(mobilePosition: MobilePosition) : SleepingParams {
                        return SleepingParams(
                                sleepStartBorder = ThresholdParams.createSleepStartBorder(mobilePosition),
                                sleepStartThreshold = ThresholdParams.createSleepStartThreshold(mobilePosition),
                                sleepCleanUp = ThresholdParams.createCleanUp(mobilePosition),
                                generalThreshold = ThresholdParams.createGeneralThreshold(mobilePosition)
                        )
                }

                fun createLightConditionParams(lightCondition : LightConditions ) : SleepingParams {
                        return SleepingParams(
                                sleepStartBorder = ThresholdParams.createSleepStartBorder(lightCondition),
                                sleepStartThreshold = ThresholdParams.createSleepStartThreshold(lightCondition),
                                sleepCleanUp = ThresholdParams.createCleanUp(lightCondition),
                                generalThreshold = ThresholdParams.createGeneralThreshold(lightCondition)
                        )
                }

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