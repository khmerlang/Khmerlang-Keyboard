package com.rathanak.khmerroman.spelling_corrector

import android.content.Context
import android.util.Log
import com.rathanak.khmerroman.data.DataLoader
import com.rathanak.khmerroman.data.KeyboardPreferences
import com.rathanak.khmerroman.data.Ngram
import com.rathanak.khmerroman.spelling_corrector.bktree.Bktree
import com.rathanak.khmerroman.view.KhmerLangApp
import io.realm.Realm
import io.realm.Sort
import java.util.*


data class Candidate(var keyword: String, var score: Double, var distance: Int)
class SpellCorrector() {
    private var bkKH: Bktree = Bktree()
    private var bkEN: Bktree = Bktree()
    private var bkRM: Bktree = Bktree()
    private var specialCases = HashMap<String, String?>()

    fun reset() {
        bkKH = Bktree()
        bkRM = Bktree()
        bkEN = Bktree()
    }

    fun loadData() {
        var realm: Realm = Realm.getInstance(KhmerLangApp.dbConfig)
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
            if (it.other?.isNotEmpty() == true) {
                specialCases[it.keyword] = it.other
            }
        }

        realm.close()
    }

    fun addKhmerWord(keyword: String, roman: String) {
        bkKH.add(keyword, "")
        bkRM.add(roman, keyword)
    }

    fun removeKhmerWord(keyword: String, roman: String) {
//        bkKH.add(keyword, "")
//        bkRM.add(roman, keyword)
    }

    private fun specialKhmer(str: String): String {
        return str.replace("េី", "ើ").replace("េា", "ោ")
    }

    fun correct(prevOne: String, prevTwo: String, misspelling: String, isStartSen: Boolean): List<String> {
        if(misspelling.isEmpty()) {
            return emptyList()
        }

        var tolerance = 1
        if (misspelling.length <= 2) {
            tolerance = 1
        } else if (misspelling.length <= 5) {
            tolerance = 2
        } else if (misspelling.length <= 8) {
            tolerance = 3
        } else {
            tolerance = 4
        }

        if (misspelling[0] in 'ក'..'ឳ') {
            var outputKMMap: List<String> = mutableListOf()
            outputKMMap = correctBy(bkKH, specialKhmer(misspelling), false, KhmerLangApp.LANG_KH, tolerance, prevOne, prevTwo, isStartSen)
            return outputKMMap
        } else {
            val MAX_WORDS_SHOW = 4
            val isENChecked = KhmerLangApp.preferences?.getBoolean(KeyboardPreferences.KEY_EN_CORRECTION_MODE, true)
            val isRMChecked = KhmerLangApp.preferences?.getBoolean(KeyboardPreferences.KEY_RM_CORRECTION_MODE, true)
            if(!isENChecked!! && !isRMChecked!!) {
                return emptyList()
            }

            var outputENMap = listOf<String>()
            var outputRMMap = listOf<String>()
            if (isENChecked!!) {
                outputENMap = correctBy(bkEN, misspelling, false, KhmerLangApp.LANG_EN, tolerance, prevOne, prevTwo, isStartSen)
            }

            if (isRMChecked!!) {
                outputRMMap = correctBy(bkRM, misspelling, true, KhmerLangApp.LANG_KH, tolerance, prevOne, prevTwo, isStartSen)
            }

            val total = outputENMap.size + outputRMMap.size
            var suggestWords = Array(total) { "" }
            var index  = 0
            var rn_ptr = 0
            var en_ptr = 0

            while(index < suggestWords.size) {
                if(rn_ptr < outputRMMap.size) suggestWords[index++] = outputRMMap[rn_ptr++]
                if(rn_ptr < outputRMMap.size)suggestWords[index++] = outputRMMap[rn_ptr++]
                if(en_ptr < outputENMap.size) suggestWords[index++] = outputENMap[en_ptr++]
                if(en_ptr < outputENMap.size) suggestWords[index++] = outputENMap[en_ptr++]
            }

            return suggestWords.toList()
        }
    }

    private fun tokenizeWord(word: String, lang: Int): String {
        if(word.isEmpty()) {
            return "<oth>"
        }
        if ((word == "<s>") or (word == "<s> <s>")) {
            return word
        } else if ((word[0] in '0'..'9') or (word[0] in '០'..'៩')) {
            return "<num>"
        } else if ((word[0] in 'ក'..'ឳ') and (lang == KhmerLangApp.LANG_KH)) {
            return word
        } else if (((word[0] in 'a'..'z') or (word[0] in 'A'..'Z') ) and (lang == KhmerLangApp.LANG_EN)) {
            return word
        }

//        return "<unk>"
        return "<oth>"
    }

    private fun correctBy(model: Bktree, misspelling: String, isOther: Boolean, lang: Int, tolerance: Int, prevOne: String = "<s>", prevTwo: String = "<s>", isStartSen: Boolean): List<String> {
        if (model.root == null) {
            return arrayListOf()
        }

        val tokenOne = tokenizeWord(prevOne, lang)
        val tokenTwo = tokenizeWord(prevTwo, lang)
        var realm: Realm = Realm.getInstance(KhmerLangApp.dbConfig)
        var query = realm.where(Ngram::class.java);
//        query = query.equalTo("lang", lang)
        query = query.beginGroup()
        query = query.equalTo("keyword", "<s>")
        query = query.or().equalTo("keyword", "<s> <s>")
        query = query.or().equalTo("keyword", "<s> <s> <s>")
        val candidatesList = arrayListOf<Candidate>()
        model.getSpellSuggestion(model.root!!, misspelling.decapitalize(), tolerance).forEach { it ->
            val distance = it.distance
            if(isOther) {
                it.other.toLowerCase().split("_").forEach { it2 ->
                    candidatesList.add(Candidate(it2, 0.0, distance))
                    query = query.or().equalTo("keyword", it2)
                    query = query.or().equalTo("keyword", "$tokenTwo $it2")
                    query = query.or().equalTo("keyword", "$tokenOne $tokenTwo $it2")
                }
            } else {
                val word = it.word.toLowerCase()
                candidatesList.add(Candidate(word, 0.0, distance))
                query = query.or().equalTo("keyword", word)
                query = query.or().equalTo("keyword", "$tokenTwo $word")
                query = query.or().equalTo("keyword", "$tokenOne $tokenTwo $word")
            }
        }
        query = query.endGroup()
        query = query.and().equalTo("lang", lang)

        var resultDict = HashMap<String, Int>()
        query.findAll().forEach {
            resultDict[it.keyword] = it.count
        }

        candidatesList.forEach {
            val word = it.keyword
            val weight = if (it.distance <= 1) {
                1.0
            } else if (it.distance <= 3) {
                0.8
            } else {
                0.7
            }
            if (it.distance == 0) {
                it.score = 1.0
            } else if (resultDict["$tokenOne $tokenTwo $word"] != null && resultDict["<s> <s> <s>"] != null) {
                it.score = weight * resultDict["$tokenOne $tokenTwo $word"]!! / resultDict["<s> <s> <s>"]!!
            } else if (resultDict["$tokenTwo $word"] != null && resultDict["<s> <s>"] != null) {
                it.score = 0.4 * weight * resultDict.get("$tokenTwo $word")!! / resultDict.get("<s> <s>")!!
            } else if (resultDict[word] != null && resultDict["<s>"] != null) {
                it.score = 0.4 * 0.4 * weight * resultDict[word]!! / resultDict["<s>"]!!
            }

            if (specialCases[it.keyword]?.isNotEmpty() == true) {
                it.keyword = specialCases[it.keyword].toString()
            } else if (lang == KhmerLangApp.LANG_EN && isStartSen) {
                it.keyword = it.keyword.capitalize()
            }
        }

        candidatesList.sortByDescending { it.score }
        realm.close()
        return candidatesList.map { it.keyword }.distinctBy { it.toLowerCase() }.take(10)
    }
}