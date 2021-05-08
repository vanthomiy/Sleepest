package com.doitstudio.sleepest_master.sleepcalculation.model.algorithm

import org.junit.Assert.*


import com.google.gson.Gson
import org.hamcrest.CoreMatchers.equalTo
import org.junit.Test

class SleepTimeParameterTest{

    @Test
    fun multiplyTimeParameterTest() {

        var gson = Gson()

        //region assigments

        val parameter1File = "{\"awakeTime\":1800,\"sleepTime\":3000,\"wakeUpTime\":5400,\"sleepSleepBorder\":55.0,\"awakeSleepBorder\":10.0,\"sleepMotionBorder\":4.0,\"awakeMotionBorder\":0.0,\"sleepMedianOverTime\":75.0,\"diffSleep\":50.0,\"diffSleepFuture\":0.0,\"awakeMedianOverTime\":20.0,\"diffAwake\":-5.0,\"modelMatchPercentage\":97.0}"
        val parameter2File = "{\"awakeTime\":1,\"sleepTime\":1,\"wakeUpTime\":1,\"sleepSleepBorder\":0.75,\"awakeSleepBorder\":2.0,\"sleepMotionBorder\":1.0,\"awakeMotionBorder\":1.0,\"sleepMedianOverTime\":1.0,\"diffSleep\":1.5,\"diffSleepFuture\":1.0,\"awakeMedianOverTime\":1.0,\"diffAwake\":1.0,\"modelMatchPercentage\":1.0}"
        val parameterFinalFile = "{\"awakeTime\":1800,\"sleepTime\":3000,\"wakeUpTime\":5400,\"sleepSleepBorder\":41.25,\"awakeSleepBorder\":20.0,\"sleepMotionBorder\":4.0,\"awakeMotionBorder\":0.0,\"sleepMedianOverTime\":75.0,\"diffSleep\":75.0,\"diffSleepFuture\":0.0,\"awakeMedianOverTime\":20.0,\"diffAwake\":-5.0,\"modelMatchPercentage\":97.0}"

        //endregion

        val param1 = gson.fromJson(parameter1File, SleepTimeParameter::class.java)
        val param2 = gson.fromJson(parameter2File, SleepTimeParameter::class.java)
        val parameterFinal = gson.fromJson(parameterFinalFile, SleepTimeParameter::class.java)

        val finalParamCalc = SleepTimeParameter.multiplyParameterByParameter(param1, param2)


        // ...then the result should be the expected one.
        assertThat(parameterFinal, equalTo(finalParamCalc))
    }

    @Test
    fun mergeTimeParameterTest(){

        var gson = Gson()

        //region assigments

        val param1File = "{\"awakeTime\":1800,\"sleepTime\":3000,\"wakeUpTime\":5400,\"sleepSleepBorder\":55.0,\"awakeSleepBorder\":10.0,\"sleepMotionBorder\":4.0,\"awakeMotionBorder\":0.0,\"sleepMedianOverTime\":75.0,\"diffSleep\":50.0,\"diffSleepFuture\":0.0,\"awakeMedianOverTime\":20.0,\"diffAwake\":-5.0,\"modelMatchPercentage\":97.0}"
        val param2File = "{\"awakeTime\":1800,\"sleepTime\":2000,\"wakeUpTime\":5400,\"sleepSleepBorder\":30.0,\"awakeSleepBorder\":15.0,\"sleepMotionBorder\":4.0,\"awakeMotionBorder\":0.0,\"sleepMedianOverTime\":75.0,\"diffSleep\":55.0,\"diffSleepFuture\":0.0,\"awakeMedianOverTime\":30.0,\"diffAwake\":-5.0,\"modelMatchPercentage\":97.0}"
        val paramFinalFile = "{\"awakeTime\":1800,\"sleepTime\":2000,\"wakeUpTime\":5400,\"sleepSleepBorder\":30.0,\"awakeSleepBorder\":10.0,\"sleepMotionBorder\":4.0,\"awakeMotionBorder\":0.0,\"sleepMedianOverTime\":75.0,\"diffSleep\":55.0,\"diffSleepFuture\":0.0,\"awakeMedianOverTime\":20.0,\"diffAwake\":-5.0,\"modelMatchPercentage\":97.0}"

        //endregion

        val param1 = gson.fromJson(param1File, SleepTimeParameter::class.java)
        val param2 = gson.fromJson(param2File, SleepTimeParameter::class.java)
        val paramFinal = gson.fromJson(paramFinalFile, SleepTimeParameter::class.java)

        val paramFinalCalc = SleepTimeParameter.mergeParameters(listOf(param1, param2))

        // ...then the result should be the expected one.
        assertThat(paramFinal, equalTo(paramFinalCalc))
    }

}