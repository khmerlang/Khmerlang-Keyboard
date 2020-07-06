package com.rathanak.khmerroman.spelling_corrector.bktree

import android.content.Context
import android.util.Log
import com.rathanak.khmerroman.spelling_corrector.PQElement
import com.rathanak.khmerroman.spelling_corrector.getEditDistance
import java.util.*
import kotlin.collections.LinkedHashMap

class SpellCorrector() {
    private var bk: Bktree = Bktree()
    private var bkEN: Bktree = Bktree()
    private var isRoman = false

    fun reset() {
        bkEN = Bktree()
        bk = Bktree()
    }

    fun loadData(context: Context, roman: Boolean) {
        isRoman = roman
        if(isRoman) {
            bk = readModel(context, "khmer_words_freq_roman.txt", true, true)
            bkEN = readModel(context, "final_words_v2.txt", false, false)
        } else {
            bk = readModel(context, "khmer_words_freq_roman.txt", false, false)
            bkEN = Bktree()
        }
    }

    private fun readModel(context: Context, filePart: String, isOther: Boolean, wordLast: Boolean): Bktree {
        var model = Bktree()
        var indexWord = 0
        var indexOther = 2
        if(wordLast) {
            indexWord = 2
            indexOther = 0
        }
        try {
            context.assets.open(filePart).bufferedReader().useLines { lines -> lines.forEach {
                    val word = it.split("\\s".toRegex())//split(",")
                    val word_1 = word[indexWord].trim()
                    val freq = word[1].toInt()
                    val other = if (isOther && word.size == 3) {
                        word[indexOther]
                    } else {
                        ""
                    }
                    model.add(word_1, freq, other)
                }
            }
        } catch (ex:Exception){
            Log.e("read_file", ex.localizedMessage)
        }

        return model
    }

    fun correct(misspelling: String): List<String> {
        return if (isRoman) {
            var outputENMap = correctBy(bkEN, misspelling, 10, false)
            var outputMap =correctBy(bk, misspelling, 10, true)
            outputENMap.take(1) + outputMap //+ outputENMap.takeLast(outputENMap.size - 1)
        } else {
            correctBy(bk, misspelling, 10, false)
        }
    }

    fun correctBy(model: Bktree, misspelling: String, limit: Int, isOther: Boolean): List<String> {
        var outputMap: MutableList<String> = mutableListOf()
        if (model.root != null) {
            var suggestion = model.getSpellSuggestion(model.root!!, misspelling.decapitalize(), 3)
            var result: MutableMap<Int, PriorityQueue<PQElement>> = mutableMapOf()
            suggestion.forEach {
                if (result[it.distance].isNullOrEmpty()) {
                    result[it.distance] = PriorityQueue<PQElement>(5)
                }
                result[it.distance]?.add(PQElement(it.word, it.distance, it.freq.toString(), it.other))
            }
            val totalKeys = result.keys.size
            if (totalKeys > 0) {
                val takeEach = limit / totalKeys
                for(key in result.keys.sorted()) {
                    var i = 0
                    var suggestedWords = result[key]
                    if (suggestedWords != null) {
                        while (!suggestedWords.isEmpty() && i < takeEach) {
                            var element = suggestedWords.poll()
                            if (getEditDistance(misspelling, element.word) <= 3) {
                                if (isOther) {
                                    outputMap.add(element.other)
                                } else {
                                    outputMap.add(element.word)
                                }
                                i++
                            }
                        }
                    }
                }
            }
        }
        return outputMap
    }
}