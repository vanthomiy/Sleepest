package com.doitstudio.sleepest_master.sleepcalculation.db

import com.doitstudio.sleepest_master.sleepcalculation.model.algorithm.SleepModel
import org.junit.Assert.*

import com.doitstudio.sleepest_master.storage.db.SleepApiRawDataEntity
import com.google.gson.Gson
import org.hamcrest.CoreMatchers.equalTo
import org.junit.Assert.*
import org.junit.Test

class SleepStateModelEntityTest{

    @Test
    fun checkIfStateModelIsPatternTest() {

        var gson = Gson()

        //region assigments

        val falseModelFile = "{\"valuesAwake\":{\"light\":{\"Average\":3.81904764,\"Factor\":1.80952382,\"Max\":6.0,\"Median\":2.0,\"Min\":1.0},\"motion\":{\"Average\":5.0,\"Factor\":1.08125,\"Max\":6.0,\"Median\":6.0,\"Min\":4.0},\"sleep\":{\"Average\":36.9629631,\"Factor\":3.86666679,\"Max\":93.0,\"Median\":37.0,\"Min\":5.0}},\"valuesDiff\":{\"light\":{\"Average\":2.61904764,\"Factor\":0.8095238,\"Max\":5.0,\"Median\":1.0,\"Min\":0.0},\"motion\":{\"Average\":4.54347825,\"Factor\":-0.07976699,\"Max\":2.0,\"Median\":5.0,\"Min\":3.0},\"sleep\":{\"Average\":-50.54873,\"Factor\":2.88326335,\"Max\":-3.0,\"Median\":-55.0,\"Min\":-1.0}},\"valuesSleep\":{\"light\":{\"Average\":1.0,\"Factor\":1.0,\"Max\":1.0,\"Median\":1.0,\"Min\":1.0},\"motion\":{\"Average\":2.322034,\"Factor\":1.875,\"Max\":6.0,\"Median\":2.0,\"Min\":1.0},\"sleep\":{\"Average\":92.175,\"Factor\":0.991129041,\"Max\":97.0,\"Median\":94.0,\"Min\":83.0}}}"
        val trueModelFile = "{\"valuesAwake\":{\"light\":{\"Average\":3.61904764,\"Factor\":1.80952382,\"Max\":6.0,\"Median\":2.0,\"Min\":1.0},\"motion\":{\"Average\":5.0,\"Factor\":1.08125,\"Max\":6.0,\"Median\":6.0,\"Min\":4.0},\"sleep\":{\"Average\":36.9629631,\"Factor\":3.86666679,\"Max\":93.0,\"Median\":37.0,\"Min\":5.0}},\"valuesDiff\":{\"light\":{\"Average\":2.61904764,\"Factor\":0.8095238,\"Max\":5.0,\"Median\":1.0,\"Min\":0.0},\"motion\":{\"Average\":3.54347825,\"Factor\":-0.07976699,\"Max\":2.0,\"Median\":5.0,\"Min\":3.0},\"sleep\":{\"Average\":-50.54873,\"Factor\":2.88326335,\"Max\":-3.0,\"Median\":-55.0,\"Min\":-1.0}},\"valuesSleep\":{\"light\":{\"Average\":1.0,\"Factor\":1.0,\"Max\":1.0,\"Median\":1.0,\"Min\":1.0},\"motion\":{\"Average\":2.322034,\"Factor\":1.875,\"Max\":6.0,\"Median\":2.0,\"Min\":1.0},\"sleep\":{\"Average\":92.175,\"Factor\":0.991129041,\"Max\":97.0,\"Median\":94.0,\"Min\":83.0}}}"
        val modelEntityFile = "{\"id\":\"TOMANYSLEEPNORMAL\",\"sleepStateModelMax\":{\"valuesAwake\":{\"light\":{\"Average\":3.61904764,\"Factor\":1.80952382,\"Max\":6.0,\"Median\":2.0,\"Min\":1.0},\"motion\":{\"Average\":5.0,\"Factor\":1.08125,\"Max\":6.0,\"Median\":6.0,\"Min\":4.0},\"sleep\":{\"Average\":36.9629631,\"Factor\":3.86666679,\"Max\":93.0,\"Median\":37.0,\"Min\":5.0}},\"valuesDiff\":{\"light\":{\"Average\":2.61904764,\"Factor\":0.8095238,\"Max\":5.0,\"Median\":1.0,\"Min\":0.0},\"motion\":{\"Average\":3.54347825,\"Factor\":-0.07976699,\"Max\":2.0,\"Median\":5.0,\"Min\":3.0},\"sleep\":{\"Average\":-50.54873,\"Factor\":2.88326335,\"Max\":-3.0,\"Median\":-55.0,\"Min\":-1.0}},\"valuesSleep\":{\"light\":{\"Average\":1.0,\"Factor\":1.0,\"Max\":1.0,\"Median\":1.0,\"Min\":1.0},\"motion\":{\"Average\":2.322034,\"Factor\":1.875,\"Max\":6.0,\"Median\":2.0,\"Min\":1.0},\"sleep\":{\"Average\":92.175,\"Factor\":0.991129041,\"Max\":97.0,\"Median\":94.0,\"Min\":83.0}}},\"sleepStateModelMin\":{\"valuesAwake\":{\"light\":{\"Average\":1.33333337,\"Factor\":0.7592593,\"Max\":2.0,\"Median\":1.0,\"Min\":1.0},\"motion\":{\"Average\":4.325,\"Factor\":0.740310133,\"Max\":6.0,\"Median\":4.0,\"Min\":1.0},\"sleep\":{\"Average\":10.1428576,\"Factor\":0.998999,\"Max\":31.0,\"Median\":5.0,\"Min\":1.0}},\"valuesDiff\":{\"light\":{\"Average\":0.333333373,\"Factor\":-0.240740716,\"Max\":1.0,\"Median\":0.0,\"Min\":0.0},\"motion\":{\"Average\":2.002966,\"Factor\":-1.13468981,\"Max\":0.0,\"Median\":2.0,\"Min\":0.0},\"sleep\":{\"Average\":-78.38256,\"Factor\":0.0269918442,\"Max\":-64.0,\"Median\":-88.0,\"Min\":-82.0}},\"valuesSleep\":{\"light\":{\"Average\":1.0,\"Factor\":1.0,\"Max\":1.0,\"Median\":1.0,\"Min\":1.0},\"motion\":{\"Average\":1.45652175,\"Factor\":1.161017,\"Max\":4.0,\"Median\":1.0,\"Min\":1.0},\"sleep\":{\"Average\":85.42373,\"Factor\":0.9417598,\"Max\":95.0,\"Median\":88.0,\"Min\":5.0}}},\"sleepStatePattern\":\"TOMANYSLEEP\",\"userFactorPattern\":\"NORMAL\"}"

        //endregion

        val trueModel = gson.fromJson(trueModelFile, SleepModel::class.java)
        val falseModel = gson.fromJson(falseModelFile, SleepModel::class.java)
        val modelEntity = gson.fromJson(modelEntityFile, SleepStateModelEntity::class.java)


        // ...then the result should be the expected one.
        assertThat(modelEntity.checkIfIsModel(trueModel, 0.025f), equalTo("TOMANYSLEEPNORMAL"))
        assertThat(modelEntity.checkIfIsModel(falseModel, 0.025f), equalTo(""))

    }

