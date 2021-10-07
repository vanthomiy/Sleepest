package com.sleepestapp.sleepest.sleepcalculation

import com.sleepestapp.sleepest.model.data.LightConditions
import com.sleepestapp.sleepest.model.data.MobilePosition
import com.sleepestapp.sleepest.model.data.MobileUseFrequency
import com.sleepestapp.sleepest.sleepcalculation.model.SleepStatesParams
import com.sleepestapp.sleepest.sleepcalculation.model.SleepingParams

object ParamsHandler{

    /**
     * Create the default params by the given conditions
     */
    fun createDefaultParams(
        mobilePosition: MobilePosition,
        lightConditions: LightConditions,
        mobileUseFrequency: MobileUseFrequency
    ): SleepingParams {

        val algorithmParams = SleepingParams.createDefaultParams(mobilePosition)
        val lightConditionsParams = SleepingParams.createLightConditionParams(lightConditions)
        val mobileUseFrequencyParams = SleepingParams.createMobileUseFrequencyParams(mobileUseFrequency)

        algorithmParams.mergeParameters(lightConditionsParams)
        algorithmParams.mergeParameters(mobileUseFrequencyParams)

        return algorithmParams
    }

    /**
     * Create the default sleep state params by the given conditions
     */
    fun createSleepStateParams(
        lightConditions: LightConditions,
    ): SleepStatesParams {

        val algorithmParams = SleepStatesParams.createDefaultParams()
        val lightConditionsParams = SleepStatesParams.createLightConditionParams(lightConditions)

        algorithmParams.mergeParameters(lightConditionsParams)

        return algorithmParams
    }
}