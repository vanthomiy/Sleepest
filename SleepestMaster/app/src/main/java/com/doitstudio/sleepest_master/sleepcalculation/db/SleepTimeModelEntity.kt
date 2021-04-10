package com.doitstudio.sleepest_master.sleepcalculation.db

import android.content.Context
import androidx.room.*
import com.doitstudio.sleepest_master.model.data.SleepTimePattern
import com.doitstudio.sleepest_master.sleepcalculation.model.algorithm.*
import com.google.gson.Gson
import java.io.BufferedReader

@Entity(tableName = "sleep_time_model_entity")
data class SleepTimeModelEntity(

        @PrimaryKey
        val id:String,

        val sleepTimePattern: SleepTimePattern,//	Der name des aktuellen Patterns

        @Embedded(prefix = "max") val sleepTimeModelMax:SleepModel,//	Die Werte des Models max
        @Embedded(prefix = "min") val sleepTimeModelMin:SleepModel,//	Die Werte des Models min

        //@Embedded val sleepTimeParameter: SleepTimeParameter,//	Die Parameterwerte für den Algorithmus

)
{

        companion object {
                // load defaults from json
                fun setupDefaultEntities(context: Context) : List<SleepTimeModelEntity>{

                    var gson = Gson()
                    val jsonFile = context
                            .assets
                            .open("databases/TimeModel.json")
                            .bufferedReader()
                            .use(BufferedReader::readText)

                    return gson.fromJson(jsonFile, Array<SleepTimeModelEntity>::class.java).asList()
                }
        }
}

