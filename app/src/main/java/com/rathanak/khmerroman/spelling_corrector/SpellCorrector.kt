package com.rathanak.khmerroman.spelling_corrector

import android.content.Context
import android.util.Log
import com.rathanak.khmerroman.data.KeyboardPreferences
import com.rathanak.khmerroman.data.Ngram
import com.rathanak.khmerroman.spelling_corrector.bktree.Bktree
import com.rathanak.khmerroman.spelling_corrector.edit_distance.LevenshteinDistance
import com.rathanak.khmerroman.view.Roman2KhmerApp
import io.realm.Realm
import java.util.*

class SpellCorrector() {
    private var bkKH: Bktree = Bktree()
    private var bkEN: Bktree = Bktree()
    private var bkRM: Bktree = Bktree()
    var isSpellDataExist = true

    fun reset() {
        bkKH = Bktree()
        bkRM = Bktree()
        bkEN = Bktree()
        isSpellDataExist = false
    }

    fun loadData(context: Context) {
        isSpellDataExist = true
        var realm = Realm.getDefaultInstance()
        val khWordList = realm.where(Ngram::class.java)
            // .equalTo("is_custom", isCustom)
            .equalTo("gram", Roman2KhmerApp.ONE_GRAM)
            .equalTo("lang", Roman2KhmerApp.LANG_KH)
            .findAll()
            .sort("keyword")
        khWordList.forEach {
            bkKH.add(it.keyword, it.count, "")
            it.roman?.let { it1 -> bkRM.add(it1, it.count, it.keyword) }
        }
        val enWordList = realm.where(Ngram::class.java)
            // .equalTo("is_custom", isCustom)
            .equalTo("gram", Roman2KhmerApp.ONE_GRAM)
            .equalTo("lang", Roman2KhmerApp.LANG_EN)
            .findAll()
            .sort("keyword")
        enWordList.forEach {
            bkEN.add(it.keyword, it.count, "")
        }

        if (!(khWordList.size > 0 || enWordList.size > 0)) {
            isSpellDataExist = false
        }

        realm.close()
    }

    private fun specialKhmer(str: String): String {
        return str.replace("េី", "ើ").replace("េា", "ោ")
    }

    fun correct(previousWord: String, misspelling: String): List<String> {
        if(misspelling.isEmpty()) {
            return emptyList()
        }

//        previousWord
        var limitResult = 10
        var tolerance = 3
        if (misspelling[0] in 'ក'..'ឳ') {
            var outputKMMap: List<String> = mutableListOf()
            outputKMMap = correctBy(bkKH, specialKhmer(misspelling), limitResult, false, tolerance)
            return outputKMMap.distinctBy { it.toLowerCase() }
        } else {
            val MAX_WORDS_SHOW = 4
            val suggestWords: MutableList<String> = mutableListOf()
            val isENChecked = Roman2KhmerApp.preferences?.getBoolean(KeyboardPreferences.KEY_EN_CORRECTION_MODE, true)
            val isRMChecked = Roman2KhmerApp.preferences?.getBoolean(KeyboardPreferences.KEY_RM_CORRECTION_MODE, true)
            if(!isENChecked!! && !isRMChecked!!) {
                return emptyList()
            }

            var outputENMap: List<String> = mutableListOf()
            var outputRMMap: List<String> = mutableListOf()

            if (isENChecked!!) {
                outputENMap = correctBy(bkEN, misspelling, limitResult, false, tolerance)
            }

            if (isRMChecked!!) {
                outputRMMap = correctBy(bkRM, misspelling, limitResult, true, tolerance)
            }

            val testEN = outputENMap.chunked(MAX_WORDS_SHOW - 2)
            val testRM = outputRMMap.chunked(MAX_WORDS_SHOW - 2)
            for (i in 0..10) {
                if (testEN.size > i) {
                    suggestWords += testEN[i]
                }

                if (testRM.size > i) {
                    suggestWords += testRM[i]
                }
            }

            return suggestWords.distinctBy { it.toLowerCase() }
        }
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
                            if (LevenshteinDistance(misspelling, element.word) <= 3) {
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