package com.rathanak.khmerroman.spelling_corrector

import android.content.Context
import com.rathanak.khmerroman.data.DataLoader
import com.rathanak.khmerroman.data.KeyboardPreferences
import com.rathanak.khmerroman.data.Ngram
import com.rathanak.khmerroman.serializable.NgramRecordSerializable
import com.rathanak.khmerroman.spelling_corrector.bktree.Bktree
import com.rathanak.khmerroman.spelling_corrector.edit_distance.LevenshteinDistance
import com.rathanak.khmerroman.view.KhmerLangApp
import io.realm.Realm
import io.realm.Sort
import java.util.*
import kotlin.collections.ArrayList

class SpellCorrector() {
    private var bkKH: Bktree = Bktree()
    private var bkEN: Bktree = Bktree()
    private var bkRM: Bktree = Bktree()

    fun reset() {
        bkKH = Bktree()
        bkRM = Bktree()
        bkEN = Bktree()
    }

    fun loadData(context: Context) {
        var realm = Realm.getDefaultInstance()
        val khWordList = realm.where(Ngram::class.java)
            .equalTo("gram", KhmerLangApp.ONE_GRAM)
            .equalTo("lang", KhmerLangApp.LANG_KH)
            .notEqualTo("keyword", "<s>")
            .findAll()
            .sort("count", Sort.DESCENDING)
        khWordList.forEach {
            bkKH.add(it.keyword, "")
            if (it.other?.isNotEmpty() == true) {
                it.other!!.split(DataLoader.SEPERATOR).forEach { it1 -> bkRM.add(it1, it.keyword) }
            }
        }
        val enWordList = realm.where(Ngram::class.java)
            .equalTo("gram", KhmerLangApp.ONE_GRAM)
            .equalTo("lang", KhmerLangApp.LANG_EN)
            .notEqualTo("keyword", "<s>")
            .findAll()
            .sort("count", Sort.DESCENDING)
        enWordList.forEach {
            bkEN.add(it.keyword, "")
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

        var tolerance = 1
        if (misspelling.length <= 2) {
            tolerance = 2
        } else if (misspelling.length <= 4) {
            tolerance = 3
        } else if (misspelling.length <= 6) {
            tolerance = 4
        } else {
            tolerance = 5
        }

//        previousWord
        if (misspelling[0] in 'ក'..'ឳ') {
            var outputKMMap: List<String> = mutableListOf()
            outputKMMap = correctBy(bkKH, specialKhmer(misspelling), false, tolerance)
            return outputKMMap.distinctBy { it.toLowerCase() }
        } else {
            val MAX_WORDS_SHOW = 4
            val suggestWords: MutableList<String> = mutableListOf()
            val isENChecked = KhmerLangApp.preferences?.getBoolean(KeyboardPreferences.KEY_EN_CORRECTION_MODE, true)
            val isRMChecked = KhmerLangApp.preferences?.getBoolean(KeyboardPreferences.KEY_RM_CORRECTION_MODE, true)
            if(!isENChecked!! && !isRMChecked!!) {
                return emptyList()
            }

            var outputENMap = arrayListOf<String>()
            var outputRMMap = arrayListOf<String>()
            if (isENChecked!!) {
                outputENMap = correctBy(bkEN, misspelling, false, tolerance)
            }

            if (isRMChecked!!) {
                outputRMMap = correctBy(bkRM, misspelling, true, tolerance)
            }

            val testEN = outputENMap.chunked(MAX_WORDS_SHOW - 2)
            val testRM = outputRMMap.chunked(MAX_WORDS_SHOW - 2)
            for (i in 0..10) {
                if (testRM.size > i) {
                    suggestWords += testRM[i]
                }

                if (testEN.size > i) {
                    suggestWords += testEN[i]
                }
            }

            return suggestWords
        }
    }

    private fun correctBy(model: Bktree, misspelling: String, isOther: Boolean, tolerance: Int): ArrayList<String> {
        if (model.root == null) {
            return arrayListOf()
        }

        var maxLimit = 10
        var outputMap = arrayListOf<String>()
        model.getSpellSuggestion(model.root!!, misspelling.decapitalize(), tolerance).forEach {
            if(isOther) {
                outputMap.add(it.other)
            } else {
                outputMap.add(it.word)
            }
        }
        return outputMap
    }
}