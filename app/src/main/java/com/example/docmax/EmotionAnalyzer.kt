package com.example.docmax

import android.content.Context
import org.tensorflow.lite.Interpreter
import java.io.FileInputStream
import java.nio.MappedByteBuffer
import java.nio.channels.FileChannel


class EmotionAnalyzer(context: Context) {
    private val interpreter: Interpreter
    private val tokenizer = Tokenizer(context)

    init {
        val options = Interpreter.Options()
        options.setUseXNNPACK(false) // 🛑 disable XNNPack for stability
        options.setNumThreads(2)     // ✅ use 2 threads only

        interpreter = Interpreter(loadModelFile(context), options) // ✅ pass options here
    }

    private fun loadModelFile(context: Context): MappedByteBuffer {
        val fileDescriptor = context.assets.openFd("model.tflite")
        val inputStream = FileInputStream(fileDescriptor.fileDescriptor)
        val fileChannel = inputStream.channel
        return fileChannel.map(
            FileChannel.MapMode.READ_ONLY,
            fileDescriptor.startOffset,
            fileDescriptor.declaredLength
        )
    }

    fun analyze(text: String): String {
        val (inputIds, attentionMask) = tokenizer.tokenize(text)

        val inputs = arrayOf(
            arrayOf(inputIds),        // [1, 128]
            arrayOf(attentionMask)    // [1, 128]
        )

        val output = HashMap<Int, Any>()
        val result = Array(1) { FloatArray(4) }
        output[0] = result

        interpreter.runForMultipleInputsOutputs(inputs, output)

        val emotions = listOf("joy", "sadness", "anger", "fear")// ✅ must match the model's order
        val predictedIndex = result[0].indices.maxByOrNull { result[0][it] } ?: -1

        return if (predictedIndex in emotions.indices) emotions[predictedIndex] else "unknown"
    }
}