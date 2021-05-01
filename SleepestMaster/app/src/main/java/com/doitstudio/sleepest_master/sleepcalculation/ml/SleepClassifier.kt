package com.doitstudio.sleepest_master.sleepcalculation.ml

import android.content.Context
import com.doitstudio.sleepest_master.ml.Normalsmall
import com.doitstudio.sleepest_master.ml.Sleep04classifier

import com.doitstudio.sleepest_master.storage.db.SleepApiRawDataEntity
import org.tensorflow.lite.DataType
import org.tensorflow.lite.support.tensorbuffer.TensorBuffer


class SleepClassifier constructor(private val context: Context) {

    fun createFeatures(data:List<SleepApiRawDataEntity>, count:Int = 11) : IntArray {

        // sort the list so we use it the right way.
        val sortedList = data.sortedByDescending { it.timestampSeconds }

        var preparedInput = IntArray(count)

        for (i in 1 .. count)
        {
            if (sortedList.count() > i)
            {
                preparedInput[i*3] = sortedList[i].motion
                preparedInput[(i*3)+1] = sortedList[i].confidence
                preparedInput[(i*3)+2] = sortedList[i].light
            }
        }


        return preparedInput
    }

    fun loadModel(){




    }


    fun callModel(data:IntArray): Int{

        try {

            val model = Normalsmall.newInstance(context)

// Creates inputs for reference.
            val inputFeature0 = TensorBuffer.createFixedSize(intArrayOf(1, 1), DataType.FLOAT32)
            inputFeature0.loadArray(intArrayOf(1))

// Runs model inference and gets result.
            val outputs = model.process(inputFeature0)
            val outputFeature0 = outputs.outputFeature0AsTensorBuffer

// Releases model resources if no longer used.
            model.close()

        } catch (e: Exception)
        {
            print(e)

        }

        //val model1 = Mnasnet132241Default1.newInstance(context)

        //val model = Mnasnet132241Metadata1.newInstance(context)

        //val model = Sleep04classifier.newInstance(context)

// Creates inputs for reference.
        //val image = TensorImage.fromBitmap(bitmap)

// Runs model inference and gets result.
        //val outputs = model.process(image)
        //val probability = outputs.probabilityAsCategoryList

// Releases model resources if no longer used.


        return 1
        /*
        val model = Sleep04model.newInstance(context)

// Creates inputs for reference.
        val inputFeature0 = TensorBuffer.createFixedSize(intArrayOf(1, 1), DataType.INT64)
        inputFeature0.loadArray(intArrayOf(data[0]))
        val inputFeature1 = TensorBuffer.createFixedSize(intArrayOf(1, 1), DataType.INT64)
        inputFeature1.loadArray(intArrayOf(data[1]))
        val inputFeature2 = TensorBuffer.createFixedSize(intArrayOf(1, 1), DataType.INT64)
        inputFeature2.loadArray(intArrayOf(data[2]))
        val inputFeature3 = TensorBuffer.createFixedSize(intArrayOf(1, 1), DataType.INT64)
        inputFeature3.loadArray(intArrayOf(data[3]))
        val inputFeature4 = TensorBuffer.createFixedSize(intArrayOf(1, 1), DataType.INT64)
        inputFeature4.loadArray(intArrayOf(data[4]))
        val inputFeature5 = TensorBuffer.createFixedSize(intArrayOf(1, 1), DataType.INT64)
        inputFeature5.loadArray(intArrayOf(data[5]))
        val inputFeature6 = TensorBuffer.createFixedSize(intArrayOf(1, 1), DataType.INT64)
        inputFeature6.loadArray(intArrayOf(data[6]))
        val inputFeature7 = TensorBuffer.createFixedSize(intArrayOf(1, 1), DataType.INT64)
        inputFeature7.loadArray(intArrayOf(data[7]))
        val inputFeature8 = TensorBuffer.createFixedSize(intArrayOf(1, 1), DataType.INT64)
        inputFeature8.loadArray(intArrayOf(data[8]))
        val inputFeature9 = TensorBuffer.createFixedSize(intArrayOf(1, 1), DataType.INT64)
        inputFeature9.loadArray(intArrayOf(data[9]))
        val inputFeature10 = TensorBuffer.createFixedSize(intArrayOf(1, 1), DataType.INT64)
        inputFeature10.loadArray(intArrayOf(data[10]))
        val inputFeature11 = TensorBuffer.createFixedSize(intArrayOf(1, 1), DataType.INT64)
        inputFeature11.loadArray(intArrayOf(data[11]))
        val inputFeature12 = TensorBuffer.createFixedSize(intArrayOf(1, 1), DataType.INT64)
        inputFeature12.loadArray(intArrayOf(data[12]))
        val inputFeature13 = TensorBuffer.createFixedSize(intArrayOf(1, 1), DataType.INT64)
        inputFeature13.loadArray(intArrayOf(data[13]))
        val inputFeature14 = TensorBuffer.createFixedSize(intArrayOf(1, 1), DataType.INT64)
        inputFeature14.loadArray(intArrayOf(data[14]))
        val inputFeature15 = TensorBuffer.createFixedSize(intArrayOf(1, 1), DataType.INT64)
        inputFeature15.loadArray(intArrayOf(data[15]))
        val inputFeature16 = TensorBuffer.createFixedSize(intArrayOf(1, 1), DataType.INT64)
        inputFeature16.loadArray(intArrayOf(data[16]))
        val inputFeature17 = TensorBuffer.createFixedSize(intArrayOf(1, 1), DataType.INT64)
        inputFeature17.loadArray(intArrayOf(data[17]))
        val inputFeature18 = TensorBuffer.createFixedSize(intArrayOf(1, 1), DataType.INT64)
        inputFeature18.loadArray(intArrayOf(data[18]))
        val inputFeature19 = TensorBuffer.createFixedSize(intArrayOf(1, 1), DataType.INT64)
        inputFeature19.loadArray(intArrayOf(data[19]))
        val inputFeature20 = TensorBuffer.createFixedSize(intArrayOf(1, 1), DataType.INT64)
        inputFeature20.loadArray(intArrayOf(data[20]))
        val inputFeature21 = TensorBuffer.createFixedSize(intArrayOf(1, 1), DataType.INT64)
        inputFeature21.loadArray(intArrayOf(data[21]))
        val inputFeature22 = TensorBuffer.createFixedSize(intArrayOf(1, 1), DataType.INT64)
        inputFeature22.loadArray(intArrayOf(data[22]))
        val inputFeature23 = TensorBuffer.createFixedSize(intArrayOf(1, 1), DataType.INT64)
        inputFeature23.loadArray(intArrayOf(data[23]))
        val inputFeature24 = TensorBuffer.createFixedSize(intArrayOf(1, 1), DataType.INT64)
        inputFeature24.loadArray(intArrayOf(data[24]))
        val inputFeature25 = TensorBuffer.createFixedSize(intArrayOf(1, 1), DataType.INT64)
        inputFeature25.loadArray(intArrayOf(data[25]))
        val inputFeature26 = TensorBuffer.createFixedSize(intArrayOf(1, 1), DataType.INT64)
        inputFeature26.loadArray(intArrayOf(data[26]))
        val inputFeature27 = TensorBuffer.createFixedSize(intArrayOf(1, 1), DataType.INT64)
        inputFeature27.loadArray(intArrayOf(data[27]))
        val inputFeature28 = TensorBuffer.createFixedSize(intArrayOf(1, 1), DataType.INT64)
        inputFeature28.loadArray(intArrayOf(data[28]))
        val inputFeature29 = TensorBuffer.createFixedSize(intArrayOf(1, 1), DataType.INT64)
        inputFeature29.loadArray(intArrayOf(data[29]))
        val inputFeature30 = TensorBuffer.createFixedSize(intArrayOf(1, 1), DataType.INT64)
        inputFeature30.loadArray(intArrayOf(data[30]))
        val inputFeature31 = TensorBuffer.createFixedSize(intArrayOf(1, 1), DataType.INT64)
        inputFeature31.loadArray(intArrayOf(data[31]))
        val inputFeature32 = TensorBuffer.createFixedSize(intArrayOf(1, 1), DataType.INT64)
        inputFeature32.loadArray(intArrayOf(data[32]))

// Runs model inference and gets result.
        val outputs = model.process(inputFeature0, inputFeature1, inputFeature2, inputFeature3, inputFeature4, inputFeature5, inputFeature6, inputFeature7, inputFeature8, inputFeature9, inputFeature10, inputFeature11, inputFeature12, inputFeature13, inputFeature14, inputFeature15, inputFeature16, inputFeature17, inputFeature18, inputFeature19, inputFeature20, inputFeature21, inputFeature22, inputFeature23, inputFeature24, inputFeature25, inputFeature26, inputFeature27, inputFeature28, inputFeature29, inputFeature30, inputFeature31, inputFeature32)
        val outputFeature0 = outputs.outputFeature0AsTensorBuffer

// Releases model resources if no longer used.
        model.close()

        val calcValue = outputFeature0.intArray[0]

        return calcValue*/
    }


}