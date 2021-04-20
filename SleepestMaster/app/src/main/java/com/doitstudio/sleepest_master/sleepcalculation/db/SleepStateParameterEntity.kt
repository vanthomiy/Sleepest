package com.doitstudio.sleepest_master.sleepcalculation.db

import android.content.Context
import androidx.room.*
import com.doitstudio.sleepest_master.model.data.SleepStatePattern
import com.doitstudio.sleepest_master.model.data.UserFactorPattern
import com.doitstudio.sleepest_master.sleepcalculation.model.algorithm.SleepStateParameter
import com.google.gson.Gson
import java.io.BufferedReader

@Entity(tableName = "sleep_state_parameter_entity")
data class SleepStateParameterEntity(

        @PrimaryKey
        val id:String,

        val userFactorPattern: UserFactorPattern,//	Der name des aktuellen Patterns

        val sleepStatePattern: SleepStatePattern,//	Der name des aktuellen Patterns

        @Embedded val sleepStateParameter: SleepStateParameter,//	Die Parameterwerte für den Algorithmus

)
{

        companion object {
                // load defaults from json
                fun setupDefaultEntities(context: Context) : List<SleepStateParameterEntity>{

                        var gson = Gson()
                        val jsonFile = context
                                .assets
                                .open("databases/StateParameter.json")
                                .bufferedReader()
                                .use(BufferedReader::readText)

                        return gson.fromJson(jsonFile, Array<SleepStateParameterEntity>::class.java).asList()
                }
        }
}
