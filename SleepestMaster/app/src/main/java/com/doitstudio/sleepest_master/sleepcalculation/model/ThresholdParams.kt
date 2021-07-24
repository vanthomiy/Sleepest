package com.doitstudio.sleepest_master.sleepcalculation.model

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.doitstudio.sleepest_master.model.data.LightConditions
import com.doitstudio.sleepest_master.model.data.MobilePosition
import com.doitstudio.sleepest_master.model.data.SleepState
import com.doitstudio.sleepest_master.storage.db.UserSleepSessionEntity

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

        fun mergeParameters(factorParams: ThresholdParams){
                confidence *= factorParams.confidence
                motion *= factorParams.motion
                light *= factorParams.light
        }

        companion object{
                fun createSleepStartBorder(mobilePosition: MobilePosition) : ThresholdParams{
                        return ThresholdParams(
                                confidence = when(mobilePosition){
                                        MobilePosition.INBED -> 40f
                                        else -> 50f
                                },
                                motion = when(mobilePosition){
                                        MobilePosition.INBED -> 3f
                                        else -> 3f
                                },
                                light = when(mobilePosition){
                                        MobilePosition.INBED -> 1f
                                        else -> 1f
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
                                        MobilePosition.INBED -> 25f
                                        else -> 35f
                                },
                                motion = when(mobilePosition){
                                        MobilePosition.INBED -> 2f
                                        else -> 2f
                                },
                                light = when(mobilePosition){
                                        MobilePosition.INBED -> 2f
                                        else -> 2f
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
                                        MobilePosition.INBED -> 2f
                                        else -> 2f
                                },
                                light = when(mobilePosition){
                                        MobilePosition.INBED -> 2f
                                        else -> 2f
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
        }
}