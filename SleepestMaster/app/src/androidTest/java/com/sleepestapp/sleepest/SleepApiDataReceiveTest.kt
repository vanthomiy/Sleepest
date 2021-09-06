package com.sleepestapp.sleepest

import android.content.Context
import androidx.test.platform.app.InstrumentationRegistry
import com.sleepestapp.sleepest.googleapi.SleepHandler
import com.sleepestapp.sleepest.storage.DataStoreRepository
import com.sleepestapp.sleepest.util.TimeConverterUtil
import kotlinx.coroutines.*
import org.hamcrest.CoreMatchers
import org.junit.Assert
import org.junit.Before
import org.junit.Test
//import org.awaitility.kotlin.await

class SleepApiDataReceiveTest {

    private lateinit var context : Context
    private lateinit var dataStoreRepository: DataStoreRepository
    private lateinit var dbRepository: DataStoreRepository
    private lateinit var sleepHandler : SleepHandler

    private val sleepCalculationStoreRepository by lazy {  DataStoreRepository.getRepo(context)}
    private val userSleepTime by lazy {sleepCalculationStoreRepository.sleepApiDataFlow.asLiveData()}

    @Before
    fun init(){
        context = InstrumentationRegistry.getInstrumentation().targetContext
        dataStoreRepository = DataStoreRepository(context)
        dbRepository = DataStoreRepository(context)
        sleepHandler = SleepHandler.getHandler(context)
       /* sleepDbRepository = DatabaseRepository.getRepo(
            sleepCalcDatabase.sleepApiRawDataDao(),
            sleepCalcDatabase.userSleepSessionDao(),
            sleepCalcDatabase.alarmDao(),
            sleepCalcDatabase.activityApiRawDataDao()

        )*/

    }

    @Test
    fun testReceiveApiData() = runBlocking {

        val a : IntArray = intArrayOf(7,30)

        val b = TimeConverterUtil.minuteToTimeFormat(450)
        Assert.assertThat(a, CoreMatchers.equalTo(b))

        //dataStoreRepository.updateSleepIsSubscribed(false)
        //Assert.assertThat(dataStoreRepository.getSleepSubscribeStatus(), CoreMatchers.equalTo(false))

        sleepHandler.startSleepHandler()
        //delay(8000)
        Assert.assertThat(dataStoreRepository.getSleepSubscribeStatus(), CoreMatchers.equalTo(true))

        sleepHandler.stopSleepHandler()
        //delay(8000)
        Assert.assertThat(dataStoreRepository.getSleepSubscribeStatus(), CoreMatchers.equalTo(false))


    }

}
