package com.doitstudio.sleepest_master.sleepcalculation

import org.junit.Assert.*
import android.content.Context
import androidx.test.platform.app.InstrumentationRegistry
import com.doitstudio.sleepest_master.model.data.SleepDataFrequency
import com.doitstudio.sleepest_master.model.data.SleepState
import com.doitstudio.sleepest_master.storage.db.SleepApiRawDataEntity
import org.hamcrest.CoreMatchers
import org.junit.Before
import org.junit.Test


class SleepCalculationHandlerUnitTest{

    @Test
    fun getFrequencyFromListByHoursTest(){

        val actualtime = 1000000
        val twohours = 60 * 60  * 2
        val onehour = 60 * 60  * 1
        val sleepCalculationHandler = SleepCalculationHandler.getHandler(this as Context)

        var sleepList = mutableListOf<SleepApiRawDataEntity>()


        // no data available
        var result = sleepCalculationHandler.getFrequencyFromListByHours(2, false, actualtime, sleepList.toList())
        assertThat(result, CoreMatchers.equalTo(SleepDataFrequency.NONE))


        // add some data that is not in the last two hours
        for(i in 0..twohours step 10)
        {
            val data = SleepApiRawDataEntity(actualtime-twohours-i, 1,2,3,sleepState = SleepState.AWAKE)
            sleepList.add(data)
        }

        result = sleepCalculationHandler.getFrequencyFromListByHours(2, false, actualtime, sleepList.toList())
        assertThat(result, CoreMatchers.equalTo(SleepDataFrequency.NONE))


        sleepList.clear()

        // add some data that is not in the last two hours ( 45 mins per file)
        for(i in 0..twohours step 45)
        {
            val data = SleepApiRawDataEntity(actualtime-i, 1,2,3,sleepState = SleepState.AWAKE)
            sleepList.add(data)
        }

        result = sleepCalculationHandler.getFrequencyFromListByHours(2, false, actualtime, sleepList.toList())
        assertThat(result, CoreMatchers.equalTo(SleepDataFrequency.THIRTY))

        sleepList.clear()

        // add some data that is not in the last two hours ( 25 mins per file)
        for(i in 0..twohours step 25)
        {
            val data = SleepApiRawDataEntity(actualtime-i, 1,2,3,sleepState = SleepState.AWAKE)
            sleepList.add(data)
        }

        result = sleepCalculationHandler.getFrequencyFromListByHours(2, false, actualtime, sleepList.toList())
        assertThat(result, CoreMatchers.equalTo(SleepDataFrequency.TEN))

        sleepList.clear()

        // add some data that is not in the last two hours ( 8 mins per file)
        for(i in 0..twohours step 8)
        {
            val data = SleepApiRawDataEntity(actualtime-i, 1,2,3,sleepState = SleepState.AWAKE)
            sleepList.add(data)
        }

        result = sleepCalculationHandler.getFrequencyFromListByHours(2, false, actualtime, sleepList.toList())
        assertThat(result, CoreMatchers.equalTo(SleepDataFrequency.FIVE))

        sleepList.clear()

        // add some data that is not in the last two hours ( 8 mins per file) and plus and minus time
        for(i in -twohours/2..twohours/2 step 8)
        {
            val data = SleepApiRawDataEntity(actualtime-i, 1,2,3,sleepState = SleepState.AWAKE)
            sleepList.add(data)
        }

        result = sleepCalculationHandler.getFrequencyFromListByHours(1, true, actualtime, sleepList.toList())
        assertThat(result, CoreMatchers.equalTo(SleepDataFrequency.FIVE))


    }



}