    @Test
    fun extendStateModelByModelTest(){

        var gson = Gson()

        //region assigments

        val addFirstModelFile = "{\"valuesAwake\":{\"light\":{\"Average\":6.023256,\"Factor\":1.20588231,\"Max\":6.0,\"Median\":6.0,\"Min\":1.0},\"motion\":{\"Average\":6.0,\"Factor\":1.0,\"Max\":6.0,\"Median\":6.0,\"Min\":6.0},\"sleep\":{\"Average\":21.17647,\"Factor\":1.90641713,\"Max\":82.0,\"Median\":19.0,\"Min\":14.0}},\"valuesDiff\":{\"light\":{\"Average\":3.9988656,\"Factor\":0.205882311,\"Max\":3.0,\"Median\":5.0,\"Min\":0.0},\"motion\":{\"Average\":3.87894731,\"Factor\":0.192982435,\"Max\":2.0,\"Median\":5.0,\"Min\":5.0},\"sleep\":{\"Average\":-63.42353,\"Factor\":0.9117344,\"Max\":-13.0,\"Median\":-68.0,\"Min\":-46.0}},\"valuesSleep\":{\"light\":{\"Average\":1.02439022,\"Factor\":1.02439022,\"Max\":2.0,\"Median\":1.0,\"Min\":1.0},\"motion\":{\"Average\":3.97142863,\"Factor\":1.967742,\"Max\":5.0,\"Median\":3.0,\"Min\":1.0},\"sleep\":{\"Average\":91.51613,\"Factor\":0.9946827,\"Max\":95.0,\"Median\":91.0,\"Min\":83.0}}}"
        val addSecondModelFile = "{\"valuesAwake\":{\"light\":{\"Average\":1.0,\"Factor\":0.8372093,\"Max\":1.0,\"Median\":1.0,\"Min\":1.0},\"motion\":{\"Average\":3.41860461,\"Factor\":0.8333333,\"Max\":6.0,\"Median\":4.0,\"Min\":1.0},\"sleep\":{\"Average\":11.3023252,\"Factor\":1.11455107,\"Max\":27.0,\"Median\":9.0,\"Min\":0.0}},\"valuesDiff\":{\"light\":{\"Average\":0.0,\"Factor\":-0.187180936,\"Max\":0.0,\"Median\":0.0,\"Min\":0.0},\"motion\":{\"Average\":1.01836061,\"Factor\":-1.13440871,\"Max\":1.0,\"Median\":2.0,\"Min\":0.0},\"sleep\":{\"Average\":-72.86841,\"Factor\":0.142137289,\"Max\":-68.0,\"Median\":-80.0,\"Min\":-82.0}},\"valuesSleep\":{\"light\":{\"Average\":1.0,\"Factor\":1.0,\"Max\":1.0,\"Median\":1.0,\"Min\":1.0},\"motion\":{\"Average\":1.967742,\"Factor\":0.807017565,\"Max\":3.0,\"Median\":1.0,\"Min\":1.0},\"sleep\":{\"Average\":84.6,\"Factor\":0.871081555,\"Max\":95.0,\"Median\":87.0,\"Min\":56.0}}}"
        val startEntityFile = "{\"id\":\"TOMANYDEEPNORMAL\",\"sleepStateModelMax\":{\"valuesAwake\":{\"light\":{\"Average\":5.023256,\"Factor\":1.20588231,\"Max\":6.0,\"Median\":6.0,\"Min\":1.0},\"motion\":{\"Average\":6.0,\"Factor\":1.0,\"Max\":6.0,\"Median\":6.0,\"Min\":6.0},\"sleep\":{\"Average\":21.17647,\"Factor\":1.90641713,\"Max\":82.0,\"Median\":19.0,\"Min\":14.0}},\"valuesDiff\":{\"light\":{\"Average\":3.9988656,\"Factor\":0.205882311,\"Max\":4.0,\"Median\":5.0,\"Min\":0.0},\"motion\":{\"Average\":3.57894731,\"Factor\":0.192982435,\"Max\":2.0,\"Median\":5.0,\"Min\":5.0},\"sleep\":{\"Average\":-63.42353,\"Factor\":0.9117344,\"Max\":-13.0,\"Median\":-68.0,\"Min\":-46.0}},\"valuesSleep\":{\"light\":{\"Average\":1.02439022,\"Factor\":1.02439022,\"Max\":2.0,\"Median\":1.0,\"Min\":1.0},\"motion\":{\"Average\":2.97142863,\"Factor\":1.967742,\"Max\":5.0,\"Median\":3.0,\"Min\":1.0},\"sleep\":{\"Average\":90.51613,\"Factor\":0.9946827,\"Max\":95.0,\"Median\":91.0,\"Min\":83.0}}},\"sleepStateModelMin\":{\"valuesAwake\":{\"light\":{\"Average\":1.0,\"Factor\":0.8372093,\"Max\":1.0,\"Median\":1.0,\"Min\":1.0},\"motion\":{\"Average\":3.41860461,\"Factor\":0.8333333,\"Max\":6.0,\"Median\":4.0,\"Min\":1.0},\"sleep\":{\"Average\":12.3023252,\"Factor\":1.11455107,\"Max\":27.0,\"Median\":9.0,\"Min\":0.0}},\"valuesDiff\":{\"light\":{\"Average\":0.0,\"Factor\":-0.187180936,\"Max\":0.0,\"Median\":0.0,\"Min\":0.0},\"motion\":{\"Average\":1.02836061,\"Factor\":-1.13440871,\"Max\":1.0,\"Median\":2.0,\"Min\":0.0},\"sleep\":{\"Average\":-72.86841,\"Factor\":0.142137289,\"Max\":-68.0,\"Median\":-80.0,\"Min\":-82.0}},\"valuesSleep\":{\"light\":{\"Average\":1.0,\"Factor\":1.0,\"Max\":1.0,\"Median\":1.0,\"Min\":1.0},\"motion\":{\"Average\":1.967742,\"Factor\":0.807017565,\"Max\":4.0,\"Median\":1.0,\"Min\":1.0},\"sleep\":{\"Average\":84.6,\"Factor\":0.971081555,\"Max\":95.0,\"Median\":87.0,\"Min\":56.0}}},\"sleepStatePattern\":\"TOMANYDEEP\",\"userFactorPattern\":\"NORMAL\"}"
        val finalEntityFile = "{\"id\":\"TOMANYDEEPNORMAL\",\"sleepStateModelMax\":{\"valuesAwake\":{\"light\":{\"Average\":6.023256,\"Factor\":1.20588231,\"Max\":6.0,\"Median\":6.0,\"Min\":1.0},\"motion\":{\"Average\":6.0,\"Factor\":1.0,\"Max\":6.0,\"Median\":6.0,\"Min\":6.0},\"sleep\":{\"Average\":21.17647,\"Factor\":1.90641713,\"Max\":82.0,\"Median\":19.0,\"Min\":14.0}},\"valuesDiff\":{\"light\":{\"Average\":3.9988656,\"Factor\":0.205882311,\"Max\":4.0,\"Median\":5.0,\"Min\":0.0},\"motion\":{\"Average\":3.87894731,\"Factor\":0.192982435,\"Max\":2.0,\"Median\":5.0,\"Min\":5.0},\"sleep\":{\"Average\":-63.42353,\"Factor\":0.9117344,\"Max\":-13.0,\"Median\":-68.0,\"Min\":-46.0}},\"valuesSleep\":{\"light\":{\"Average\":1.02439022,\"Factor\":1.02439022,\"Max\":2.0,\"Median\":1.0,\"Min\":1.0},\"motion\":{\"Average\":3.97142863,\"Factor\":1.967742,\"Max\":5.0,\"Median\":3.0,\"Min\":1.0},\"sleep\":{\"Average\":91.51613,\"Factor\":0.9946827,\"Max\":95.0,\"Median\":91.0,\"Min\":83.0}}},\"sleepStateModelMin\":{\"valuesAwake\":{\"light\":{\"Average\":1.0,\"Factor\":0.8372093,\"Max\":1.0,\"Median\":1.0,\"Min\":1.0},\"motion\":{\"Average\":3.41860461,\"Factor\":0.8333333,\"Max\":6.0,\"Median\":4.0,\"Min\":1.0},\"sleep\":{\"Average\":11.3023252,\"Factor\":1.11455107,\"Max\":27.0,\"Median\":9.0,\"Min\":0.0}},\"valuesDiff\":{\"light\":{\"Average\":0.0,\"Factor\":-0.187180936,\"Max\":0.0,\"Median\":0.0,\"Min\":0.0},\"motion\":{\"Average\":1.01836061,\"Factor\":-1.13440871,\"Max\":1.0,\"Median\":2.0,\"Min\":0.0},\"sleep\":{\"Average\":-72.86841,\"Factor\":0.142137289,\"Max\":-68.0,\"Median\":-80.0,\"Min\":-82.0}},\"valuesSleep\":{\"light\":{\"Average\":1.0,\"Factor\":1.0,\"Max\":1.0,\"Median\":1.0,\"Min\":1.0},\"motion\":{\"Average\":1.967742,\"Factor\":0.807017565,\"Max\":3.0,\"Median\":1.0,\"Min\":1.0},\"sleep\":{\"Average\":84.6,\"Factor\":0.871081555,\"Max\":95.0,\"Median\":87.0,\"Min\":56.0}}},\"sleepStatePattern\":\"TOMANYDEEP\",\"userFactorPattern\":\"NORMAL\"}"

        //endregion

        val addFirstModel = gson.fromJson(addFirstModelFile, SleepModel::class.java)
        val addSecondModel = gson.fromJson(addSecondModelFile, SleepModel::class.java)

        val startEntity = gson.fromJson(startEntityFile, SleepStateModelEntity::class.java)
        val finalEntity = gson.fromJson(finalEntityFile, SleepStateModelEntity::class.java)

        startEntity.extendModelByModel(addFirstModel)
        startEntity.extendModelByModel(addSecondModel)

        // ...then the result should be the expected one.
        assertThat(startEntity, equalTo(finalEntity))

    }

}