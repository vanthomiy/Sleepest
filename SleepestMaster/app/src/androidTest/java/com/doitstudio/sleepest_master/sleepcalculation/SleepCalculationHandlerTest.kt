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

class SleepCalculationHandlerTest
{
    lateinit var context: Context

    @Before
    fun init(){
        context = InstrumentationRegistry.getInstrumentation().targetContext
    }

    @Test
    fun getFrequencyFromListByHoursTest(){

        val actualtime = 1000000
        val twohours = 60 * 60  * 2
        val onehour = 60 * 60  * 1
        val sleepCalculationHandler = SleepCalculationHandler.getHandler(context)

        var sleepList = mutableListOf<SleepApiRawDataEntity>()


        // no data available
        var result = sleepCalculationHandler.getFrequencyFromListByHours(2, false, actualtime, sleepList.toList())
        assertThat(result, CoreMatchers.equalTo(SleepDataFrequency.NONE))


        sleepList.clear()

        // add some data that is not in the last two hours ( 45 mins per file)
        for(i in 0..twohours step (45*60))
        {
            val data = SleepApiRawDataEntity(actualtime-i, 1,2,3,sleepState = SleepState.AWAKE)
            sleepList.add(data)
        }

        result = sleepCalculationHandler.getFrequencyFromListByHours(2, false, actualtime, sleepList.toList())
        assertThat(result, CoreMatchers.equalTo(SleepDataFrequency.THIRTY))

        sleepList.clear()

        // add some data that is not in the last two hours ( 25 mins per file)
        for(i in 0..twohours step (25*60))
        {
            val data = SleepApiRawDataEntity(actualtime-i, 1,2,3,sleepState = SleepState.AWAKE)
            sleepList.add(data)
        }

        result = sleepCalculationHandler.getFrequencyFromListByHours(2, false, actualtime, sleepList.toList())
        assertThat(result, CoreMatchers.equalTo(SleepDataFrequency.TEN))

        sleepList.clear()

        // add some data that is not in the last two hours ( 8 mins per file)
        for(i in 0..twohours step (8*60))
        {
            val data = SleepApiRawDataEntity(actualtime-i, 1,2,3,sleepState = SleepState.AWAKE)
            sleepList.add(data)
        }

        result = sleepCalculationHandler.getFrequencyFromListByHours(2, false, actualtime, sleepList.toList())
        assertThat(result, CoreMatchers.equalTo(SleepDataFrequency.FIVE))

        sleepList.clear()

        // add some data that is not in the last two hours ( 8 mins per file) and plus and minus time
        for(i in -twohours/2..twohours/2 step (8*60))
        {
            val data = SleepApiRawDataEntity(actualtime-i, 1,2,3,sleepState = SleepState.AWAKE)
            sleepList.add(data)
        }

        result = sleepCalculationHandler.getFrequencyFromListByHours(1, true, actualtime, sleepList.toList())
        assertThat(result, CoreMatchers.equalTo(SleepDataFrequency.FIVE))

    }

    @Test
    fun createTimeNormedDataTest(){

        val actualtime = 1000000
        val twohours = 60 * 60  * 2
        val onehour = 60 * 60  * 1
        val sleepCalculationHandler = SleepCalculationHandler.getHandler(context)

        var sleepList = mutableListOf<SleepApiRawDataEntity>()


        var (normedSleepApiData, frequency) = sleepCalculationHandler.createTimeNormedData(2, false, actualtime, sleepList)

        assertThat(normedSleepApiData.count(), CoreMatchers.equalTo(0))
        assertThat(frequency, CoreMatchers.equalTo(SleepDataFrequency.NONE))

        sleepList.clear()

        // add some data that is not in the last two hours ( 45 mins per file)
        for(i in 0..twohours step (45*60))
        {
            val data = SleepApiRawDataEntity(actualtime-i, 1,2,3,sleepState = SleepState.AWAKE)
            sleepList.add(data)
        }

        var (normedSleepApiData1, frequency1) = sleepCalculationHandler.createTimeNormedData(2, false, actualtime, sleepList)

        assertThat(normedSleepApiData1.count(), CoreMatchers.equalTo(4))
        assertThat(frequency1, CoreMatchers.equalTo(SleepDataFrequency.THIRTY))

        sleepList.clear()

        // add some data that is not in the last two hours ( 25 mins per file)
        for(i in 0..twohours step (25*60))
        {
            val data = SleepApiRawDataEntity(actualtime-i, 1,2,3,sleepState = SleepState.AWAKE)
            sleepList.add(data)
        }

        var (normedSleepApiData2, frequency2) = sleepCalculationHandler.createTimeNormedData(2, false, actualtime, sleepList)

        assertThat(normedSleepApiData2.count(), CoreMatchers.equalTo(12))
        assertThat(frequency2, CoreMatchers.equalTo(SleepDataFrequency.TEN))

        sleepList.clear()

        // add some data that is not in the last two hours ( 8 mins per file)
        for(i in 0..twohours step (8*60))
        {
            val data = SleepApiRawDataEntity(actualtime-i, 1,2,3,sleepState = SleepState.AWAKE)
            sleepList.add(data)
        }

        var (normedSleepApiData3, frequency3) = sleepCalculationHandler.createTimeNormedData(2, false, actualtime, sleepList)

        assertThat(normedSleepApiData3.count(), CoreMatchers.equalTo(24))
        assertThat(frequency3, CoreMatchers.equalTo(SleepDataFrequency.FIVE))

        sleepList.clear()

        // add some data that is not in the last two hours ( 8 mins per file) and plus and minus time
        for(i in -twohours/2..twohours/2 step (8*60))
        {
            val data = SleepApiRawDataEntity(actualtime-i, 1,2,3,sleepState = SleepState.AWAKE)
            sleepList.add(data)
        }

        var (normedSleepApiData4, frequency4) = sleepCalculationHandler.createTimeNormedData(2, false, actualtime, sleepList)

        assertThat(normedSleepApiData4.count(), CoreMatchers.equalTo(24))
        assertThat(frequency4, CoreMatchers.equalTo(SleepDataFrequency.FIVE))


    }

}