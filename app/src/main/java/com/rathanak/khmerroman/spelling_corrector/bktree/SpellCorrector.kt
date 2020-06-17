package com.rathanak.khmerroman.spelling_corrector.bktree

import android.content.Context
import android.util.Log
import com.rathanak.khmerroman.spelling_corrector.PQElement
import java.util.*
import kotlin.collections.LinkedHashMap

class SpellCorrector() {
    private var bk: Bktree = Bktree()

    fun loadData(context: Context, filePart: String) {
        try {
            context.assets.open(filePart).bufferedReader().useLines {
                    lines -> lines.forEach {
                val word = it.split("\\s".toRegex())//split(",")
                bk.add(word[0].trim(), word[1].toInt())
            }
            }
        } catch (ex:Exception){
            Log.e("read_file", ex.localizedMessage)
        }

    }

    fun correct(misspelling: String): LinkedHashMap<String, Int> {
        var outputMap: LinkedHashMap<String, Int> = LinkedHashMap<String, Int>()
        for(word in bk.getSpellSuggestion(misspelling, 3, 9)) {
            outputMap[word] = 1
        }
        return outputMap
    }
}