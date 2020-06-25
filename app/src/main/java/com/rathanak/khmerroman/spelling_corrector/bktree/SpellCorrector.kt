package com.rathanak.khmerroman.spelling_corrector.bktree

import android.content.Context
import android.util.Log
import com.rathanak.khmerroman.spelling_corrector.PQElement
import com.rathanak.khmerroman.spelling_corrector.getEditDistance
import java.util.*
import kotlin.collections.LinkedHashMap

class SpellCorrector() {
    private var bk: Bktree = Bktree()

    fun loadData(context: Context, filePart: String) {
        bk = Bktree()
        try {
            context.assets.open(filePart).bufferedReader().useLines {
                    lines -> lines.forEach {
                val word = it.split("\\s".toRegex())//split(",")
                bk.add(word[0].trim(), word[1].toInt(), word[2])
            }
            }
        } catch (ex:Exception){
            Log.e("read_file", ex.localizedMessage)
        }

    }

    fun correct(misspelling: String): LinkedHashMap<String, Int> {
        var outputMap: LinkedHashMap<String, Int> = LinkedHashMap<String, Int>()
//        for(word in bk.getSpellSuggestion(misspelling, 3, 9)) {
//            if (getEditDistance(misspelling, word) <= 3) {
//                outputMap[word] = 1
//            }
//        }
//        return outputMap

        if (bk.root != null) {
            val limit = 10
            var suggestion = bk.getSpellSuggestion(bk.root!!, misspelling.decapitalize(), 3)
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
                                outputMap[element.other] = 1
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