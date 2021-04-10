package com.doitstudio.sleepest_master.sleepcalculation.model.algorithm

import androidx.room.ColumnInfo
import androidx.room.Embedded
import androidx.room.PrimaryKey
import androidx.room.Relation
import com.doitstudio.sleepest_master.model.data.SleepStatePattern


data class SleepModel(

    @Embedded(prefix = "awake") val valuesAwake:ValuesTimeModel,
    @Embedded(prefix = "sleep") val valuesSleep:ValuesTimeModel,
    @Embedded(prefix = "diff") val valuesDiff:ValuesTimeModel,

    )

data class ValuesTimeModel(
    @Embedded(prefix = "sleep") val sleep:DataSetter,
    @Embedded(prefix = "light") val light:DataSetter,
    @Embedded(prefix = "motion") val motion:DataSetter,
)

data class DataSetter(

    @ColumnInfo(name = "max")
    val Max:Float,	//Der Max wert
    @ColumnInfo(name = "min")
    val Min:Float,	//Der Min wert
    @ColumnInfo(name = "median")
    val Median:Float,	//Der Median wert
    @ColumnInfo(name = "average")
    val Average:Float,	//Der Average wert
    @ColumnInfo(name = "factor")
    val Factor:Float,	//Der Factor wert

)