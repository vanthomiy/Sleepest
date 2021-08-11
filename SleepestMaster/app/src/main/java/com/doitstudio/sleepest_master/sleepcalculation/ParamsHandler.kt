package com.doitstudio.sleepest_master.sleepcalculation

import com.doitstudio.sleepest_master.model.data.LightConditions
import com.doitstudio.sleepest_master.model.data.MobilePosition
import com.doitstudio.sleepest_master.model.data.MobileUseFrequency
import com.doitstudio.sleepest_master.storage.db.SleepStatesParams
import com.doitstudio.sleepest_master.storage.db.SleepingParams

object ParamsHandler{


    fun createDefaultParams(
        mobilePosition: MobilePosition,
        lightConditions: LightConditions,
        mobileUseFrequency: MobileUseFrequency
    ): SleepingParams{

        val algorithmParams = SleepingParams.createDefaultParams(mobilePosition)
        val lightConditionsParams = SleepingParams.createLightConditionParams(lightConditions)
        val mobileUseFrequencyParams = SleepingParams.createMobileUseFrequencyParams(mobileUseFrequency)

        algorithmParams.mergeParameters(lightConditionsParams)
        algorithmParams.mergeParameters(mobileUseFrequencyParams)

        return algorithmParams
    }

    fun createSleepStateParams(
        lightConditions: LightConditions,
    ): SleepStatesParams{

        val algorithmParams = SleepStatesParams.createDefaultParams()
        val lightConditionsParams = SleepStatesParams.createLightConditionParams(lightConditions)

        algorithmParams.mergeParameters(lightConditionsParams)

        return algorithmParams
    }
}