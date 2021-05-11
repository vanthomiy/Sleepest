package com.doitstudio.sleepest_master.sleepcalculation.ml

import org.junit.Assert.*
import android.content.Context
import androidx.test.platform.app.InstrumentationRegistry
import com.doitstudio.sleepest_master.model.data.SleepState
import com.doitstudio.sleepest_master.sleepcalculation.SleepCalculationDbRepository
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.runBlocking
import org.junit.Before
import org.junit.Test
import com.doitstudio.sleepest_master.sleepcalculation.db.SleepCalculationDatabase
import com.doitstudio.sleepest_master.storage.db.SleepApiRawDataEntity
import com.google.gson.Gson
import java.io.BufferedReader
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.hamcrest.CoreMatchers
import org.hamcrest.CoreMatchers.equalTo
import org.junit.Assert.*

/*
class SleepClassifierTest{

    // this is RoomDatabase
    val scope = GlobalScope

    lateinit var context: Context

    @Test
    fun sleepLifeCalculationTest() = runBlocking {


        val sleepClassifier = SleepClassifier.getHandler(context)

        // region No sleep data inside!
        //db.deleteSleepApiRawData()

        var sleepData = listOf<SleepApiRawDataEntity>()
        var processedSleepData = sleepClassifier.createFeatures(sleepData)
        var sleepState = sleepClassifier.isUserSleeping(processedSleepData)

        assertThat(sleepState, equalTo(SleepState.NONE))

        //db.deleteSleepApiRawData()

        var gson = Gson()
        val jsonFile = context
                .assets
                .open("databases/testdata/SleepValues.json")
                .bufferedReader()
                .use(BufferedReader::readText)


        val sleepTimes = gson.fromJson(jsonFile, Array<Array<SleepApiRawDataEntity>>::class.java).asList()

        sleepData = (sleepTimes[3].filter{it.timestampSeconds < sleepTimes[3][(sleepTimes[3].size / 2).toInt()].timestampSeconds}.toList().reversed())
        processedSleepData = sleepClassifier.createFeatures(sleepData)
        sleepState = sleepClassifier.isUserSleeping(processedSleepData)

        assertThat(sleepState, equalTo(SleepState.SLEEPING))


        sleepData = (sleepTimes[3].toList().reversed())
        processedSleepData = sleepClassifier.createFeatures(sleepData)
        sleepState = sleepClassifier.isUserSleeping(processedSleepData)

        assertThat(sleepState, equalTo(SleepState.AWAKE))

    }
}
*/