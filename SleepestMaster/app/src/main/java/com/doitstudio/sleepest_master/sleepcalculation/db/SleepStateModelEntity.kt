package com.doitstudio.sleepest_master.sleepcalculation.db

import androidx.room.*
import com.doitstudio.sleepest_master.model.data.SleepStatePattern
import com.doitstudio.sleepest_master.sleepcalculation.model.algorithm.SleepModel
import com.doitstudio.sleepest_master.sleepcalculation.model.algorithm.SleepStateParameter

@Entity(tableName = "sleep_state_model_entity")
data class SleepStateModelEntity(

        @PrimaryKey
        val id:Int,

        @ColumnInfo(name="sleepStatePattern")
        val sleepStatePattern:SleepStatePattern,//	Der name des aktuellen Patterns

        @Embedded(prefix = "max") val sleepStateModelMax:SleepModel,//	Die Werte des Models max
        @Embedded(prefix = "min") val sleepStateModelMin:SleepModel,//	Die Werte des Models min
        @Embedded val sleepStateParameter:SleepStateParameter,//	Die Parameterwerte für den Algorithmus

)