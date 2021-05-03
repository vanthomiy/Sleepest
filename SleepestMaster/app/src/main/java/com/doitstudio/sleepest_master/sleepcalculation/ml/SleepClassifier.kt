package com.doitstudio.sleepest_master.sleepcalculation.ml

import android.content.Context
import com.doitstudio.sleepest_master.ml.SleepClassifierModel
import com.doitstudio.sleepest_master.model.data.SleepState
import com.doitstudio.sleepest_master.storage.db.SleepApiRawDataEntity
import org.tensorflow.lite.DataType
import org.tensorflow.lite.support.label.TensorLabel
import org.tensorflow.lite.support.tensorbuffer.TensorBuffer


class SleepClassifier constructor(private val context: Context) {

    /**
     * Pass the actual raw sleep api data entity
     * It returns the last 10 sleep api data and fills the rest with 0 is neccessary
     */
    fun createFeatures(data: List<SleepApiRawDataEntity>, count: Int = 10) : IntArray {

        // sort the list so we use it the right way.
        val sortedList = data.sortedByDescending { it.timestampSeconds }

        var preparedInput = IntArray(count*3)

        for (i in 0 until count)
        {
            if (sortedList.count() > i)
            {
                preparedInput[i * 3] = sortedList[i].confidence
                preparedInput[(i * 3) + 1] = sortedList[i].motion
                preparedInput[(i * 3) + 2] = sortedList[i].light
            }
        }


        return preparedInput
    }

