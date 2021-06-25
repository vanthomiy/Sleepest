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


class SleepClassifier constructor(private val context: Context) {

    private val inputSleep045 = lazy { loadInputAssigmentFile(ModelProcess.SLEEP04, SleepDataFrequency.FIVE)  }
    private val inputSleep0410 = lazy { loadInputAssigmentFile(ModelProcess.SLEEP04, SleepDataFrequency.TEN)  }
    private val inputSleep0430 = lazy { loadInputAssigmentFile(ModelProcess.SLEEP04, SleepDataFrequency.THIRTY)  }

    private val inputSleep125 = lazy { loadInputAssigmentFile(ModelProcess.SLEEP12, SleepDataFrequency.FIVE)  }
    private val inputSleep1210 = lazy { loadInputAssigmentFile(ModelProcess.SLEEP12, SleepDataFrequency.TEN)  }
    private val inputSleep1230 = lazy { loadInputAssigmentFile(ModelProcess.SLEEP12, SleepDataFrequency.THIRTY)  }

    private val inputTableBed = lazy { loadInputAssigmentFile(ModelProcess.TABLEBED)  }

    /**
     * loading the model inputs indexes for preparing model input
     */
    fun loadInputAssigmentFile(process:ModelProcess, sleepDataFrequency: SleepDataFrequency = SleepDataFrequency.NONE): List<ModelInputAssignment> {

        val file = ModelProcess.getString(process)
        val frequency = SleepDataFrequency.getValue(sleepDataFrequency)

        var path = if(sleepDataFrequency != SleepDataFrequency.NONE) "ml/InputAssignments$file$frequency.json" else "ml/InputAssignments$file.json"

        var gson = Gson()

        val jsonFile = context
                .assets
                .open(path)
                .bufferedReader()
                .use(BufferedReader::readText)

        return gson.fromJson(jsonFile, Array<ModelInputAssignment>::class.java).asList()
    }


    /**
     * Pass the actual raw sleep api data entity
     * It returns the last 10 sleep api data and fills the rest with 0 if necessary
     **/
    fun createFeatures(data: List<SleepApiRawDataEntity>, modelProcess: ModelProcess, sleepDataFrequency: SleepDataFrequency) : FloatArray {


        val inputAssignment = when (modelProcess) {
            ModelProcess.SLEEP04 -> {
                 when(sleepDataFrequency) {
                    SleepDataFrequency.FIVE -> inputSleep045
                    SleepDataFrequency.TEN -> inputSleep0410
                    else -> inputSleep0430
                }
            }
            ModelProcess.SLEEP12 -> {
                when(sleepDataFrequency) {
                    SleepDataFrequency.FIVE -> inputSleep125
                    SleepDataFrequency.TEN -> inputSleep1210
                    else -> inputSleep1230
                }
            }
            else -> {
                when(sleepDataFrequency) {
                    SleepDataFrequency.FIVE -> inputSleep045
                    SleepDataFrequency.TEN -> inputSleep0410
                    else -> inputSleep0430
                }
            }
        }

        // sort the list so we use it the right way.
        val sortedList = data.sortedByDescending { it.timestampSeconds }

        val count = SleepDataFrequency.getCount(sleepDataFrequency)
        var preparedInput = FloatArray(count*3)

        for (i in 0 until count)
        {
            if (sortedList.count() > i)
            {
                val sleepIndex = inputAssignment.value.first{x -> x.name.contains("sleep$i:")}.index
                val motionIndex = inputAssignment.value.first{x -> x.name.contains("motion$i:")}.index
                val lightIndex = inputAssignment.value.first{x -> x.name.contains("brigthness$i:")}.index

                preparedInput[sleepIndex] = sortedList[i].confidence.toFloat()
                preparedInput[motionIndex] = sortedList[i].motion.toFloat()
                preparedInput[lightIndex] = sortedList[i].light.toFloat()
            }
        }

        return preparedInput
    }

    /**
     * Pass the arrays for [light] [sleep] and [motion] as Int Arrays
     * Loads the actual TableBed Json Assignment and defines the inputs for the model
     */
    fun createTableFeatures(light: IntArray, motion:IntArray, sleep:IntArray) : FloatArray {

        val mapping = mapOf(0 to "max", 1 to "min", 2 to "average", 3 to "median"  )
        val inputAssignment = inputTableBed

        var preparedInput = FloatArray(12)

        for(i in 0 until light.count())
            preparedInput[inputAssignment.value.first{x -> x.name.toLowerCase().contains("brigthness") && x.name.toLowerCase().contains(mapping[i]?:"") }?.index] = light[i].toFloat()

        for(i in 0 until motion.count())
            preparedInput[inputAssignment.value.first{x -> x.name.toLowerCase().contains("motion") && x.name.toLowerCase().contains(mapping[i]?:"") }.index] = motion[i].toFloat()

        for(i in 0 until sleep.count())
            preparedInput[inputAssignment.value.first{x -> x.name.toLowerCase().contains("sleep") && x.name.toLowerCase().contains(mapping[i]?:"") }.index] = sleep[i].toFloat()


        return preparedInput
    }

