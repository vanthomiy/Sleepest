package com.doitstudio.sleepest_master.sleepcalculation.db

import androidx.room.*
import com.doitstudio.sleepest_master.model.data.SleepStatePattern
import com.doitstudio.sleepest_master.model.data.SleepTimePattern
import com.doitstudio.sleepest_master.sleepcalculation.model.algorithm.SleepModel
import com.doitstudio.sleepest_master.sleepcalculation.model.algorithm.SleepStateParameter
import com.doitstudio.sleepest_master.sleepcalculation.model.algorithm.SleepTimeParameter

@Entity(tableName = "sleep_time_model_entity")
data class SleepTimeModelEntity(

        @PrimaryKey
        val id:Int,

        @ColumnInfo(name="sleepTimePattern")
        val sleepTimePattern: SleepTimePattern,//	Der name des aktuellen Patterns

        @Embedded(prefix = "max") val sleepTimeModelMax:SleepModel,//	Die Werte des Models max
        @Embedded(prefix = "min") val sleepTimeModelMin:SleepModel,//	Die Werte des Models min
        @Embedded val sleepTimeParameter: SleepTimeParameter,//	Die Parameterwerte für den Algorithmus

)