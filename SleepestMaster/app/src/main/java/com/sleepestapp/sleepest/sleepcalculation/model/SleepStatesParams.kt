package com.sleepestapp.sleepest.sleepcalculation.model

import androidx.room.Entity
import com.sleepestapp.sleepest.model.data.LightConditions
import com.sleepestapp.sleepest.model.data.SleepState

/**
 * Stores the param values for the algorithm
 * It stores different values for each
 */
@Entity
data class SleepStatesParams(

        /**
         * It is used to create a [ThresholdParams] to define a users [SleepState.LIGHT] phase
         */
        var lightSleepParams: ThresholdParams,

        /**
         * It is used to create a [ThresholdParams] to define a users [SleepState.DEEP] phase
         */
        var deepSleepParams: ThresholdParams,

        /**
         * It is used to create a [ThresholdParams] to define a users [SleepState.REM] phase
         */
        var remSleepParams: ThresholdParams,

        )
{

        /**
         * Merge this [SleepStatesParams] with another factor [SleepStatesParams] by multiplying
         */
        fun mergeParameters(factorParams: SleepStatesParams){
                lightSleepParams.mergeParameters(factorParams = factorParams.lightSleepParams)
                deepSleepParams.mergeParameters(factorParams = factorParams.deepSleepParams)
                remSleepParams.mergeParameters(factorParams = factorParams.remSleepParams)
        }

        companion object{

                /**
                 * Helper function to create default [SleepStatesParams]
                 */
                fun createDefaultParams() : SleepStatesParams {
                        return SleepStatesParams(
                                lightSleepParams = ThresholdParams.createLightSleepBorder(),
                                deepSleepParams = ThresholdParams.createDeepSleepBorder(),
                                remSleepParams = ThresholdParams.createRemSleepBorder(),
                        )
                }

                /**
                 * Helper function to create light condition factor [LightConditions]
                 */
                fun createLightConditionParams(lightCondition : LightConditions ) : SleepStatesParams {
                        return SleepStatesParams(
                                lightSleepParams = ThresholdParams.createLightSleepBorder(lightCondition),
                                deepSleepParams = ThresholdParams.createDeepSleepBorder(lightCondition),
                                remSleepParams = ThresholdParams.createRemSleepBorder(lightCondition),
                        )
                }

        }
}