package com.doitstudio.sleepest_master.sleepcalculation.db

import android.content.Context
import androidx.room.*
import com.doitstudio.sleepest_master.model.data.SleepStatePattern
import com.doitstudio.sleepest_master.sleepcalculation.model.algorithm.*
import com.google.gson.Gson
import java.io.BufferedReader

@Entity(tableName = "sleep_state_model_entity")
data class SleepStateModelEntity(

        @PrimaryKey
        val id:String,

        val sleepStatePattern:SleepStatePattern,//	Der name des aktuellen Patterns

        @Embedded(prefix = "max") val sleepStateModelMax:SleepModel,//	Die Werte des Models max
        @Embedded(prefix = "min") val sleepStateModelMin:SleepModel,//	Die Werte des Models min

        //@Embedded val sleepStateParameter:SleepStateParameter,//	Die Parameterwerte für den Algorithmus

)
{

        companion object {
                // load defaults from json
                fun setupDefaultEntities(context: Context) : List<SleepStateModelEntity>{

                        var gson = Gson()
                        val jsonFile = context
                                .assets
                                .open("databases/StateModel.json")
                                .bufferedReader()
                                .use(BufferedReader::readText)

                        return gson.fromJson(jsonFile, Array<SleepStateModelEntity>::class.java).asList()
                }
        }
}