    /**
     * Pass the with [createFeatures] created array of int to predict if the user is sleeping or not
     * Returns [SleepState] [SleepState.AWAKE] or [SleepState.SLEEPING] and [SleepState.NONE] if no data or an error occures
     */
    fun isUserSleeping(data: FloatArray, sleepDataFrequency: SleepDataFrequency) : SleepState {

        try{
            val inputCount = SleepDataFrequency.getCount(sleepDataFrequency)

            // region assignments
            val inputs = Array<TensorBuffer>(inputCount*3) { TensorBuffer.createFixedSize(intArrayOf(1, 1), DataType.FLOAT32)  }
            for(i in 0 until inputs.count()) {
                // Creates inputs for reference.
                inputs[i].loadArray(floatArrayOf(data[i]))
            }

            val model = when (sleepDataFrequency) {
                SleepDataFrequency.FIVE ->  Sleep045.newInstance(context)
                SleepDataFrequency.TEN -> Sleep0410.newInstance(context)
                else -> Sleep0430.newInstance(context)
            }

            val outputs = when(model){
                is Sleep045 -> model.process(inputs[0],inputs[1],inputs[2],inputs[3],inputs[4],inputs[5],inputs[6],inputs[7],inputs[8],inputs[9],inputs[10],inputs[11],inputs[12],inputs[13],inputs[14],inputs[15],inputs[16],inputs[17],inputs[18],inputs[19],inputs[20],inputs[21],inputs[22],inputs[23],inputs[24],inputs[25],inputs[26],inputs[27],inputs[28],inputs[29],inputs[30],inputs[31],inputs[32], inputs[33],inputs[34],inputs[35],inputs[36],inputs[37],inputs[38],inputs[39],inputs[40],inputs[41],inputs[42],inputs[43],inputs[44],inputs[45],inputs[46],inputs[47],inputs[48],inputs[49],inputs[50],inputs[51],inputs[52],inputs[53],inputs[54],inputs[55],inputs[56],inputs[57],inputs[58],inputs[59],inputs[60],inputs[61],inputs[62],inputs[63],inputs[64],inputs[65],inputs[66],inputs[67],inputs[68],inputs[69],inputs[70],inputs[71])
                is Sleep0410 -> model.process(inputs[0],inputs[1],inputs[2],inputs[3],inputs[4],inputs[5],inputs[6],inputs[7],inputs[8],inputs[9],inputs[10],inputs[11],inputs[12],inputs[13],inputs[14],inputs[15],inputs[16],inputs[17],inputs[18],inputs[19],inputs[20],inputs[21],inputs[22],inputs[23],inputs[24],inputs[25],inputs[26],inputs[27],inputs[28],inputs[29],inputs[30],inputs[31],inputs[32], inputs[33],inputs[34],inputs[35])
                else -> (model as Sleep0430).process(inputs[0],inputs[1],inputs[2],inputs[3],inputs[4],inputs[5],inputs[6],inputs[7],inputs[8],inputs[9],inputs[10],inputs[11])
            }

            val outputFeature = when(outputs){
                is Sleep045.Outputs -> outputs.outputFeature0AsTensorBuffer
                is Sleep0410.Outputs -> outputs.outputFeature0AsTensorBuffer
                else -> (model as Sleep0430.Outputs).outputFeature0AsTensorBuffer
            }

            // Map of labels and their corresponding probability
            val associatedAxisLabels: List<String> = listOf("AWAKE", "SLEEPING")

            // Map of labels and their corresponding probability
            val labels = TensorLabel(associatedAxisLabels, outputFeature)

            // Create a map to access the result based on label
            val floatMap: Map<String, Float> = labels.mapWithFloatValue

            // classificate the data
            val classification = floatMap.maxByOrNull { it.value }

            // Releases model resources if no longer used.
            when(model){
                is Sleep045 -> model.close()
                is Sleep0410 -> model.close()
                else -> (model as Sleep0430).close()
            }

           return SleepState.valueOf(classification?.key ?: "NONE")

        } catch (e: Exception)
        {
            return SleepState.NONE
        }

        return SleepState.AWAKE
    }

