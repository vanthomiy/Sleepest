package com.doitstudio.sleepest_master.sleepcalculation.ml

import android.content.Context
import android.content.res.AssetManager
import com.doitstudio.sleepest_master.ml.Sleep04model
import org.tensorflow.lite.DataType
import org.tensorflow.lite.Interpreter
import org.tensorflow.lite.support.tensorbuffer.TensorBuffer
import java.io.FileInputStream
import java.io.IOException
import java.nio.MappedByteBuffer
import java.nio.channels.FileChannel


class SleepClassifier constructor(private val context: Context) {

    fun callModel(): Int{
        val model = Sleep04model.newInstance(context)

// Creates inputs for reference.
        val inputFeature0 = TensorBuffer.createFixedSize(intArrayOf(1, 1), DataType.INT64)
        inputFeature0.loadArray(intArrayOf(1))
        val inputFeature1 = TensorBuffer.createFixedSize(intArrayOf(1, 1), DataType.INT64)
        inputFeature1.loadArray(intArrayOf(1))
        val inputFeature2 = TensorBuffer.createFixedSize(intArrayOf(1, 1), DataType.INT64)
        inputFeature2.loadArray(intArrayOf(1))
        val inputFeature3 = TensorBuffer.createFixedSize(intArrayOf(1, 1), DataType.INT64)
        inputFeature3.loadArray(intArrayOf(1))
        val inputFeature4 = TensorBuffer.createFixedSize(intArrayOf(1, 1), DataType.INT64)
        inputFeature4.loadArray(intArrayOf(1))
        val inputFeature5 = TensorBuffer.createFixedSize(intArrayOf(1, 1), DataType.INT64)
        inputFeature5.loadArray(intArrayOf(1))
        val inputFeature6 = TensorBuffer.createFixedSize(intArrayOf(1, 1), DataType.INT64)
        inputFeature6.loadArray(intArrayOf(1))
        val inputFeature7 = TensorBuffer.createFixedSize(intArrayOf(1, 1), DataType.INT64)
        inputFeature7.loadArray(intArrayOf(1))
        val inputFeature8 = TensorBuffer.createFixedSize(intArrayOf(1, 1), DataType.INT64)
        inputFeature8.loadArray(intArrayOf(1))
        val inputFeature9 = TensorBuffer.createFixedSize(intArrayOf(1, 1), DataType.INT64)
        inputFeature9.loadArray(intArrayOf(1))
        val inputFeature10 = TensorBuffer.createFixedSize(intArrayOf(1, 1), DataType.INT64)
        inputFeature10.loadArray(intArrayOf(1))
        val inputFeature11 = TensorBuffer.createFixedSize(intArrayOf(1, 1), DataType.INT64)
        inputFeature11.loadArray(intArrayOf(1))
        val inputFeature12 = TensorBuffer.createFixedSize(intArrayOf(1, 1), DataType.INT64)
        inputFeature12.loadArray(intArrayOf(1))
        val inputFeature13 = TensorBuffer.createFixedSize(intArrayOf(1, 1), DataType.INT64)
        inputFeature13.loadArray(intArrayOf(1))
        val inputFeature14 = TensorBuffer.createFixedSize(intArrayOf(1, 1), DataType.INT64)
        inputFeature14.loadArray(intArrayOf(1))
        val inputFeature15 = TensorBuffer.createFixedSize(intArrayOf(1, 1), DataType.INT64)
        inputFeature15.loadArray(intArrayOf(1))
        val inputFeature16 = TensorBuffer.createFixedSize(intArrayOf(1, 1), DataType.INT64)
        inputFeature16.loadArray(intArrayOf(1))
        val inputFeature17 = TensorBuffer.createFixedSize(intArrayOf(1, 1), DataType.INT64)
        inputFeature17.loadArray(intArrayOf(1))
        val inputFeature18 = TensorBuffer.createFixedSize(intArrayOf(1, 1), DataType.INT64)
        inputFeature18.loadArray(intArrayOf(1))
        val inputFeature19 = TensorBuffer.createFixedSize(intArrayOf(1, 1), DataType.INT64)
        inputFeature19.loadArray(intArrayOf(1))
        val inputFeature20 = TensorBuffer.createFixedSize(intArrayOf(1, 1), DataType.INT64)
        inputFeature20.loadArray(intArrayOf(1))
        val inputFeature21 = TensorBuffer.createFixedSize(intArrayOf(1, 1), DataType.INT64)
        inputFeature21.loadArray(intArrayOf(1))
        val inputFeature22 = TensorBuffer.createFixedSize(intArrayOf(1, 1), DataType.INT64)
        inputFeature22.loadArray(intArrayOf(1))
        val inputFeature23 = TensorBuffer.createFixedSize(intArrayOf(1, 1), DataType.INT64)
        inputFeature23.loadArray(intArrayOf(1))
        val inputFeature24 = TensorBuffer.createFixedSize(intArrayOf(1, 1), DataType.INT64)
        inputFeature24.loadArray(intArrayOf(1))
        val inputFeature25 = TensorBuffer.createFixedSize(intArrayOf(1, 1), DataType.INT64)
        inputFeature25.loadArray(intArrayOf(1))
        val inputFeature26 = TensorBuffer.createFixedSize(intArrayOf(1, 1), DataType.INT64)
        inputFeature26.loadArray(intArrayOf(1))
        val inputFeature27 = TensorBuffer.createFixedSize(intArrayOf(1, 1), DataType.INT64)
        inputFeature27.loadArray(intArrayOf(1))
        val inputFeature28 = TensorBuffer.createFixedSize(intArrayOf(1, 1), DataType.INT64)
        inputFeature28.loadArray(intArrayOf(1))
        val inputFeature29 = TensorBuffer.createFixedSize(intArrayOf(1, 1), DataType.INT64)
        inputFeature29.loadArray(intArrayOf(1))
        val inputFeature30 = TensorBuffer.createFixedSize(intArrayOf(1, 1), DataType.INT64)
        inputFeature30.loadArray(intArrayOf(1))
        val inputFeature31 = TensorBuffer.createFixedSize(intArrayOf(1, 1), DataType.INT64)
        inputFeature31.loadArray(intArrayOf(1))
        val inputFeature32 = TensorBuffer.createFixedSize(intArrayOf(1, 1), DataType.INT64)
        inputFeature32.loadArray(intArrayOf(1))

// Runs model inference and gets result.
        val outputs = model.process(inputFeature0, inputFeature1, inputFeature2, inputFeature3, inputFeature4, inputFeature5, inputFeature6, inputFeature7, inputFeature8, inputFeature9, inputFeature10, inputFeature11, inputFeature12, inputFeature13, inputFeature14, inputFeature15, inputFeature16, inputFeature17, inputFeature18, inputFeature19, inputFeature20, inputFeature21, inputFeature22, inputFeature23, inputFeature24, inputFeature25, inputFeature26, inputFeature27, inputFeature28, inputFeature29, inputFeature30, inputFeature31, inputFeature32)
        val outputFeature0 = outputs.outputFeature0AsTensorBuffer

// Releases model resources if no longer used.
        model.close()

        val calcValue = outputFeature0.intArray[0]

        return calcValue
    }

    /*
    @Throws(IOException::class)
    private fun loadModelFile(): MappedByteBuffer {


        val MODEL_ASSETS_PATH = "ml/sleep04model.tflite"
        val assetFileDescriptor = context.assets.openFd(MODEL_ASSETS_PATH)
        val fileInputStream = FileInputStream(assetFileDescriptor.fileDescriptor)
        val fileChannel = fileInputStream.channel
        val startoffset = assetFileDescriptor.startOffset
        val declaredLength = assetFileDescriptor.declaredLength
        val mapByteBuff = fileChannel.map(FileChannel.MapMode.READ_ONLY, startoffset, declaredLength)
        return mapByteBuff
    }

    private lateinit var interpreter:Interpreter

    fun classifySequence(sleepValues: Array<Int>): Int {
        interpreter = Interpreter(loadModelFile())
        val inputs : Array<Int> = sleepValues
        val output : Array<Int> = arrayOf(0)
        interpreter.run(inputs, output)
        return output[0]
    }

    fun close() {
        interpreter?.close()
    }*/
}