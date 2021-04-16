package com.doitstudio.sleepest_master.sleepcalculation.db

import android.content.Context
import androidx.room.*
import com.doitstudio.sleepest_master.model.data.SleepStatePattern
import com.doitstudio.sleepest_master.model.data.SleepTimePattern
import com.doitstudio.sleepest_master.model.data.UserFactorPattern
import com.doitstudio.sleepest_master.sleepcalculation.model.algorithm.*
import com.google.gson.Gson
import java.io.BufferedReader

@Entity(tableName = "sleep_state_model_entity")
data class SleepStateModelEntity(

        @PrimaryKey
        val id:String,

        val sleepStatePattern:SleepStatePattern,//	Der name des aktuellen Patterns
        val userFactorPattern: UserFactorPattern,//	Der name des aktuellen Patterns

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

        /**
         * Returns the sleepstate pattern else 0 if the model matches the pattern
         */
        fun checkIfIsModel(model: SleepModel, accuracy:Float): String
        {
                var times = 0
                val alltimes = 12
                times += sleepStateModelMax.checkIfInBounds(model, false, accuracy)
                times += sleepStateModelMin.checkIfInBounds(model, true, accuracy)

                if (times == 0) // (times * 100) / alltimes > 95f)
                {
                        return id
                }

                return ""
        }


}

