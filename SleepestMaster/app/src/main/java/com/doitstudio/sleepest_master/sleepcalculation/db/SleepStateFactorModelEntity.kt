package com.doitstudio.sleepest_master.sleepcalculation.db

import androidx.room.*
import com.doitstudio.sleepest_master.model.data.SleepStatePattern
import com.doitstudio.sleepest_master.model.data.SleepTimePattern
import com.doitstudio.sleepest_master.model.data.UserStartPattern
import com.doitstudio.sleepest_master.sleepcalculation.model.algorithm.SleepModel
import com.doitstudio.sleepest_master.sleepcalculation.model.algorithm.SleepStateParameter
import com.doitstudio.sleepest_master.sleepcalculation.model.algorithm.SleepTimeParameter

@Entity(tableName = "sleep_state_factor_model_entity")
data class SleepStateFactorModelEntity(

        @PrimaryKey
        val id:Int,

        @ColumnInfo(name="userStartPattern")
        val userStartPattern: UserStartPattern,//	Der name des aktuellen Patterns

        @Embedded val sleepTimeParameter: SleepStateParameter,//	Die Parameterwerte für den Algorithmus

)