package com.doitstudio.sleepest_master

import android.content.Context
import androidx.datastore.createDataStore
import androidx.lifecycle.asLiveData
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.doitstudio.sleepest_master.MainApplication
import com.doitstudio.sleepest_master.googleapi.SleepHandler
import com.doitstudio.sleepest_master.storage.DataStoreRepository
import com.doitstudio.sleepest_master.storage.DatabaseRepository
import com.doitstudio.sleepest_master.storage.datastorage.ACTIVITY_API_DATA_NAME
import com.doitstudio.sleepest_master.storage.datastorage.ActivityApiDataSerializer
import com.doitstudio.sleepest_master.storage.datastorage.ActivityApiDataStatus
import com.doitstudio.sleepest_master.util.TimeConverterUtil
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import org.hamcrest.CoreMatchers
import org.junit.Assert
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import java.util.concurrent.Callable
import java.util.concurrent.TimeUnit
//import org.awaitility.kotlin.await
import java.time.Duration
import org.junit.Assert.assertEquals

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
