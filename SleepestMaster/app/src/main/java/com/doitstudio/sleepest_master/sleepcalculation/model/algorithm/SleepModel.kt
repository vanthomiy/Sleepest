package com.doitstudio.sleepest_master.sleepcalculation.model.algorithm

import androidx.room.ColumnInfo
import androidx.room.Embedded
import androidx.room.PrimaryKey
import androidx.room.Relation
import com.doitstudio.sleepest_master.model.data.SleepStatePattern
import com.doitstudio.sleepest_master.storage.db.SleepApiRawDataEntity
import java.lang.Exception


data class SleepModel(

    @Embedded(prefix = "awake") var valuesAwake:ValuesTimeModel = ValuesTimeModel(),
    @Embedded(prefix = "sleep") var valuesSleep:ValuesTimeModel= ValuesTimeModel(),
    @Embedded(prefix = "diff") var valuesDiff:ValuesTimeModel= ValuesTimeModel(),

    ) {

    companion object{

        /**
         *  Creates a new model with a lost o f sleep api data
         */
        fun calculateModel(awakeList:List<SleepApiRawDataEntity>, sleepList:List<SleepApiRawDataEntity>): SleepModel {

            val awake = ValuesTimeModel.calculateModel(awakeList)
            val sleep = ValuesTimeModel.calculateModel(sleepList)
            val diff =  ValuesTimeModel.calculateModelDiff(awake, sleep)
            return SleepModel(
                    awake,
                    sleep,
                    diff
            )
        }
    }

    /**
     *  Checks if the sleep matches a model
     */
    fun checkIfInBounds(sleepModel:SleepModel, greater:Boolean) : Int{
        var times = 0

        times += valuesAwake.checkIfInBounds(sleepModel.valuesAwake, greater)
        times += valuesSleep.checkIfInBounds(sleepModel.valuesSleep, greater)
        times += valuesDiff.checkIfInBounds(sleepModel.valuesDiff, greater)

        return times
    }

}
data class ValuesTimeModel(
    @Embedded(prefix = "sleep") val sleep:DataSetter = DataSetter(),
    @Embedded(prefix = "light") val light:DataSetter = DataSetter(),
    @Embedded(prefix = "motion") val motion:DataSetter = DataSetter(),
)
{
    companion object {

        /**
         *  Creates a new model with a lost o f sleep api data
         */
        fun calculateModel(list: List<SleepApiRawDataEntity>): ValuesTimeModel {
            return ValuesTimeModel(
                    DataSetter.calculateSleep(list),
                    DataSetter.calculateMotion(list),
                    DataSetter.calculateLight(list)
            )
        }

        /**
         *  Creates a new model with diff between awake and sleep
         */
        fun calculateModelDiff(awake:ValuesTimeModel, sleep:ValuesTimeModel): ValuesTimeModel {
            return ValuesTimeModel(
                    DataSetter.calculateDifference(awake.sleep, sleep.sleep),
                    DataSetter.calculateDifference(awake.motion, sleep.motion),
                    DataSetter.calculateDifference(awake.light, sleep.light)
            )
        }
    }

    /**
     *  Checks if the sleep matches a model
     */
    fun checkIfInBounds(timeModel:ValuesTimeModel, greater:Boolean) : Int{
        var times = 0

        if(!sleep.checkIfInBounds(timeModel.sleep, greater))
            times++
        if(!light.checkIfInBounds(timeModel.light, greater))
            times++
        if(!motion.checkIfInBounds(timeModel.motion, greater))
            times++

        return times
    }
}

data class DataSetter(

    @ColumnInfo(name = "max")
    val Max:Float = 1000f,	//Der Max wert
    @ColumnInfo(name = "min")
    val Min:Float = 0f,	//Der Min wert
    @ColumnInfo(name = "median")
    val Median:Float = 0f,	//Der Median wert
    @ColumnInfo(name = "average")
    val Average:Float = 0f,	//Der Average wert
    @ColumnInfo(name = "factor")
    val Factor:Float = 0f,	//Der Factor wert

)
{
    companion object {

        /**
         *  Creates a new model of sleep
         */
        fun calculateSleep(list: List<SleepApiRawDataEntity>): DataSetter {

            val max = list.maxOf { x-> x.confidence }
            val min = list.minOf { x-> x.confidence }
            val average = (list.sumOf { x-> x.confidence }) / list.count()
            val median = list.sortedBy { x-> x.confidence }[list.count()/2].confidence
            var factor = 1
            if (median != 0)
            {
                factor = average / median
            }

            return DataSetter(
                    max.toFloat(),
                    min.toFloat(),
                    average.toFloat(),
                    median.toFloat(),
                    factor.toFloat()
            )
        }
        /**
         *  Creates a new model of motion
         */
        fun calculateMotion(list: List<SleepApiRawDataEntity>): DataSetter {

            val max = list.maxOf { x-> x.motion }
            val min = list.minOf { x-> x.motion }
            val average = (list.sumOf { x-> x.motion }) / list.count()
            val median = list.sortedBy { x-> x.motion }[list.count()/2].motion
            var factor = 1
            if (median != 0)
            {
                factor = average / median
            }

            return DataSetter(
                    max.toFloat(),
                    min.toFloat(),
                    average.toFloat(),
                    median.toFloat(),
                    factor.toFloat()
            )
        }
        /**
         *  Creates a new model of light
         */
        fun calculateLight(list: List<SleepApiRawDataEntity>): DataSetter {

            val max = list.maxOf { x-> x.light }
            val min = list.minOf { x-> x.light }
            val average = (list.sumOf { x-> x.light }) / list.count()
            val median = list.sortedBy { x-> x.light }[list.count()/2].light
            var factor = 1
            if (median != 0)
            {
                factor = average / median
            }

            return DataSetter(
                    max.toFloat(),
                    min.toFloat(),
                    average.toFloat(),
                    median.toFloat(),
                    factor.toFloat()
            )
        }
        /**
         *  Creates a new model of diffrence
         */
        fun calculateDifference(awake:DataSetter, sleep:DataSetter): DataSetter {

            val max = sleep.Max-awake.Max
            val min = sleep.Min-awake.Min
            val average = sleep.Average-awake.Average
            val median = sleep.Median-awake.Median
            var factor = 1f
            if (median.toInt() != 0)
            {
                factor = average / median
            }

            return DataSetter(
                    max.toFloat(),
                    min.toFloat(),
                    average.toFloat(),
                    median.toFloat(),
                    factor.toFloat()
            )
        }

    }

    /**
     *  Checks if the sleep matches a model
     */
    fun checkIfInBounds(model:DataSetter, greater:Boolean) : Boolean{
        if(greater && model.Max >= Max && model.Min >= Min && model.Median >= Median && model.Average >= Average && model.Factor >= Factor)
        {
            return true
        }
        else if (!greater && model.Max <= Max && model.Min <= Min && model.Median <= Median && model.Average <= Average && model.Factor <= Factor)
        {
            return true
        }

        return false
    }
}
