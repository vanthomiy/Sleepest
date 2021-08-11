package com.doitstudio.sleepest_master.storage.db

import androidx.room.Entity
import com.doitstudio.sleepest_master.model.data.LightConditions
import com.doitstudio.sleepest_master.sleepcalculation.model.ThresholdParams

/**
 * Stores the param values for the algorithm
 * It stores different values for each
 */
@Entity
data class SleepStatesParams(

        /**
         * The utc timestamp in seconds when the first user sleep of the sleep session is detected
         */
        var lightSleepParams: ThresholdParams,

        /**
         * The utc timestamp in seconds when the first user sleep of the sleep session is detected
         */
        var deepSleepParams: ThresholdParams,

        /**
         * The utc timestamp in seconds when the first user sleep of the sleep session is detected
         */
        var remSleepParams: ThresholdParams,

        )
{

        fun mergeParameters(factorParams:SleepStatesParams){
                lightSleepParams.mergeParameters(factorParams = factorParams.lightSleepParams)
                deepSleepParams.mergeParameters(factorParams = factorParams.deepSleepParams)
                remSleepParams.mergeParameters(factorParams = factorParams.remSleepParams)
        }

        companion object{
                fun createDefaultParams() : SleepStatesParams {
                        return SleepStatesParams(
                                lightSleepParams = ThresholdParams.createLightSleepBorder(),
                                deepSleepParams = ThresholdParams.createDeepSleepBorder(),
                                remSleepParams = ThresholdParams.createRemSleepBorder(),
                        )
                }

                fun createLightConditionParams(lightCondition : LightConditions ) : SleepStatesParams {
                        return SleepStatesParams(
                                lightSleepParams = ThresholdParams.createLightSleepBorder(lightCondition),
                                deepSleepParams = ThresholdParams.createDeepSleepBorder(lightCondition),
                                remSleepParams = ThresholdParams.createRemSleepBorder(lightCondition),
                        )
                }

        }
}