    /**
     * Pass the with [createFeatures] created array of int to predict if the user sleep state
     * Returns [SleepState] [SleepState.LIGHT] or [SleepState.DEEP] and [SleepState.NONE] if no data or an error occures
     */
    fun defineUserSleep(data: FloatArray, sleepDataFrequency: SleepDataFrequency) : SleepState {

        try{

            val inputCount = SleepDataFrequency.getCount(sleepDataFrequency)

            // region assignments
            val inputs = Array<TensorBuffer>(inputCount*3) { TensorBuffer.createFixedSize(intArrayOf(1, 1), DataType.FLOAT32)  }
            for(i in 0 until inputs.count()) {
                // Creates inputs for reference.
                inputs[i].loadArray(floatArrayOf(data[i]))
            }

            val model = when (sleepDataFrequency) {
                SleepDataFrequency.FIVE ->  Sleep125.newInstance(context)
                SleepDataFrequency.TEN -> Sleep1210.newInstance(context)
                else -> Sleep1230.newInstance(context)
            }

            val outputs = when(model){
                is Sleep125 -> model.process(inputs[0],inputs[1],inputs[2],inputs[3],inputs[4],inputs[5],inputs[6],inputs[7],inputs[8],inputs[9],inputs[10],inputs[11],inputs[12],inputs[13],inputs[14],inputs[15],inputs[16],inputs[17],inputs[18],inputs[19],inputs[20],inputs[21],inputs[22],inputs[23],inputs[24],inputs[25],inputs[26],inputs[27],inputs[28],inputs[29],inputs[30],inputs[31],inputs[32],inputs[33],inputs[34],inputs[35],inputs[36],inputs[37],inputs[38],inputs[39],inputs[40],inputs[41],inputs[42],inputs[43],inputs[44],inputs[45],inputs[46],inputs[47],inputs[48],inputs[49],inputs[50],inputs[51],inputs[52],inputs[53],inputs[54],inputs[55],inputs[56],inputs[57],inputs[58],inputs[59],inputs[60],inputs[61],inputs[62],inputs[63],inputs[64],inputs[65],inputs[66],inputs[67],inputs[68],inputs[69],inputs[70],inputs[71])
                is Sleep1210 -> model.process(inputs[0],inputs[1],inputs[2],inputs[3],inputs[4],inputs[5],inputs[6],inputs[7],inputs[8],inputs[9],inputs[10],inputs[11],inputs[12],inputs[13],inputs[14],inputs[15],inputs[16],inputs[17],inputs[18],inputs[19],inputs[20],inputs[21],inputs[22],inputs[23],inputs[24],inputs[25],inputs[26],inputs[27],inputs[28],inputs[29],inputs[30],inputs[31],inputs[32])
                else -> (model as Sleep1230).process(inputs[0],inputs[1],inputs[2],inputs[3],inputs[4],inputs[5],inputs[6],inputs[7],inputs[8])
            }

            val outputFeature = when(outputs){
                is Sleep125.Outputs -> outputs.outputFeature0AsTensorBuffer
                is Sleep1210.Outputs -> outputs.outputFeature0AsTensorBuffer
                else -> (model as Sleep1230.Outputs).outputFeature0AsTensorBuffer
            }

            // Map of labels and their corresponding probability
            val associatedAxisLabels: List<String> = listOf("LIGHT", "DEEP")

            // Map of labels and their corresponding probability
            val labels = TensorLabel(associatedAxisLabels, outputFeature)

            // Create a map to access the result based on label
            val floatMap: Map<String, Float> = labels.mapWithFloatValue

            // classificate the data
            val classification = floatMap.maxByOrNull { it.value }

            // Releases model resources if no longer used.
            when(model){
                is Sleep125 -> model.close()
                is Sleep1210 -> model.close()
                else -> (model as Sleep1230).close()
            }

            return SleepState.valueOf(classification?.key ?: "NONE")

        } catch (e: Exception)
        {
            return SleepState.NONE
        }

        return SleepState.NONE
    }