    /**
     * Pass the with [createFeatures] created array of int to predict if the user is sleeping or not
     * Returns [SleepState] [SleepState.AWAKE] or [SleepState.SLEEPING] and [SleepState.NONE] if no data or an error occures
     */
    fun isUserSleeping(data: IntArray) : SleepState {

        try{

            val model = SleepClassifierModel.newInstance(context)

            // Creates inputs for reference.
            val inputFeature0 = TensorBuffer.createFixedSize(intArrayOf(1, 1), DataType.FLOAT32)
            inputFeature0.loadArray(intArrayOf(data[0]))
            val inputFeature1 = TensorBuffer.createFixedSize(intArrayOf(1, 1), DataType.FLOAT32)
            inputFeature1.loadArray(intArrayOf(data[1]))
            val inputFeature2 = TensorBuffer.createFixedSize(intArrayOf(1, 1), DataType.FLOAT32)
            inputFeature2.loadArray(intArrayOf(data[2]))
            val inputFeature3 = TensorBuffer.createFixedSize(intArrayOf(1, 1), DataType.FLOAT32)
            inputFeature3.loadArray(intArrayOf(data[3]))
            val inputFeature4 = TensorBuffer.createFixedSize(intArrayOf(1, 1), DataType.FLOAT32)
            inputFeature4.loadArray(intArrayOf(data[4]))
            val inputFeature5 = TensorBuffer.createFixedSize(intArrayOf(1, 1), DataType.FLOAT32)
            inputFeature5.loadArray(intArrayOf(data[5]))
            val inputFeature6 = TensorBuffer.createFixedSize(intArrayOf(1, 1), DataType.FLOAT32)
            inputFeature6.loadArray(intArrayOf(data[6]))
            val inputFeature7 = TensorBuffer.createFixedSize(intArrayOf(1, 1), DataType.FLOAT32)
            inputFeature7.loadArray(intArrayOf(data[7]))
            val inputFeature8 = TensorBuffer.createFixedSize(intArrayOf(1, 1), DataType.FLOAT32)
            inputFeature8.loadArray(intArrayOf(data[8]))
            val inputFeature9 = TensorBuffer.createFixedSize(intArrayOf(1, 1), DataType.FLOAT32)
            inputFeature9.loadArray(intArrayOf(data[9]))
            val inputFeature10 = TensorBuffer.createFixedSize(intArrayOf(1, 1), DataType.FLOAT32)
            inputFeature10.loadArray(intArrayOf(data[10]))
            val inputFeature11 = TensorBuffer.createFixedSize(intArrayOf(1, 1), DataType.FLOAT32)
            inputFeature11.loadArray(intArrayOf(data[11]))
            val inputFeature12 = TensorBuffer.createFixedSize(intArrayOf(1, 1), DataType.FLOAT32)
            inputFeature12.loadArray(intArrayOf(data[12]))
            val inputFeature13 = TensorBuffer.createFixedSize(intArrayOf(1, 1), DataType.FLOAT32)
            inputFeature13.loadArray(intArrayOf(data[13]))
            val inputFeature14 = TensorBuffer.createFixedSize(intArrayOf(1, 1), DataType.FLOAT32)
            inputFeature14.loadArray(intArrayOf(data[14]))
            val inputFeature15 = TensorBuffer.createFixedSize(intArrayOf(1, 1), DataType.FLOAT32)
            inputFeature15.loadArray(intArrayOf(data[15]))
            val inputFeature16 = TensorBuffer.createFixedSize(intArrayOf(1, 1), DataType.FLOAT32)
            inputFeature16.loadArray(intArrayOf(data[16]))
            val inputFeature17 = TensorBuffer.createFixedSize(intArrayOf(1, 1), DataType.FLOAT32)
            inputFeature17.loadArray(intArrayOf(data[17]))
            val inputFeature18 = TensorBuffer.createFixedSize(intArrayOf(1, 1), DataType.FLOAT32)
            inputFeature18.loadArray(intArrayOf(data[18]))
            val inputFeature19 = TensorBuffer.createFixedSize(intArrayOf(1, 1), DataType.FLOAT32)
            inputFeature19.loadArray(intArrayOf(data[19]))
            val inputFeature20 = TensorBuffer.createFixedSize(intArrayOf(1, 1), DataType.FLOAT32)
            inputFeature20.loadArray(intArrayOf(data[20]))
            val inputFeature21 = TensorBuffer.createFixedSize(intArrayOf(1, 1), DataType.FLOAT32)
            inputFeature21.loadArray(intArrayOf(data[21]))
            val inputFeature22 = TensorBuffer.createFixedSize(intArrayOf(1, 1), DataType.FLOAT32)
            inputFeature22.loadArray(intArrayOf(data[22]))
            val inputFeature23 = TensorBuffer.createFixedSize(intArrayOf(1, 1), DataType.FLOAT32)
            inputFeature23.loadArray(intArrayOf(data[23]))
            val inputFeature24 = TensorBuffer.createFixedSize(intArrayOf(1, 1), DataType.FLOAT32)
            inputFeature24.loadArray(intArrayOf(data[24]))
            val inputFeature25 = TensorBuffer.createFixedSize(intArrayOf(1, 1), DataType.FLOAT32)
            inputFeature25.loadArray(intArrayOf(data[25]))
            val inputFeature26 = TensorBuffer.createFixedSize(intArrayOf(1, 1), DataType.FLOAT32)
            inputFeature26.loadArray(intArrayOf(data[26]))
            val inputFeature27 = TensorBuffer.createFixedSize(intArrayOf(1, 1), DataType.FLOAT32)
            inputFeature27.loadArray(intArrayOf(data[27]))
            val inputFeature28 = TensorBuffer.createFixedSize(intArrayOf(1, 1), DataType.FLOAT32)
            inputFeature28.loadArray(intArrayOf(data[28]))
            val inputFeature29 = TensorBuffer.createFixedSize(intArrayOf(1, 1), DataType.FLOAT32)
            inputFeature29.loadArray(intArrayOf(data[29]))

            // Runs model inference and gets result.
            val outputs = model.process(inputFeature0, inputFeature1, inputFeature2, inputFeature3, inputFeature4, inputFeature5, inputFeature6, inputFeature7, inputFeature8, inputFeature9, inputFeature10, inputFeature11, inputFeature12, inputFeature13, inputFeature14, inputFeature15, inputFeature16, inputFeature17, inputFeature18, inputFeature19, inputFeature20, inputFeature21, inputFeature22, inputFeature23, inputFeature24, inputFeature25, inputFeature26, inputFeature27, inputFeature28, inputFeature29)

            val outputFeature0 = outputs.outputFeature0AsTensorBuffer

            // Map of labels and their corresponding probability
            val associatedAxisLabels: List<String> = listOf("AWAKE", "SLEEPING")

            // Map of labels and their corresponding probability
            val labels = TensorLabel(associatedAxisLabels, outputFeature0)

            // Create a map to access the result based on label

            // Create a map to access the result based on label
            val floatMap: Map<String, Float> = labels.mapWithFloatValue

            val classification = floatMap.maxByOrNull { it.value }

            // Releases model resources if no longer used.
            model.close()

           return SleepState.valueOf(classification?.key ?: "NONE")

        } catch (e: Exception)
        {
            print(e)

        }

        return SleepState.NONE
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





