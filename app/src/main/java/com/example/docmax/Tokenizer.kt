package com.example.docmax

import android.content.Context
import java.io.BufferedReader
import java.io.InputStreamReader
import java.util.*

class Tokenizer(context: Context) {

    private val vocab: Map<String, Int>
    private val maxLen = 128

    init {
        vocab = loadVocab(context)
    }

    private fun loadVocab(context: Context): Map<String, Int> {
        val vocabMap = mutableMapOf<String, Int>()
        val reader = BufferedReader(InputStreamReader(context.assets.open("vocab.txt")))
        var line: String?
        var index = 0
        while (reader.readLine().also { line = it } != null) {
            vocabMap[line!!] = index++
        }
        reader.close()
        return vocabMap
    }

    fun tokenize(text: String): Pair<IntArray, IntArray> {
        val tokens = mutableListOf<String>()
        val cleaned = text.lowercase(Locale.getDefault())
            .replace("[^a-z0-9 ]".toRegex(), "")
            .split(" ")

        tokens.add("[CLS]")
        for (word in cleaned) {
            if (vocab.containsKey(word)) {
                tokens.add(word)
            } else {
                tokens.add("[UNK]")
            }
        }
        tokens.add("[SEP]")

        val inputIds = IntArray(maxLen) { 0 }
        val attentionMask = IntArray(maxLen) { 0 }

        for (i in tokens.indices) {
            if (i >= maxLen) break
            inputIds[i] = vocab[tokens[i]] ?: vocab["[UNK]"] ?: 0
            attentionMask[i] = 1
        }

        return Pair(inputIds, attentionMask)
    }
}