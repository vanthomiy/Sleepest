package com.doitstudio.sleepest_master.sleepcalculation.ml

import com.doitstudio.sleepest_master.model.data.LightConditions
import com.doitstudio.sleepest_master.model.data.MobilePosition
import com.doitstudio.sleepest_master.model.data.MobileUseFrequency
import com.doitstudio.sleepest_master.storage.db.AlgorithmParams

class ParamsHandler {

    val algorithmParams : AlgorithmParams by lazy {


    }

    /**
     * With this we are loading the params that are possible best for the calculation
     * Therefore we use the most used params in the last week
     */
    fun LoadDefaultParams() : AlgorithmParams {

    }

    fun initalizeParams(mobilePosition: MobilePosition, lightConditions: LightConditions, mobileUseFrequency: MobileUseFrequency){

        val defaultParams = AlgorithmParams.createDefaultParams(mobilePosition)
        val lightConditionsParams = AlgorithmParams.createLightConditionParams(lightConditions)
        val mobileUseFrequencyParams = AlgorithmParams.createMobileUseFrequencyParams(mobileUseFrequency)

        defaultParams.mergeParameters(lightConditionsParams)
        defaultParams.mergeParameters(mobileUseFrequencyParams)
    }
}