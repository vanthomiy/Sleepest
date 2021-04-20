package com.doitstudio.sleepest_master.sleepcalculation.model.algorithm

import com.google.gson.Gson
import org.hamcrest.CoreMatchers
import org.junit.Assert.*
import org.junit.Test

class SleepStateParameterTest{

    @Test
    fun mergeStateParameterTest(){

        var gson = Gson()

        //region assignments

        val param1File = "{\"sleepSleepBorder\":25.0,\"deepSleepSleepBorder\":90.0,\"remSleepSleepBorder\":95.0,\"sleepMotionBorder\":4.0,\"deepSleepMotionBorder\":3.0,\"remSleepMotionBorder\":1.0,\"sleepLightBorder\":4.0,\"deepSleepLightBorder\":3.0,\"remSleepLightBorder\":1.0,\"soundClearSleepBorder\":7.0,\"soundClearMotionBorder\":1.0,\"modelMatchPercentage\":98.0}"
        val param2File = "{\"sleepSleepBorder\":30.0,\"deepSleepSleepBorder\":93.5,\"remSleepSleepBorder\":95.0,\"sleepMotionBorder\":5.0,\"deepSleepMotionBorder\":3.0,\"remSleepMotionBorder\":1.0,\"sleepLightBorder\":4.0,\"deepSleepLightBorder\":2.0,\"remSleepLightBorder\":1.0,\"soundClearSleepBorder\":7.0,\"soundClearMotionBorder\":1.0,\"modelMatchPercentage\":98.0}"
        val paramFinalFile = "{\"sleepSleepBorder\":30.0,\"deepSleepSleepBorder\":93.5,\"remSleepSleepBorder\":95.0,\"sleepMotionBorder\":4.0,\"deepSleepMotionBorder\":3.0,\"remSleepMotionBorder\":1.0,\"sleepLightBorder\":4.0,\"deepSleepLightBorder\":2.0,\"remSleepLightBorder\":1.0,\"soundClearSleepBorder\":7.0,\"soundClearMotionBorder\":1.0,\"modelMatchPercentage\":98.0}"

        //endregion

        val param1 = gson.fromJson(param1File, SleepStateParameter::class.java)
        val param2 = gson.fromJson(param2File, SleepStateParameter::class.java)
        val paramFinal = gson.fromJson(paramFinalFile, SleepStateParameter::class.java)

        val paramFinalCalc = SleepStateParameter.mergeParameters(listOf(param1, param2))

        // ...then the result should be the expected one.
        assertThat(paramFinal, CoreMatchers.equalTo(paramFinalCalc))
    }

}