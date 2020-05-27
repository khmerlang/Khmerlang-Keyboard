package com.rathanak.khmerroman.core

import android.content.Context
import android.util.Log

class SpellingChecker(private val context: Context, private val filePart: String) {
    private var bk: Bktree = Bktree()

    fun loadData() {
        try {
            val lineList = mutableListOf<String>()
            context.assets.open(filePart).bufferedReader().useLines { lines -> lines.forEach { lineList.add(it) } }
            lineList.forEach {
                var word = it.trim()
                if(!word.isEmpty()) {
                    bk.add(word)
                }
            }
        } catch (ex:Exception){
            Log.e("read_file", ex.localizedMessage)
        }

    }

    fun getSuggestion(misspelling: String) : List<String> {

        return bk.getSpellSuggestion(misspelling, 1)
    }
}