    /**
     * Pass the with [createFeatures] created array of int to predict if the user sleep state for the future ( next ) [sleepDataFrequency]
     * Returns [SleepState] [SleepState.LIGHT] or [SleepState.DEEP] and [SleepState.NONE] if no data or an error occures
     */
    fun defineFutureUserSleep(data: FloatArray, sleepDataFrequency: SleepDataFrequency) : SleepState {

        try{

            val inputCount = SleepDataFrequency.getCount(sleepDataFrequency)

            // region assignments
            val inputs = Array<TensorBuffer>(inputCount*3) { TensorBuffer.createFixedSize(intArrayOf(1, 1), DataType.FLOAT32)  }
            for(i in 0 until inputs.count()) {
                // Creates inputs for reference.
                inputs[i].loadArray(floatArrayOf(data[i]))
            }

            val model = when (sleepDataFrequency) {
                SleepDataFrequency.FIVE ->  Wakeuplight5.newInstance(context)
                SleepDataFrequency.TEN -> Wakeuplight10.newInstance(context)
                else -> Wakeuplight30.newInstance(context)
            }

            val outputs = when(model){
                is Wakeuplight5 -> model.process(inputs[0],inputs[1],inputs[2],inputs[3],inputs[4],inputs[5],inputs[6],inputs[7],inputs[8],inputs[9],inputs[10],inputs[11],inputs[12],inputs[13],inputs[14],inputs[15],inputs[16],inputs[17],inputs[18],inputs[19],inputs[20],inputs[21],inputs[22],inputs[23],inputs[24],inputs[25],inputs[26],inputs[27],inputs[28],inputs[29],inputs[30],inputs[31],inputs[32],inputs[33],inputs[34],inputs[35],inputs[36],inputs[37],inputs[38],inputs[39],inputs[40],inputs[41],inputs[42],inputs[43],inputs[44],inputs[45],inputs[46],inputs[47],inputs[48],inputs[49],inputs[50],inputs[51],inputs[52],inputs[53],inputs[54],inputs[55],inputs[56],inputs[57],inputs[58],inputs[59],inputs[60],inputs[61],inputs[62],inputs[63],inputs[64],inputs[65],inputs[66],inputs[67],inputs[68],inputs[69],inputs[70],inputs[71])
                is Wakeuplight10 -> model.process(inputs[0],inputs[1],inputs[2],inputs[3],inputs[4],inputs[5],inputs[6],inputs[7],inputs[8],inputs[9],inputs[10],inputs[11],inputs[12],inputs[13],inputs[14],inputs[15],inputs[16],inputs[17],inputs[18],inputs[19],inputs[20],inputs[21],inputs[22],inputs[23],inputs[24],inputs[25],inputs[26],inputs[27],inputs[28],inputs[29],inputs[30],inputs[31],inputs[32],inputs[33],inputs[34],inputs[35])
                else -> (model as Wakeuplight30).process(inputs[0],inputs[1],inputs[2],inputs[3],inputs[4],inputs[5],inputs[6],inputs[7],inputs[8],inputs[9],inputs[10],inputs[11])
            }

            val outputFeature = when(outputs){
                is Wakeuplight5.Outputs -> outputs.outputFeature0AsTensorBuffer
                is Wakeuplight10.Outputs -> outputs.outputFeature0AsTensorBuffer
                else -> (model as Wakeuplight30.Outputs).outputFeature0AsTensorBuffer
            }

            // Map of labels and their corresponding probability
            val associatedAxisLabels: List<String> = listOf("LIGHT", "DEEP")

            // Map of labels and their corresponding probability
            val labels = TensorLabel(associatedAxisLabels, outputFeature)

            // Create a map to access the result based on label
            val floatMap: Map<String, Float> = labels.mapWithFloatValue

            // classificate the data
            val classification = floatMap.maxByOrNull { it.value }

            // Releases model resources if no longer used.
            when(model){
                is Wakeuplight5 -> model.close()
                is Wakeuplight5 -> model.close()
                else -> (model as Wakeuplight30).close()
            }

            return SleepState.valueOf(classification?.key ?: "NONE")

        } catch (e: Exception)
        {
            return SleepState.NONE
        }

        return SleepState.NONE
    }

    /**
     * Pass the [data] created array of int to predict if the user sleep state
     * Returns [MobilePosition] [MobilePosition.INBED] or [MobilePosition.ONTABLE] and [MobilePosition.UNIDENTIFIED] if no data or an error occures
     */
    fun defineTableBed(data: FloatArray) : MobilePosition {

        try{

            // region assignments
            val inputs = Array<TensorBuffer>(data.size) { TensorBuffer.createFixedSize(intArrayOf(1, 1), DataType.FLOAT32)  }
            for(i in 0 until inputs.count()) {
                // Creates inputs for reference.
                inputs[i].loadArray(floatArrayOf(data[i]))
            }

            val model = Tablefile5.newInstance(context)

            val outputs = model.process(inputs[0],inputs[1],inputs[2],inputs[3],inputs[4],inputs[5],inputs[6],inputs[7],inputs[8],inputs[9],inputs[10],inputs[11])


            val outputFeature = outputs.outputFeature0AsTensorBuffer

            // Map of labels and their corresponding probability
            val associatedAxisLabels: List<String> = listOf("ONTABLE", "INBED")

            // Map of labels and their corresponding probability
            val labels = TensorLabel(associatedAxisLabels, outputFeature)

            // Create a map to access the result based on label
            val floatMap: Map<String, Float> = labels.mapWithFloatValue

            // classificate the data
            val classification = floatMap.maxByOrNull { it.value }

            // Releases model resources if no longer used.
            model.close()

            return MobilePosition.valueOf(classification?.key ?: "NONE")

        } catch (e: Exception)
        {
            return MobilePosition.UNIDENTIFIED
        }

        return MobilePosition.UNIDENTIFIED
    }

    companion object {
        // For Singleton instantiation
        @Volatile
        private var INSTANCE: SleepClassifier? = null

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





