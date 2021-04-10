package com.doitstudio.sleepest_master.sleepcalculation.db

import android.content.Context
import androidx.room.*
import com.doitstudio.sleepest_master.model.data.SleepTimePattern
import com.doitstudio.sleepest_master.model.data.UserFactorPattern
import com.doitstudio.sleepest_master.sleepcalculation.model.algorithm.SleepTimeParameter
import com.google.gson.Gson
import java.io.BufferedReader

@Entity(tableName = "sleep_time_parameter_entity")
data class SleepTimeParameterEntity(

        @PrimaryKey
        val id:String,

        val userFactorPattern: UserFactorPattern,//	Der name des aktuellen Patterns

        val sleepTimePattern: SleepTimePattern,//	Der name des aktuellen Patterns

        @Embedded val sleepTimeParameter: SleepTimeParameter,//	Die Parameterwerte für den Algorithmus

)
{
        companion object {
                // load defaults from json
                fun setupDefaultEntities(context: Context) : List<SleepTimeParameterEntity>{

                        var gson = Gson()
                        val jsonFile = context
                                .assets
                                .open("databases/TimeParameter.json")
                                .bufferedReader()
                                .use(BufferedReader::readText)

                        return gson.fromJson(jsonFile, Array<SleepTimeParameterEntity>::class.java).asList()
                }
        }
}
