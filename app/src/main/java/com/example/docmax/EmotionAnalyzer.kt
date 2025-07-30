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

    private val emotionKeywords = mapOf(
        "senang" to "joy",
        "bahagia" to "joy",
        "gembira" to "joy",
        "sedih" to "sadness",
        "capek" to "sadness",
        "lelah" to "sadness",
        "marah" to "anger",
        "kesal" to "anger",
        "takut" to "fear",
        "cemas" to "fear"
    )

    fun analyze(text: String): String {
        val lowered = text.lowercase()

        // Prioritaskan deteksi keyword dulu
        for ((keyword, emotion) in emotionKeywords) {
            if (lowered.contains(keyword)) return emotion
        }

        // Jika tidak ada keyword cocok, pakai model MobileBERT
        val (inputIds, attentionMask) = tokenizer.tokenize(text)

        val inputs = arrayOf(
            arrayOf(inputIds),        // [1, 128]
            arrayOf(attentionMask)    // [1, 128]
        )

        val output = HashMap<Int, Any>()
        val result = Array(1) { FloatArray(4) }
        output[0] = result

        interpreter.runForMultipleInputsOutputs(inputs, output)

        val emotions = listOf("joy", "sadness", "anger", "fear")
        val predictedIndex = result[0].indices.maxByOrNull { result[0][it] } ?: -1

        return if (predictedIndex in emotions.indices) emotions[predictedIndex] else "unknown"
    }

}