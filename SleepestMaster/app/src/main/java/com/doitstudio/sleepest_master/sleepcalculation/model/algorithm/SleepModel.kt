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
            val diff =  ValuesTimeModel.calculateModelDiff(sleep, awake)
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
    fun checkIfInBounds(sleepModel:SleepModel, greater:Boolean, accuracy:Float) : Int{
        var times = 0

        times += valuesAwake.checkIfInBounds(sleepModel.valuesAwake, greater, accuracy)
        times += valuesSleep.checkIfInBounds(sleepModel.valuesSleep, greater, accuracy)
        times += valuesDiff.checkIfInBounds(sleepModel.valuesDiff, greater, accuracy)

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
                    DataSetter.calculateLight(list),
                    DataSetter.calculateMotion(list)
            )
        }

        /**
         *  Creates a new model with diff between awake and sleep
         */
        fun calculateModelDiff(awake:ValuesTimeModel, sleep:ValuesTimeModel): ValuesTimeModel {
            return ValuesTimeModel(
                    DataSetter.calculateDifference(awake.sleep, sleep.sleep),
                    DataSetter.calculateDifference(awake.light, sleep.light),
                    DataSetter.calculateDifference(awake.motion, sleep.motion)

            )
        }
    }

    /**
     *  Checks if the sleep matches a model
     */
    fun checkIfInBounds(timeModel:ValuesTimeModel, greater:Boolean, accuracy:Float) : Int{
        var times = 0

        times += sleep.checkIfInBounds(timeModel.sleep, greater, accuracy)
        times += light.checkIfInBounds(timeModel.light, greater, accuracy)
        times += motion.checkIfInBounds(timeModel.motion, greater, accuracy)

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

            val max:Float = list.maxOf { x-> x.confidence }.toFloat()
            val min:Float = list.minOf { x-> x.confidence }.toFloat()
            val average:Float = (list.sumOf { x-> x.confidence }).toFloat() / list.count()
            val median:Float = list.sortedBy { x-> x.confidence }[list.count()/2].confidence.toFloat()
            var factor:Float = 1.0f
            if (median >= 1)
            {
                factor = average / median
            }

            DataSetter()

            return DataSetter(
                    max.toFloat(),
                    min.toFloat(),
                    median.toFloat(),
                    average.toFloat(),
                    factor.toFloat()
            )
        }
        /**
         *  Creates a new model of motion
         */
        fun calculateMotion(list: List<SleepApiRawDataEntity>): DataSetter {

            val max:Float = list.maxOf { x-> x.motion }.toFloat()
            val min:Float = list.minOf { x-> x.motion }.toFloat()
            val average:Float = (list.sumOf { x-> x.motion }).toFloat() / list.count()
            val median:Float = list.sortedBy { x-> x.motion }[list.count()/2].motion.toFloat()
            var factor:Float = 1.0f
            if (median >= 1)
            {
                factor = average / median
            }

            DataSetter()

            return DataSetter(
                max.toFloat(),
                min.toFloat(),
                median.toFloat(),
                average.toFloat(),
                factor.toFloat()
            )
        }
        /**
         *  Creates a new model of light
         */
        fun calculateLight(list: List<SleepApiRawDataEntity>): DataSetter {

            val max:Float = list.maxOf { x-> x.light }.toFloat()
            val min:Float = list.minOf { x-> x.light }.toFloat()
            val average:Float = (list.sumOf { x-> x.light }).toFloat() / list.count()
            val median:Float = list.sortedBy { x-> x.light }[list.count()/2].light.toFloat()
            var factor:Float = 1.0f
            if (median >= 1)
            {
                factor = average / median
            }

            DataSetter()

            return DataSetter(
                max.toFloat(),
                min.toFloat(),
                median.toFloat(),
                average.toFloat(),
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
            val factor = sleep.Factor-awake.Factor

            return DataSetter(
                    max.toFloat(),
                    min.toFloat(),
                    median.toFloat(),
                    average.toFloat(),
                    factor.toFloat()
            )
        }

    }

    /**
     *  Checks if the sleep matches a model
     */
    fun checkIfInBounds(model:DataSetter, greater:Boolean, accuracy:Float) : Int{

        var times = 0

        if(greater && model.Max < Max - getFactor(Max, accuracy)) {
            times++
        }
        if(greater && model.Min < Min - getFactor(Min, accuracy)) {
            times++
        }
        if(greater && model.Median < Median - getFactor(Median, accuracy)) {
            times++
        }
        if(greater && model.Average < Average - getFactor(Average, accuracy)) {
            times++
        }
        if(greater && model.Factor < Factor - getFactor(Factor, accuracy)) {
            times++
        }


        if(!greater && model.Max > Max + getFactor(Max, accuracy)) {
            times++
        }
        if(!greater && model.Min > Min + getFactor(Min, accuracy)) {
            times++
        }
        if(!greater && model.Median > Median + getFactor(Median, accuracy)) {
            times++
        }
        if(!greater && model.Average > Average + getFactor(Average, accuracy)) {
            times++
        }
        if(!greater && model.Factor > Factor + getFactor(Factor, accuracy)) {
            times++
        }


        return times
        /*

        if(greater &&
            model.Max >= Max - (Max * accuracy) &&
            model.Min >= Min - (Min  * accuracy) &&
            model.Median >= Median - (Median  * accuracy) &&
            model.Average >= Average - (Average  * accuracy) &&
            model.Factor >= Factor - (Factor  * accuracy) )
        {
            return true
        }
        else if (!greater &&
            model.Max <= Max+ (Max * accuracy)  &&
            model.Min <= Min+ (Min * accuracy)  &&
            model.Median <= Median+ (Median * accuracy)  &&
            model.Average <= Average + (Average * accuracy) &&
            model.Factor <= Factor+ (Factor * accuracy) )
        {
            return true
        }

        return false
        */

    }

    private fun getFactor(value:Float, accuracy:Float) : Float
    {
        if(value < 100) {
            return accuracy
        } else if(value<1)
            return accuracy/10
        else if(value<0.1)
            return accuracy/100

        return 2.0f
    }
}
