package com.rathanak.khmerroman.spelling_corrector.bktree

import android.content.Context
import android.util.Log
import com.rathanak.khmerroman.data.KeyboardPreferences
import com.rathanak.khmerroman.spelling_corrector.EnglishKhmerMap
import com.rathanak.khmerroman.spelling_corrector.PQElement
import com.rathanak.khmerroman.spelling_corrector.getEditDistance
import com.rathanak.khmerroman.view.Roman2KhmerApp
import java.util.*
import kotlin.collections.LinkedHashMap

class SpellCorrector() {
    private var bKM: Bktree = Bktree()
    private var bRM: Bktree = Bktree()
    private var bEN: Bktree = Bktree()

    fun reset() {
        bKM = Bktree()
        bRM = Bktree()
        bEN = Bktree()
    }

    fun loadData(context: Context) {
        bRM = readModel(context, Roman2KhmerApp.khmerWordsFile, true, true)
        bEN = readModel(context, Roman2KhmerApp.englishWordsFile, false, false)
        bKM = readModel(context, Roman2KhmerApp.khmerWordsFile, false, false)
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

    private fun isENString(str: String) : Boolean {
        return (str[0] in 'a'..'z' || str[0] in 'A'..'Z')
    }

    private fun en2Khmer(str: String): String {
        var khmerStr = ""
        for(ch in str) {
            if(ch in EnglishKhmerMap.en2Khmer.keys) {
                khmerStr += EnglishKhmerMap.en2Khmer[ch]
            }
        }
        return khmerStr
    }

    private fun km2English(str: String) : String {
        var englishStr = ""
        for(ch in str) {
            if(ch in EnglishKhmerMap.kh2English.keys) {
                englishStr += EnglishKhmerMap.kh2English[ch]
            }
        }
        return englishStr
    }

    private fun specialKhmer(str: String): String {
        return str.replace("េី", "ើ").replace("េា", "ោ")
    }

    fun correct(previousWord: String, misspelling: String): List<String> {
//        previousWord
        val isKMChecked = Roman2KhmerApp.preferences?.getBoolean(KeyboardPreferences.KEY_KM_CORRECTION_MODE, false)
        val isENChecked = Roman2KhmerApp.preferences?.getBoolean(KeyboardPreferences.KEY_EN_CORRECTION_MODE, true)
        val isRMChecked = Roman2KhmerApp.preferences?.getBoolean(KeyboardPreferences.KEY_RM_CORRECTION_MODE, true)
        val arrStatus = listOf(isENChecked!!, isKMChecked!!, isRMChecked!!)
        var count = 0
        var tolerance = 3
        var limitResult = 10
        val MAX_WORDS_SHOW = 4
        for(status in arrStatus) {
            if(status) {
                count += 1
            }
        }

        return if (isENString(misspelling)) {
            if(count > 2) {
                tolerance = 2
                limitResult = 6
            }
            var outputENMap: List<String> = mutableListOf()
            var outputRMMap: List<String> = mutableListOf()
            var outputKMMap: List<String> = mutableListOf()
            if (isENChecked) {
                outputENMap = correctBy(bEN, misspelling, limitResult, false, tolerance)
            }

            if (isRMChecked) {
                outputRMMap = correctBy(bRM, misspelling, limitResult, true, tolerance)
            }

            if (isKMChecked) {
                outputKMMap = correctBy(bKM, specialKhmer(en2Khmer(misspelling)), limitResult, false, tolerance)
            }

            val suggestWords: MutableList<String> = mutableListOf()
            val testEN = outputENMap.chunked(MAX_WORDS_SHOW - count)
            val testKM = outputKMMap.chunked(MAX_WORDS_SHOW - count)
            val testRM = outputRMMap.chunked(MAX_WORDS_SHOW - count)
            for (i in 0..10) {
                if(testEN.size > i) {
                    suggestWords += testEN[i]
                }
                if(testKM.size > i) {
                    suggestWords += testKM[i]
                }
                if(testRM.size > i) {
                    suggestWords += testRM[i]
                }
            }

            suggestWords
        } else {
            if(count >= 2) {
                tolerance = 2
                limitResult = 6
            }
            var outputKMMap: List<String> = mutableListOf()
            var outputENMap: List<String> = mutableListOf()
            if (isKMChecked) {
                outputKMMap = correctBy(bKM, specialKhmer(misspelling), limitResult, false, tolerance)
            }

            if (isENChecked) {
                outputENMap = correctBy(bEN, specialKhmer(km2English(misspelling)), limitResult, false, tolerance)
            }
            val suggestWords: MutableList<String> = mutableListOf()

            val testEN = outputENMap.chunked(MAX_WORDS_SHOW - count)
            val testKM = outputKMMap.chunked(MAX_WORDS_SHOW - count)
            for (i in 0..10) {
                if(testKM.size > i) {
                    suggestWords += testKM[i]
                }
                if(testEN.size > i) {
                    suggestWords += testEN[i]
                }
            }
            suggestWords
        }.distinctBy { it.toLowerCase() }
    }

    fun correctBy(model: Bktree, misspelling: String, limit: Int, isOther: Boolean, tolerance: Int): List<String> {
        var outputMap: MutableList<String> = mutableListOf()
        if (model.root != null) {
            var suggestion = model.getSpellSuggestion(model.root!!, misspelling.decapitalize(), tolerance)
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
                                    for (word in element.other.split("_")) {
                                        outputMap.add(word)
                                        i++
                                    }
                                } else {
                                    outputMap.add(element.word)
                                    i++
                                }
                            }
                        }
                    }
                }
            }
        }

        return outputMap
    }
}