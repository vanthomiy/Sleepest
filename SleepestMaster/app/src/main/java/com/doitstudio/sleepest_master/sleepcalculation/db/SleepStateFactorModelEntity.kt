package com.doitstudio.sleepest_master.sleepcalculation.db

import androidx.room.*
import com.doitstudio.sleepest_master.model.data.UserStartPattern
import com.doitstudio.sleepest_master.sleepcalculation.model.algorithm.SleepStateParameter

@Entity(tableName = "sleep_state_factor_model_entity")
data class SleepStateFactorModelEntity(

        @PrimaryKey
        val id:Int,

        @ColumnInfo(name="userStartPattern")
        val userStartPattern: UserStartPattern,//	Der name des aktuellen Patterns

        @Embedded val sleepTimeParameter: SleepStateParameter,//	Die Parameterwerte für den Algorithmus




)
{

        companion object {

                // load defaults from json
                fun setupDefaultEntities() : List<SleepStateFactorModelEntity>{

                        return listOf(
                                SleepStateFactorModelEntity(1,
                                        userStartPattern = UserStartPattern.HEAVY,
                                        SleepStateParameter(1f,1f,1f,1f,1f,1f,1f,1f,1f, 1f, 1f, 20)),
                                SleepStateFactorModelEntity(2,
                                        userStartPattern = UserStartPattern.HEAVY,
                                        SleepStateParameter(1f,1f,1f,1f,1f,1f,1f,1f,1f, 1f, 1f, 20))
                        )
                }

        }
}