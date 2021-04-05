package com.doitstudio.sleepest_master.sleepcalculation.model.algorithm

import androidx.room.ColumnInfo
import androidx.room.Embedded
import androidx.room.PrimaryKey
import androidx.room.Relation
import com.doitstudio.sleepest_master.model.data.SleepStatePattern


data class SleepModel(

    @Embedded(prefix = "awake") val valuesAwake:SleepModelTypes,
    @Embedded(prefix = "sleep") val valuesSleep:SleepModelTypes,
    @Embedded(prefix = "diff") val valuesDiff:SleepModelTypes,

    )

data class SleepModelTypes(
    @Embedded(prefix = "maxSleep") val maxSchlaf:SleepModelValues,
    @Embedded(prefix = "minSleep") val minSchlaf:SleepModelValues,
    @Embedded(prefix = "maxLight") val maxLicht:SleepModelValues,
    @Embedded(prefix = "minLight") val minLicht:SleepModelValues,
    @Embedded(prefix = "maxMotion") val maxMotion:SleepModelValues,
    @Embedded(prefix = "minMotion") val minMotion:SleepModelValues
)

data class SleepModelValues(

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