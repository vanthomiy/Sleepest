package com.doitstudio.sleepest_master.sleepcalculation.model

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.doitstudio.sleepest_master.model.data.LightConditions
import com.doitstudio.sleepest_master.model.data.MobilePosition
import com.doitstudio.sleepest_master.model.data.MobileUseFrequency
import com.doitstudio.sleepest_master.model.data.SleepState
import com.doitstudio.sleepest_master.storage.db.UserSleepSessionEntity
import java.lang.Math.abs

/**
 * Stores the param values for the algorithm
 * It stores different values for each
 */
data class ThresholdParams(

        /**
         * The utc timestamp in seconds when the first user sleep of the sleep session is detected
         */
        var confidence: Float = 0f,

        /**
         * The utc timestamp in seconds when the last user sleep of the sleep session is detected
         */
        var motion: Float = 0f,

        /**
         * The utc timestamp in seconds when the last user sleep of the sleep session is detected
         */
        var light: Float = 0f,

){
        fun absBetweenTresholds(paramsToSubtract : ThresholdParams){
                confidence = kotlin.math.abs(confidence - paramsToSubtract.confidence)
                light = kotlin.math.abs(light - paramsToSubtract.light)
                motion = kotlin.math.abs(motion - paramsToSubtract.motion)
        }

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

        fun mergeParameters(factorParams: ThresholdParams){
                confidence *= factorParams.confidence
                motion *= factorParams.motion
                light *= factorParams.light
        }

        companion object{

                //region Sleep Params
                fun createSleepStartBorder(mobilePosition: MobilePosition) : ThresholdParams{
                        return ThresholdParams(
                                confidence = when(mobilePosition){
                                        MobilePosition.INBED -> 40f
                                        else -> 50f
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

                fun createSleepEndBorder(mobilePosition: MobilePosition) : ThresholdParams{
                        return ThresholdParams(
                                confidence = when(mobilePosition){
                                        MobilePosition.INBED -> 20f
                                        else -> 50f
                                },
                                motion = when(mobilePosition){
                                        MobilePosition.INBED -> 2f
                                        else -> 2f
                                },
                                light = when(mobilePosition){
                                        MobilePosition.INBED -> 0f
                                        else -> 0f
                                }
                        )
                }

                fun createCleanUp(mobilePosition: MobilePosition) : ThresholdParams{
                        return ThresholdParams(
                                confidence = when(mobilePosition){
                                        MobilePosition.INBED -> 15f
                                        else -> 20f
                                },
                                motion = when(mobilePosition){
                                        MobilePosition.INBED -> 3f
                                        else -> 3f
                                },
                                light = when(mobilePosition){
                                        MobilePosition.INBED -> 3f
                                        else -> 3f
                                }
                        )
                }

                fun createGeneralThreshold(mobilePosition: MobilePosition) : ThresholdParams{
                        return ThresholdParams(
                                confidence = when(mobilePosition){
                                        MobilePosition.INBED -> 35f
                                        else -> 45f
                                },
                                motion = when(mobilePosition){
                                        MobilePosition.INBED -> 4f
                                        else -> 2f
                                },
                                light = when(mobilePosition){
                                        MobilePosition.INBED -> 4f
                                        else -> 4f
                                }
                        )
                }

                fun createSleepStartBorder(lightConditions: LightConditions) : ThresholdParams{
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

                fun createSleepEndBorder(lightConditions: LightConditions) : ThresholdParams{
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

                fun createCleanUp(lightConditions: LightConditions) : ThresholdParams{
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

                fun createGeneralThreshold(lightConditions: LightConditions) : ThresholdParams{
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

                fun createSleepStartBorder(mobileUseFrequency: MobileUseFrequency) : ThresholdParams{
                        return ThresholdParams(
                                confidence = when(mobileUseFrequency){
                                        MobileUseFrequency.VERYLESS -> 0.85f
                                        MobileUseFrequency.LESS -> 0.9f
                                        MobileUseFrequency.OFTEN -> 1.0f
                                        MobileUseFrequency.VERYOFTEN -> 1.1f
                                        else -> 1f
                                },
                                motion = when(mobileUseFrequency){
                                        MobileUseFrequency.VERYLESS -> 0.85f
                                        MobileUseFrequency.LESS -> 0.9f
                                        MobileUseFrequency.OFTEN -> 1.0f
                                        MobileUseFrequency.VERYOFTEN -> 1.1f                                       else -> 1f
                                },
                                light = when(mobileUseFrequency){
                                        MobileUseFrequency.VERYLESS -> 0.85f
                                        MobileUseFrequency.LESS -> 0.9f
                                        MobileUseFrequency.OFTEN -> 1.0f
                                        MobileUseFrequency.VERYOFTEN -> 1.1f                                       else -> 1f
                                }
                        )
                }

                fun createSleepEndBorder(mobileUseFrequency: MobileUseFrequency) : ThresholdParams{
                        return ThresholdParams(
                                confidence = when(mobileUseFrequency){
                                        MobileUseFrequency.VERYLESS -> 0.85f
                                        MobileUseFrequency.LESS -> 0.9f
                                        MobileUseFrequency.OFTEN -> 1.0f
                                        MobileUseFrequency.VERYOFTEN -> 1.1f
                                        else -> 1f
                                },
                                motion = when(mobileUseFrequency){
                                        MobileUseFrequency.VERYLESS -> 0.85f
                                        MobileUseFrequency.LESS -> 0.9f
                                        MobileUseFrequency.OFTEN -> 1.0f
                                        MobileUseFrequency.VERYOFTEN -> 1.1f                                       else -> 1f
                                },
                                light = when(mobileUseFrequency){
                                        MobileUseFrequency.VERYLESS -> 0.85f
                                        MobileUseFrequency.LESS -> 0.9f
                                        MobileUseFrequency.OFTEN -> 1.0f
                                        MobileUseFrequency.VERYOFTEN -> 1.1f                                       else -> 1f
                                }
                        )
                }

                fun createCleanUp(mobileUseFrequency: MobileUseFrequency) : ThresholdParams{
                        return ThresholdParams(
                                confidence = when(mobileUseFrequency){
                                        MobileUseFrequency.VERYLESS -> 0.85f
                                        MobileUseFrequency.LESS -> 0.9f
                                        MobileUseFrequency.OFTEN -> 1.0f
                                        MobileUseFrequency.VERYOFTEN -> 1.1f
                                        else -> 1f
                                },
                                motion = when(mobileUseFrequency){
                                        MobileUseFrequency.VERYLESS -> 0.85f
                                        MobileUseFrequency.LESS -> 0.9f
                                        MobileUseFrequency.OFTEN -> 1.0f
                                        MobileUseFrequency.VERYOFTEN -> 1.1f                                       else -> 1f
                                },
                                light = when(mobileUseFrequency){
                                        MobileUseFrequency.VERYLESS -> 0.85f
                                        MobileUseFrequency.LESS -> 0.9f
                                        MobileUseFrequency.OFTEN -> 1.0f
                                        MobileUseFrequency.VERYOFTEN -> 1.1f                                       else -> 1f
                                }
                        )
                }

                fun createGeneralThreshold(mobileUseFrequency: MobileUseFrequency) : ThresholdParams{
                        return ThresholdParams(
                                confidence = when(mobileUseFrequency){
                                        MobileUseFrequency.VERYLESS -> 0.85f
                                        MobileUseFrequency.LESS -> 0.9f
                                        MobileUseFrequency.OFTEN -> 1.0f
                                        MobileUseFrequency.VERYOFTEN -> 1.1f
                                        else -> 1f
                                },
                                motion = when(mobileUseFrequency){
                                        MobileUseFrequency.VERYLESS -> 0.85f
                                        MobileUseFrequency.LESS -> 0.9f
                                        MobileUseFrequency.OFTEN -> 1.0f
                                        MobileUseFrequency.VERYOFTEN -> 1.1f                                       else -> 1f
                                },
                                light = when(mobileUseFrequency){
                                        MobileUseFrequency.VERYLESS -> 0.85f
                                        MobileUseFrequency.LESS -> 0.9f
                                        MobileUseFrequency.OFTEN -> 1.0f
                                        MobileUseFrequency.VERYOFTEN -> 1.1f                                       else -> 1f
                                }
                        )
                }

                //endregion

                //region SleepState params
                fun createLightSleepBorder() : ThresholdParams{
                        return ThresholdParams(
                                confidence = 85f,
                                motion = 3f,
                                light = 3f
                        )
                }

                fun createDeepSleepBorder() : ThresholdParams{
                        return ThresholdParams(
                                confidence = 95f,
                                motion = 2f,
                                light = 2f
                        )
                }

                fun createRemSleepBorder() : ThresholdParams{
                        return ThresholdParams(
                                confidence = 100f,
                                motion = 1f,
                                light = 1f
                        )
                }

                fun createLightSleepBorder(lightConditions: LightConditions) : ThresholdParams{
                        return ThresholdParams(
                                confidence = when(lightConditions){
                                        LightConditions.DARK -> 1f
                                        LightConditions.LIGHT -> 0.95f
                                        else -> 1f
                                },
                                motion = when(lightConditions){
                                        LightConditions.DARK -> 1f
                                        LightConditions.LIGHT -> 1.15f
                                        else -> 1f
                                },
                                light = when(lightConditions){
                                        LightConditions.DARK -> 1f
                                        LightConditions.LIGHT -> 1.15f
                                        else -> 1f
                                }
                        )
                }

                fun createDeepSleepBorder(lightConditions: LightConditions) : ThresholdParams{
                        return ThresholdParams(
                                confidence = when(lightConditions){
                                        LightConditions.DARK -> 1f
                                        LightConditions.LIGHT -> 0.95f
                                        else -> 1f
                                },
                                motion = when(lightConditions){
                                        LightConditions.DARK -> 1f
                                        LightConditions.LIGHT -> 1.15f
                                        else -> 1f
                                },
                                light = when(lightConditions){
                                        LightConditions.DARK -> 1f
                                        LightConditions.LIGHT -> 1.15f
                                        else -> 1f
                                }
                        )
                }

                fun createRemSleepBorder(lightConditions: LightConditions) : ThresholdParams{
                        return ThresholdParams(
                                confidence = when(lightConditions){
                                        LightConditions.DARK -> 1f
                                        LightConditions.LIGHT -> 0.95f
                                        else -> 1f
                                },
                                motion = when(lightConditions){
                                        LightConditions.DARK -> 1f
                                        LightConditions.LIGHT -> 1.15f
                                        else -> 1f
                                },
                                light = when(lightConditions){
                                        LightConditions.DARK -> 1f
                                        LightConditions.LIGHT -> 1.15f
                                        else -> 1f
                                }
                        )
                }
                //endregion
        }
}