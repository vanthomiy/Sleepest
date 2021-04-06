package com.doitstudio.sleepest_master.sleepcalculation.db

import androidx.room.*
import com.doitstudio.sleepest_master.model.data.SleepStatePattern
import com.doitstudio.sleepest_master.model.data.SleepTimePattern
import com.doitstudio.sleepest_master.model.data.UserStartPattern
import com.doitstudio.sleepest_master.sleepcalculation.model.algorithm.SleepModel
import com.doitstudio.sleepest_master.sleepcalculation.model.algorithm.SleepStateParameter
import com.doitstudio.sleepest_master.sleepcalculation.model.algorithm.SleepTimeParameter

@Entity(tableName = "sleep_time_factor_model_entity")
data class SleepTimeFactorModelEntity(

        @PrimaryKey
        val id:Int,

        @ColumnInfo(name="userStartPattern")
        val userStartPattern: UserStartPattern,//	Der name des aktuellen Patterns

        @Embedded val sleepTimeParameter: SleepTimeParameter,//	Die Parameterwerte für den Algorithmus

)
{

        companion object {

                // load defaults from json
                fun setupDefaultEntities() : List<SleepTimeFactorModelEntity>{

                        return listOf(
                                SleepTimeFactorModelEntity(1,
                                        userStartPattern = UserStartPattern.HEAVY,
                                        SleepTimeParameter(1f,1f,1f,1f,1f,1f,1f,1f,1f, 1f, 1f, 1f,20)),
                                SleepTimeFactorModelEntity(2,
                                        userStartPattern = UserStartPattern.HEAVY,
                                        SleepTimeParameter(1f,1f,1f,1f,1f,1f,1f,1f,1f, 1f, 1f, 1f,20))
                        )
                }

        }
}