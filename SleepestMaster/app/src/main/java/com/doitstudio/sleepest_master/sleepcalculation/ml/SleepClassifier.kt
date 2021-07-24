package com.doitstudio.sleepest_master.sleepcalculation.ml

import android.content.Context
import com.doitstudio.sleepest_master.ml.*
import com.doitstudio.sleepest_master.model.data.MobilePosition
import com.doitstudio.sleepest_master.model.data.ModelProcess
import com.doitstudio.sleepest_master.model.data.SleepDataFrequency
import com.doitstudio.sleepest_master.model.data.SleepState
import com.doitstudio.sleepest_master.storage.db.SleepApiRawDataEntity
import com.google.gson.Gson
import org.tensorflow.lite.DataType
import org.tensorflow.lite.support.label.TensorLabel
import org.tensorflow.lite.support.tensorbuffer.TensorBuffer
import java.io.BufferedReader

/**
 * This class is used to use the functionality of the imported ml models.
 * In the app are used different types of ml models for specific use-cases.
 * With this we can
 *  - load input assignments [loadInputAssignmentFile]
 *  - create features of an actual sleep api raw data list [createFeatures]
 *  - create features for indicating table/bed of an actual sleep api raw data list [createTableFeatures]
 *  - defining if user is sleeping [isUserSleeping]
 *  - defining if phone is in bed [defineTableBed]
 *  - defining the actual state of sleeping [defineUserSleep]
 */
class SleepClassifier constructor(private val context: Context) {




    /**
     * Companion object is used for static fields in kotlin
     */
    companion object {
        // For Singleton instantiation
        @Volatile
        private var INSTANCE: SleepClassifier? = null

        /**
         * This should be used to create or get the actual instance of the [SleepClassifier] class
         */
        fun getHandler(context: Context): SleepClassifier {
            return INSTANCE ?: synchronized(this) {
                val instance = SleepClassifier(context)
                INSTANCE = instance
                // return instance
                instance
            }
        }
    }
}





