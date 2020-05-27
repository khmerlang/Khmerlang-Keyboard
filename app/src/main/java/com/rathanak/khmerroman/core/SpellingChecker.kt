package com.rathanak.khmerroman.core

import android.content.Context
import android.util.Log

class SpellingChecker(private val context: Context, private val filePart: String) {
    private var bk: Bktree = Bktree()

    fun loadData() {
        try {
            context.assets.open(filePart).bufferedReader().useLines {
                    lines -> lines.forEach {
                        val word = it.split(",")
                        bk.add(word[0].trim(), word[1].toInt())
                    }
            }
        } catch (ex:Exception){
            Log.e("read_file", ex.localizedMessage)
        }

    }

    fun getSuggestion(misspelling: String) : List<String> {

        return bk.getSpellSuggestion(misspelling, 2)
    }
}