package com.doitstudio.sleepest_master.sleepcalculation.ml

import org.junit.Assert.*
import android.content.Context
import androidx.test.platform.app.InstrumentationRegistry
import com.doitstudio.sleepest_master.model.data.ModelProcess
import com.doitstudio.sleepest_master.model.data.SleepDataFrequency
import com.doitstudio.sleepest_master.model.data.SleepState
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.runBlocking
import org.junit.Before
import org.junit.Test
import com.doitstudio.sleepest_master.storage.db.SleepApiRawDataEntity
import org.hamcrest.CoreMatchers.equalTo
class SleepClassifierTest{

    // this is RoomDatabase
    val scope = GlobalScope

    lateinit var context: Context

    @Before
    fun init(){
        context = InstrumentationRegistry.getInstrumentation().targetContext
    }

    @Test
    fun loadInputAssigmentFileTest() = runBlocking {


        val sleepClassifier = SleepClassifier.getHandler(context)


        var assigments = sleepClassifier.loadInputAssignmentFile(ModelProcess.SLEEP04, sleepDataFrequency = SleepDataFrequency.FIVE)
        assertThat(assigments.count() != 0, equalTo(true))


        assigments = sleepClassifier.loadInputAssignmentFile(ModelProcess.SLEEP04, sleepDataFrequency = SleepDataFrequency.TEN)
        assertThat(assigments.count() != 0, equalTo(true))

        assigments = sleepClassifier.loadInputAssignmentFile(ModelProcess.SLEEP04, sleepDataFrequency = SleepDataFrequency.THIRTY)
        assertThat(assigments.count() != 0, equalTo(true))

        assigments = sleepClassifier.loadInputAssignmentFile(ModelProcess.SLEEP12, sleepDataFrequency = SleepDataFrequency.FIVE)
        assertThat(assigments.count() != 0, equalTo(true))

        assigments = sleepClassifier.loadInputAssignmentFile(ModelProcess.SLEEP12, sleepDataFrequency = SleepDataFrequency.TEN)
        assertThat(assigments.count() != 0, equalTo(true))

        assigments = sleepClassifier.loadInputAssignmentFile(ModelProcess.SLEEP12, sleepDataFrequency = SleepDataFrequency.THIRTY)
        assertThat(assigments.count() != 0, equalTo(true))

        assigments = sleepClassifier.loadInputAssignmentFile(ModelProcess.LIGHTAWAKE, sleepDataFrequency = SleepDataFrequency.FIVE)
        assertThat(assigments.count() != 0, equalTo(true))

        assigments = sleepClassifier.loadInputAssignmentFile(ModelProcess.LIGHTAWAKE, sleepDataFrequency = SleepDataFrequency.TEN)
        assertThat(assigments.count() != 0, equalTo(true))

        assigments = sleepClassifier.loadInputAssignmentFile(ModelProcess.LIGHTAWAKE, sleepDataFrequency = SleepDataFrequency.THIRTY)
        assertThat(assigments.count() != 0, equalTo(true))

        assigments = sleepClassifier.loadInputAssignmentFile(ModelProcess.TABLEBED, sleepDataFrequency = SleepDataFrequency.NONE)
        assertThat(assigments.count() != 0, equalTo(true))
    }

    @Test
    fun createFeaturesTest() = runBlocking {


        val sleepClassifier = SleepClassifier.getHandler(context)

        var sleepList = mutableListOf<SleepApiRawDataEntity>()

        // add some data that is not in the last two hours ( 45 mins per file)
        for(i in 0..10000 step (45*60))
        {
            val data = SleepApiRawDataEntity(10000-i, 1,2,3,sleepState = SleepState.AWAKE)
            sleepList.add(data)
        }


        var assigments = sleepClassifier.createFeatures(sleepList, ModelProcess.SLEEP04, sleepDataFrequency = SleepDataFrequency.FIVE)
        assertThat(assigments.count() != 0, equalTo(true))

        assigments = sleepClassifier.createFeatures(sleepList, ModelProcess.SLEEP04, sleepDataFrequency = SleepDataFrequency.TEN)
        assertThat(assigments.count() != 0, equalTo(true))

        assigments = sleepClassifier.createFeatures(sleepList, ModelProcess.SLEEP04, sleepDataFrequency = SleepDataFrequency.THIRTY)
        assertThat(assigments.count() != 0, equalTo(true))

        assigments = sleepClassifier.createFeatures(sleepList, ModelProcess.SLEEP12, sleepDataFrequency = SleepDataFrequency.FIVE)
        assertThat(assigments.count() != 0, equalTo(true))

        assigments = sleepClassifier.createFeatures(sleepList, ModelProcess.SLEEP12, sleepDataFrequency = SleepDataFrequency.TEN)
        assertThat(assigments.count() != 0, equalTo(true))

        assigments = sleepClassifier.createFeatures(sleepList, ModelProcess.SLEEP12, sleepDataFrequency = SleepDataFrequency.THIRTY)
        assertThat(assigments.count() != 0, equalTo(true))

        assigments = sleepClassifier.createFeatures(sleepList, ModelProcess.LIGHTAWAKE, sleepDataFrequency = SleepDataFrequency.FIVE)
        assertThat(assigments.count() != 0, equalTo(true))

        assigments = sleepClassifier.createFeatures(sleepList, ModelProcess.LIGHTAWAKE, sleepDataFrequency = SleepDataFrequency.TEN)
        assertThat(assigments.count() != 0, equalTo(true))

        assigments = sleepClassifier.createFeatures(sleepList, ModelProcess.LIGHTAWAKE, sleepDataFrequency = SleepDataFrequency.THIRTY)
        assertThat(assigments.count() != 0, equalTo(true))

        // now we need to calc the values to provide...
        var light = IntArray(4)
        light[0] = sleepList.maxOf { x->x.light }
        light[1] = sleepList.minOf { x->x.light }
        light[2] = sleepList.sumOf { x->x.light } / sleepList.count()
        light[3] = sleepList.sortedBy { x-> x.light }[sleepList.count()/2].light

        var motion = IntArray(4)
        motion[0] = sleepList.maxOf { x->x.motion }
        motion[1] = sleepList.minOf { x->x.motion }
        motion[2] = sleepList.sumOf { x->x.motion } / sleepList.count()
        motion[3] = sleepList.sortedBy { x-> x.motion }[sleepList.count()/2].motion

        var sleep = IntArray(4)
        sleep[0] = sleepList.maxOf { x->x.confidence }
        sleep[1] = sleepList.minOf { x->x.confidence }
        sleep[2] = sleepList.sumOf { x->x.confidence } / sleepList.count()
        sleep[3] = sleepList.sortedBy { x-> x.confidence }[sleepList.count()/2].confidence


        assigments = sleepClassifier.createTableFeatures(light, motion, sleep)
        assertThat(assigments.count() != 0, equalTo(true))
    }



}
