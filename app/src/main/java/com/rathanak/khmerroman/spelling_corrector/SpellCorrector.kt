package com.rathanak.khmerroman.spelling_corrector

import android.content.Context
import android.util.Log
import com.rathanak.khmerroman.data.DataLoader
import com.rathanak.khmerroman.data.KeyboardPreferences
import com.rathanak.khmerroman.data.Ngram
import com.rathanak.khmerroman.spelling_corrector.bktree.Bktree
import com.rathanak.khmerroman.view.KhmerLangApp
import io.realm.Case
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

    fun loadData(realm: Realm) {
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
    }

    fun addKhmerWord(keyword: String, roman: String) {
        bkKH.add(keyword, "")
        bkRM.add(roman, keyword)
    }

    private fun specialKhmer(str: String): String {
        return str.replace("េី", "ើ").replace("េា", "ោ")
    }

    fun getNextWords(realm: Realm, prevOne: String, prevTwo: String): List<String> {
        var lang = KhmerLangApp.LANG_KH
        if (prevTwo.isNotEmpty()) {
            if (prevTwo[0] in 'a'..'z' || prevTwo[0] in 'A'..'Z' || prevTwo[0] in '0'..'9') {
                lang = KhmerLangApp.LANG_EN
            }
        }

        val tokenOne = tokenizeWord(prevOne, lang)
        val tokenTwo = tokenizeWord(prevTwo, lang)

        var result = realm.where(Ngram::class.java)
            .like("keyword", "$tokenOne $tokenTwo *", Case.INSENSITIVE)
            .equalTo("gram", KhmerLangApp.THREE_GRAM)
            .limit(10)
            .findAll()
            .sort("count", Sort.DESCENDING)
            .map {
                val word = it.keyword.subSequence("$tokenOne $tokenTwo ".length, it.keyword.length).toString()
                if (specialCases[word]?.isNotEmpty() == true) {
                    specialCases[word].toString()
                } else {
                    word
                }
            }

        if(result.size < 10) {
            result += realm.where(Ngram::class.java)
                .equalTo("gram", KhmerLangApp.TWO_GRAM)
                .like("keyword", "$tokenTwo *", Case.INSENSITIVE)
                .limit((10 - result.size).toLong())
                .findAll()
                .sort("count", Sort.DESCENDING)
                .map {
                    val word = it.keyword.subSequence("$tokenTwo ".length, it.keyword.length).toString()
                    if (specialCases[word]?.isNotEmpty() == true) {
                        specialCases[word].toString()
                    } else {
                        word
                    }
                }
        }

        return result.distinctBy { it.toLowerCase() }
    }

    fun correct(realm: Realm, prevOne: String, prevTwo: String, misspelling: String, isStartSen: Boolean): List<String> {
        if(misspelling.isEmpty()) {
            return emptyList()
        }

        var tolerance = 1
        if (misspelling.length <= 2) {
            tolerance = 1
        } else if (misspelling.length <= 4) {
            tolerance = 2
        } else if (misspelling.length <= 8) {
            tolerance = 3
        } else {
            tolerance = 4
        }

        if (misspelling[0] in 'ក'..'ឳ') {
            var outputKMMap: List<String> = mutableListOf()
            outputKMMap = correctBy(realm, bkKH, specialKhmer(misspelling), false, KhmerLangApp.LANG_KH, tolerance, prevOne, prevTwo, isStartSen)
            return outputKMMap
        } else {
            val isENChecked = KhmerLangApp.preferences?.getBoolean(KeyboardPreferences.KEY_EN_CORRECTION_MODE, true)
            val isRMChecked = KhmerLangApp.preferences?.getBoolean(KeyboardPreferences.KEY_RM_CORRECTION_MODE, true)
            if(!isENChecked!! && !isRMChecked!!) {
                return emptyList()
            }

            var outputENMap = listOf<String>()
            var outputRMMap = listOf<String>()
            if (isENChecked!!) {
                outputENMap = correctBy(realm, bkEN, misspelling, false, KhmerLangApp.LANG_EN, tolerance, prevOne, prevTwo, isStartSen)
            }

            if (isRMChecked!!) {
                outputRMMap = correctBy(realm, bkRM, misspelling, true, KhmerLangApp.LANG_KH, tolerance, prevOne, prevTwo, isStartSen)
            }

            val total = outputENMap.size + outputRMMap.size
            var suggestWords = Array(total) { "" }
            var index  = 0
            var rnPtr = 0
            var enPtr = 0

            while(index < suggestWords.size) {
                if(rnPtr < outputRMMap.size) suggestWords[index++] = outputRMMap[rnPtr++]
                if(rnPtr < outputRMMap.size)suggestWords[index++] = outputRMMap[rnPtr++]
                if(enPtr < outputENMap.size) suggestWords[index++] = outputENMap[enPtr++]
                if(enPtr < outputENMap.size) suggestWords[index++] = outputENMap[enPtr++]
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

    private fun correctBy(realm: Realm, model: Bktree, misspelling: String, isOther: Boolean, lang: Int, tolerance: Int, prevOne: String = "<s>", prevTwo: String = "<s>", isStartSen: Boolean): List<String> {
        if (model.root == null) {
            return arrayListOf()
        }

        val tokenOne = tokenizeWord(prevOne, lang)
        val tokenTwo = tokenizeWord(prevTwo, lang)
        var query = realm.where(Ngram::class.java)
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
            val weight = if (it.distance < 1) {
                1.0
            } else if (it.distance <= 1) {
                0.95
            } else if (it.distance <= 2) {
                0.9
            } else if (it.distance <= 3) {
                0.6
            } else {
                0.5
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
        return candidatesList.map { it.keyword }.distinctBy { it.toLowerCase() }.take(10)
    }
}