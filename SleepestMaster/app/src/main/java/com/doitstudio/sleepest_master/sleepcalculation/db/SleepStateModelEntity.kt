package com.doitstudio.sleepest_master.sleepcalculation.db

import androidx.room.*
import com.doitstudio.sleepest_master.model.data.SleepStatePattern
import com.doitstudio.sleepest_master.model.data.SleepTimePattern
import com.doitstudio.sleepest_master.model.data.UserStartPattern
import com.doitstudio.sleepest_master.sleepcalculation.model.algorithm.*

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
{
        companion object {
                // load defaults from json
                fun setupDefaultEntities() : List<SleepStateModelEntity>{

                        return listOf(
                                SleepStateModelEntity(1, SleepStatePattern.TOLESSDEEP,
                                        SleepModel(
                                                SleepModelTypes(
                                                        SleepModelValues(1f,1f,1f,1f,1f),
                                                        SleepModelValues(1f,1f,1f,1f,1f),
                                                        SleepModelValues(1f,1f,1f,1f,1f),
                                                        SleepModelValues(1f,1f,1f,1f,1f),
                                                        SleepModelValues(1f,1f,1f,1f,1f),
                                                        SleepModelValues(1f,1f,1f,1f,1f)
                                                ),
                                                SleepModelTypes(
                                                        SleepModelValues(1f,1f,1f,1f,1f),
                                                        SleepModelValues(1f,1f,1f,1f,1f),
                                                        SleepModelValues(1f,1f,1f,1f,1f),
                                                        SleepModelValues(1f,1f,1f,1f,1f),
                                                        SleepModelValues(1f,1f,1f,1f,1f),
                                                        SleepModelValues(1f,1f,1f,1f,1f)
                                                ),
                                                SleepModelTypes(
                                                        SleepModelValues(1f,1f,1f,1f,1f),
                                                        SleepModelValues(1f,1f,1f,1f,1f),
                                                        SleepModelValues(1f,1f,1f,1f,1f),
                                                        SleepModelValues(1f,1f,1f,1f,1f),
                                                        SleepModelValues(1f,1f,1f,1f,1f),
                                                        SleepModelValues(1f,1f,1f,1f,1f)
                                                )),
                                        SleepModel(
                                                SleepModelTypes(
                                                        SleepModelValues(1f,1f,1f,1f,1f),
                                                        SleepModelValues(1f,1f,1f,1f,1f),
                                                        SleepModelValues(1f,1f,1f,1f,1f),
                                                        SleepModelValues(1f,1f,1f,1f,1f),
                                                        SleepModelValues(1f,1f,1f,1f,1f),
                                                        SleepModelValues(1f,1f,1f,1f,1f)
                                                ),
                                                SleepModelTypes(
                                                        SleepModelValues(1f,1f,1f,1f,1f),
                                                        SleepModelValues(1f,1f,1f,1f,1f),
                                                        SleepModelValues(1f,1f,1f,1f,1f),
                                                        SleepModelValues(1f,1f,1f,1f,1f),
                                                        SleepModelValues(1f,1f,1f,1f,1f),
                                                        SleepModelValues(1f,1f,1f,1f,1f)
                                                ),
                                                SleepModelTypes(
                                                        SleepModelValues(1f,1f,1f,1f,1f),
                                                        SleepModelValues(1f,1f,1f,1f,1f),
                                                        SleepModelValues(1f,1f,1f,1f,1f),
                                                        SleepModelValues(1f,1f,1f,1f,1f),
                                                        SleepModelValues(1f,1f,1f,1f,1f),
                                                        SleepModelValues(1f,1f,1f,1f,1f)
                                                )),
                                        SleepStateParameter(1f,1f,1f,1f,1f,1f,1f,1f,1f,1f,1f,11)
                                ),
                                SleepStateModelEntity(1, SleepStatePattern.TOLESSDEEP,
                                        SleepModel(
                                                SleepModelTypes(
                                                        SleepModelValues(1f,1f,1f,1f,1f),
                                                        SleepModelValues(1f,1f,1f,1f,1f),
                                                        SleepModelValues(1f,1f,1f,1f,1f),
                                                        SleepModelValues(1f,1f,1f,1f,1f),
                                                        SleepModelValues(1f,1f,1f,1f,1f),
                                                        SleepModelValues(1f,1f,1f,1f,1f)
                                                ),
                                                SleepModelTypes(
                                                        SleepModelValues(1f,1f,1f,1f,1f),
                                                        SleepModelValues(1f,1f,1f,1f,1f),
                                                        SleepModelValues(1f,1f,1f,1f,1f),
                                                        SleepModelValues(1f,1f,1f,1f,1f),
                                                        SleepModelValues(1f,1f,1f,1f,1f),
                                                        SleepModelValues(1f,1f,1f,1f,1f)
                                                ),
                                                SleepModelTypes(
                                                        SleepModelValues(1f,1f,1f,1f,1f),
                                                        SleepModelValues(1f,1f,1f,1f,1f),
                                                        SleepModelValues(1f,1f,1f,1f,1f),
                                                        SleepModelValues(1f,1f,1f,1f,1f),
                                                        SleepModelValues(1f,1f,1f,1f,1f),
                                                        SleepModelValues(1f,1f,1f,1f,1f)
                                                )),
                                        SleepModel(
                                                SleepModelTypes(
                                                        SleepModelValues(1f,1f,1f,1f,1f),
                                                        SleepModelValues(1f,1f,1f,1f,1f),
                                                        SleepModelValues(1f,1f,1f,1f,1f),
                                                        SleepModelValues(1f,1f,1f,1f,1f),
                                                        SleepModelValues(1f,1f,1f,1f,1f),
                                                        SleepModelValues(1f,1f,1f,1f,1f)
                                                ),
                                                SleepModelTypes(
                                                        SleepModelValues(1f,1f,1f,1f,1f),
                                                        SleepModelValues(1f,1f,1f,1f,1f),
                                                        SleepModelValues(1f,1f,1f,1f,1f),
                                                        SleepModelValues(1f,1f,1f,1f,1f),
                                                        SleepModelValues(1f,1f,1f,1f,1f),
                                                        SleepModelValues(1f,1f,1f,1f,1f)
                                                ),
                                                SleepModelTypes(
                                                        SleepModelValues(1f,1f,1f,1f,1f),
                                                        SleepModelValues(1f,1f,1f,1f,1f),
                                                        SleepModelValues(1f,1f,1f,1f,1f),
                                                        SleepModelValues(1f,1f,1f,1f,1f),
                                                        SleepModelValues(1f,1f,1f,1f,1f),
                                                        SleepModelValues(1f,1f,1f,1f,1f)
                                                )),
                                        SleepStateParameter(1f,1f,1f,1f,1f,1f,1f,1f,1f,1f,1f,11)
                                )
                        )
                }
        }
}

