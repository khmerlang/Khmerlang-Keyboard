package com.rathanak.khmerroman.core

class Bktree {
    private var levenshtein: Levenshtein? = null

    init {
        levenshtein = Levenshtein(1,1,1,1)
    }
    fun Add(str: String) {}

    fun Search(str: String, maxChange: Int) {}

    fun Suggestion(str: String) {}

    fun Save() {}
    fun Load() {}
    // save new or update
    // load from save
}