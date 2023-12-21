package com.rathanak.khmerroman.keyboard.smartbar

class WordSuggestion(val word: String, val startPos: Int, val endPos: Int) {}
class SpellSuggestionItem(val typoWord: String, val wordsSuggestion: List<WordSuggestion>) {}
