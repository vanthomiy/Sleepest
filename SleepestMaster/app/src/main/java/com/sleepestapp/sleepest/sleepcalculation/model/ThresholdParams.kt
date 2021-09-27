package com.sleepestapp.sleepest.sleepcalculation.model

import com.sleepestapp.sleepest.model.data.LightConditions
import com.sleepestapp.sleepest.model.data.MobilePosition
import com.sleepestapp.sleepest.model.data.MobileUseFrequency
import com.sleepestapp.sleepest.model.data.SleepState

/**
 * Stores the param values for the algorithm
 * It stores different values for each
 */
data class ThresholdParams(

        /**
         * The confidence threshold of the actual [ThresholdParams]
         */
        var confidence: Float = 0f,

        /**
         * The motion threshold of the actual [ThresholdParams]
         */
        var motion: Float = 0f,

        /**
         * The light threshold of the actual [ThresholdParams]
         */
        var light: Float = 0f,

){
        /**
         * For comparing the abs difference between two [ThresholdParams] we can call this function
         */
        fun absBetweenThresholds(paramsToSubtract : ThresholdParams){
                confidence = kotlin.math.abs(confidence - paramsToSubtract.confidence)
                light = kotlin.math.abs(light - paramsToSubtract.light)
                motion = kotlin.math.abs(motion - paramsToSubtract.motion)
        }

        /**
         * Checks whether a threshold is over or under another given threshold.
         * [isAbove] = true: We check if the passed threshold is above the actual one
         * [neededCount] = indicates how many of the 3 parameters have to match the condition
         * [motion] is checked positive
         * [light] and [motion] is checked negative
         */
        fun checkIfThreshold(isAbove:Boolean, neededCount:Int, paramsToCheck : ThresholdParams) : Boolean{
                var countTrue = 0

                if(isAbove){
                        countTrue += if(paramsToCheck.confidence >= confidence) 1 else 0
                        countTrue += if(paramsToCheck.motion <= motion) 1 else 0
                        countTrue += if(paramsToCheck.light <= light) 1 else 0
                }
                else {
                        countTrue += if(paramsToCheck.confidence <= confidence) 1 else 0
                        countTrue += if(paramsToCheck.motion >= motion) 1 else 0
                        countTrue += if(paramsToCheck.light >= light) 1 else 0
                }

                return (countTrue >= neededCount)
        }

        /**
         * Checks whether a threshold is over or under another given threshold.
         * [isAbove] = true: We check if the passed threshold is above the actual one
         * [neededCount] = indicates how many of the 3 parameters have to match the condition
         * [motion] is checked positive
         * [light] and [motion] are also checked positive
         */
        fun checkIfDifferenceThreshold(isAbove:Boolean, neededCount:Int, paramsToCheck : ThresholdParams) : Boolean{
                var countTrue = 0

                if(isAbove){
                        countTrue += if(paramsToCheck.confidence >= confidence) 1 else 0
                        countTrue += if(paramsToCheck.motion >= motion) 1 else 0
                        countTrue += if(paramsToCheck.light >= light) 1 else 0
                }
                else {
                        countTrue += if(paramsToCheck.confidence <= confidence) 1 else 0
                        countTrue += if(paramsToCheck.motion <= motion) 1 else 0
                        countTrue += if(paramsToCheck.light <= light) 1 else 0
                }

                return (countTrue >= neededCount)
        }

        /**
         * Merge this [ThresholdParams] with another factor [ThresholdParams] by multiplying
         */
        fun mergeParameters(factorParams: ThresholdParams){
                confidence *= factorParams.confidence
                motion *= factorParams.motion
                light *= factorParams.light
        }

        companion object{

                /**
                 * Helper function to create sleep start border  [ThresholdParams]
                 */
                fun createSleepStartBorder(mobilePosition: MobilePosition) : ThresholdParams{
                        return ThresholdParams(
                                confidence = when(mobilePosition){
                                        MobilePosition.INBED -> 10f
                                        else -> 20f
                                },
                                motion = when(mobilePosition){
                                        MobilePosition.INBED -> 0f
                                        else -> 1f
                                },
                                light = when(mobilePosition){
                                        MobilePosition.INBED -> 0f
                                        else -> 0f
                                }
                        )
                }

                /**
                 * Helper function to create sleep start threshold  [ThresholdParams]
                 */
                fun createSleepStartThreshold(mobilePosition: MobilePosition) : ThresholdParams{
                        return ThresholdParams(
                                confidence = when(mobilePosition){
                                        MobilePosition.INBED -> 35f
                                        else -> 45f
                                },
                                motion = when(mobilePosition){
                                        MobilePosition.INBED -> 2f
                                        else -> 2f
                                },
                                light = when(mobilePosition){
                                        MobilePosition.INBED -> 6f
                                        else -> 6f
                                }
                        )
                }

                /**
                 * Helper function to create sleep clean up over time  [ThresholdParams]
                 */
                fun createCleanUp(mobilePosition: MobilePosition) : ThresholdParams{
                        return ThresholdParams(
                                confidence = when(mobilePosition){
                                        MobilePosition.INBED -> 15f
                                        else -> 20f
                                },
                                motion = when(mobilePosition){
                                        MobilePosition.INBED -> 3f
                                        else -> 2f
                                },
                                light = when(mobilePosition){
                                        MobilePosition.INBED -> 3f
                                        else -> 3f
                                }
                        )
                }

                /**
                 * Helper function to create general sleep threshold for a specific time [ThresholdParams]
                 */
                fun createGeneralThreshold(mobilePosition: MobilePosition) : ThresholdParams{
                        return ThresholdParams(
                                confidence = when(mobilePosition){
                                        MobilePosition.INBED -> 35f
                                        else -> 45f
                                },
                                motion = when(mobilePosition){
                                        MobilePosition.INBED -> 5f
                                        else -> 2f
                                },
                                light = when(mobilePosition){
                                        MobilePosition.INBED -> 4f
                                        else -> 4f
                                }
                        )
                }

                /**
                 * Helper function to create sleep start border factor by [LightConditions]
                 */
                fun createSleepStartBorder(lightConditions: LightConditions) : ThresholdParams{
                        return ThresholdParams(
                                confidence = when(lightConditions){
                                        LightConditions.DARK -> 1f
                                        else -> 0.9f
                                },
                                motion = when(lightConditions){
                                        LightConditions.DARK -> 1f
                                        else -> 1f
                                },
                                light = when(lightConditions){
                                        LightConditions.DARK -> 1f
                                        else -> 0.9f
                                }
                        )
                }

                /**
                 * Helper function to create sleep start threshold factor by [LightConditions]
                 */
                fun createSleepStartThreshold(lightConditions: LightConditions) : ThresholdParams{
                        return ThresholdParams(
                                confidence = when(lightConditions){
                                        LightConditions.DARK -> 1f
                                        else -> 1f
                                },
                                motion = when(lightConditions){
                                        LightConditions.DARK -> 1f
                                        else -> 1f
                                },
                                light = when(lightConditions){
                                        LightConditions.DARK -> 1f
                                        else -> 1f
                                }
                        )
                }

                /**
                 * Helper function to create sleep cleanup over time factor by [LightConditions]
                 */
                fun createCleanUp(lightConditions: LightConditions) : ThresholdParams{
                        return ThresholdParams(
                                confidence = when(lightConditions){
                                        LightConditions.DARK -> 1f
                                        else -> 0.9f
                                },
                                motion = when(lightConditions){
                                        LightConditions.DARK -> 1f
                                        else -> 1f
                                },
                                light = when(lightConditions){
                                        LightConditions.DARK -> 1f
                                        else -> 1.1f
                                }
                        )
                }

                /**
                 * Helper function to create sleep general border factor by [LightConditions]
                 */
                fun createGeneralThreshold(lightConditions: LightConditions) : ThresholdParams{
                        return ThresholdParams(
                                confidence = when(lightConditions){
                                        LightConditions.DARK -> 1f
                                        else -> 0.9f
                                },
                                motion = when(lightConditions){
                                        LightConditions.DARK -> 1f
                                        else -> 1f
                                },
                                light = when(lightConditions){
                                        LightConditions.DARK -> 1f
                                        else -> 1.1f
                                }
                        )
                }

                /**
                 * Helper function to create sleep start border factor by [MobileUseFrequency]
                 */
                fun createSleepStartBorder(mobileUseFrequency: MobileUseFrequency) : ThresholdParams{
                        return ThresholdParams(
                                confidence = when(mobileUseFrequency){
                                        MobileUseFrequency.VERYLESS -> 0.5f
                                        MobileUseFrequency.LESS -> 0.7f
                                        MobileUseFrequency.OFTEN -> 0.9f
                                        MobileUseFrequency.VERYOFTEN -> 1f
                                        else -> 1f
                                },
                                motion = when(mobileUseFrequency){
                                        MobileUseFrequency.VERYLESS -> 0.5f
                                        MobileUseFrequency.LESS -> 0.7f
                                        MobileUseFrequency.OFTEN -> 0.9f
                                        MobileUseFrequency.VERYOFTEN -> 1f
                                        else -> 1f
                                },
                                light = when(mobileUseFrequency){
                                        MobileUseFrequency.VERYLESS -> 1f
                                        MobileUseFrequency.LESS -> 1f
                                        MobileUseFrequency.OFTEN -> 1f
                                        MobileUseFrequency.VERYOFTEN -> 1f
                                        else -> 1f
                                }
                        )
                }

                /**
                 * Helper function to create sleep start threshold factor by [MobileUseFrequency]
                 * Not needed for [MobileUseFrequency] at the moment
                 */
                fun createSleepStartThreshold(mobileUseFrequency: MobileUseFrequency) : ThresholdParams{
                        return ThresholdParams(
                                confidence = 1f,
                                motion = 1f,
                                light = 1f
                        )
                }

                /**
                 * Helper function to create sleep cleanup over time factor by [MobileUseFrequency]
                 * Not needed for [MobileUseFrequency] at the moment
                 */
                fun createCleanUp(mobileUseFrequency: MobileUseFrequency) : ThresholdParams{
                        return ThresholdParams(
                                confidence = when(mobileUseFrequency){
                                        MobileUseFrequency.VERYLESS -> 1f
                                        MobileUseFrequency.LESS -> 1f
                                        MobileUseFrequency.OFTEN -> 1f
                                        MobileUseFrequency.VERYOFTEN -> 1f
                                        else -> 1f
                                },
                                motion = when(mobileUseFrequency){
                                        MobileUseFrequency.VERYLESS -> 1f
                                        MobileUseFrequency.LESS -> 1f
                                        MobileUseFrequency.OFTEN -> 1f
                                        MobileUseFrequency.VERYOFTEN -> 1f
                                        else -> 1f
                                },
                                light = when(mobileUseFrequency){
                                        MobileUseFrequency.VERYLESS -> 1f
                                        MobileUseFrequency.LESS -> 1f
                                        MobileUseFrequency.OFTEN -> 1f
                                        MobileUseFrequency.VERYOFTEN -> 1f
                                        else -> 1f
                                }
                        )
                }

                /**
                 * Helper function to create sleep general border factor by [MobileUseFrequency]
                 * Not needed for [MobileUseFrequency] at the moment
                 */
                fun createGeneralThreshold(mobileUseFrequency: MobileUseFrequency) : ThresholdParams{
                        return ThresholdParams(
                                confidence = when(mobileUseFrequency){
                                        MobileUseFrequency.VERYLESS -> 1f
                                        MobileUseFrequency.LESS -> 1f
                                        MobileUseFrequency.OFTEN -> 1f
                                        MobileUseFrequency.VERYOFTEN -> 1f
                                        else -> 1f
                                },
                                motion = when(mobileUseFrequency){
                                        MobileUseFrequency.VERYLESS -> 1f
                                        MobileUseFrequency.LESS -> 1f
                                        MobileUseFrequency.OFTEN -> 1f
                                        MobileUseFrequency.VERYOFTEN -> 1f
                                        else -> 1f
                                },
                                light = when(mobileUseFrequency){
                                        MobileUseFrequency.VERYLESS -> 1f
                                        MobileUseFrequency.LESS -> 1f
                                        MobileUseFrequency.OFTEN -> 1f
                                        MobileUseFrequency.VERYOFTEN -> 1f
                                        else -> 1f
                                }
                        )
                }

                //endregion

                //region SleepState params

                /**
                 * Light sleep border. Sleep is [SleepState.LIGHT] under this condition
                 */
                fun createLightSleepBorder() : ThresholdParams{
                        return ThresholdParams(
                                confidence = 90f,
                                motion = 2f,
                                light = 2f
                        )
                }

                /**
                 * Deep sleep border. Sleep is [SleepState.DEEP] under this condition
                 */
                fun createDeepSleepBorder() : ThresholdParams{
                        return ThresholdParams(
                                confidence = 90f,
                                motion = 2f,
                                light = 2f
                        )
                }

                /**
                 * Rem sleep border. Sleep is [SleepState.REM] under this condition
                 */
                fun createRemSleepBorder() : ThresholdParams{
                        return ThresholdParams(
                                confidence = 70f,
                                motion = 4f,
                                light = 0f
                        )
                }

                /**
                 * Light sleep border factor for [LightConditions]
                 */
                fun createLightSleepBorder(lightConditions: LightConditions) : ThresholdParams{
                        return ThresholdParams(
                                confidence = when(lightConditions){
                                        LightConditions.DARK -> 1f
                                        LightConditions.LIGHT -> 0.95f
                                        else -> 1f
                                },
                                motion = when(lightConditions){
                                        LightConditions.DARK -> 1f
                                        LightConditions.LIGHT -> 1f
                                        else -> 1f
                                },
                                light = when(lightConditions){
                                        LightConditions.DARK -> 1f
                                        LightConditions.LIGHT -> 1.05f
                                        else -> 1f
                                }
                        )
                }

                /**
                 * Deep sleep border factor for [LightConditions]
                 */
                fun createDeepSleepBorder(lightConditions: LightConditions) : ThresholdParams{
                        return ThresholdParams(
                                confidence = when(lightConditions){
                                        LightConditions.DARK -> 1f
                                        LightConditions.LIGHT -> 0.95f
                                        else -> 1f
                                },
                                motion = when(lightConditions){
                                        LightConditions.DARK -> 1f
                                        LightConditions.LIGHT -> 1f
                                        else -> 1f
                                },
                                light = when(lightConditions){
                                        LightConditions.DARK -> 1f
                                        LightConditions.LIGHT -> 1.05f
                                        else -> 1f
                                }
                        )
                }

                /**
                 * Rem sleep border factor for [LightConditions]
                 */
                fun createRemSleepBorder(lightConditions: LightConditions) : ThresholdParams{
                        return ThresholdParams(
                                confidence = when(lightConditions){
                                        LightConditions.DARK -> 1f
                                        LightConditions.LIGHT -> 1f
                                        else -> 1f
                                },
                                motion = when(lightConditions){
                                        LightConditions.DARK -> 1f
                                        LightConditions.LIGHT -> 1f
                                        else -> 1f
                                },
                                light = when(lightConditions){
                                        LightConditions.DARK -> 1f
                                        LightConditions.LIGHT -> 1.025f
                                        else -> 1f
                                }
                        )
                }

                //endregion
        }
}