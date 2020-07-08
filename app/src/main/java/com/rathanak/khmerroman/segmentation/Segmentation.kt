package com.rathanak.khmerroman.segmentation

import android.content.Context
import android.util.Log
import com.rathanak.khmerroman.view.Roman2KhmerApp

class Segmentation {
    private var model: Trie = Trie()

    fun reset() {
        model = Trie()
    }

    private fun isNumber(ch: Char) : Boolean {
        return ch in "0123456789០១២៣៤៥៦៧៨៩"
    }

    private fun isEnglish(ch: Char) : Boolean {
        return ch in "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ"
    }

    private fun parseNumber(index: Int, text: String): String {
        var result = ""
        var tmpIndex = index
        while (tmpIndex < text.length) {
            val ch = text[tmpIndex]
            if (isNumber(ch)) {
                result += text[tmpIndex]
                tmpIndex += 1
            } else {
                return result
            }
        }

        return result
    }

    private fun parseEnglish(index: Int, text: String): String {
        var result = ""
        var tmpIndex = index
        while (tmpIndex < text.length) {
            val ch = text[tmpIndex]
            if (isEnglish(ch) or isNumber(ch)) {
                result += text[tmpIndex]
                tmpIndex += 1
            } else {
                return result
            }
        }

        return result
    }

    private fun parseTrie(index: Int, text: String): String {
        var tmpIndex = index
        var word = ""
        var foundWord = ""
        while (tmpIndex < text.length) {
            var ch = text[tmpIndex]
            word += ch

            if (model.searchPrefix(word)) {
                if (model.search(word)) {
                    foundWord = word
                }
            } else if (model.search(word)) {
                return word
            } else {
                return foundWord
            }

            tmpIndex += 1
        }

        return foundWord
    }

    fun forwardSegment(text: String) : List<String> {
        var startIndex = 0
        var errorword = ""
        var result: MutableList<String> = mutableListOf()
        while (startIndex < text.length) {
            val ch = text[startIndex]
            var word = ""
            if(isNumber(ch)) {
                word = parseNumber(startIndex, text)
            } else if (isEnglish(ch)) {
                word = parseEnglish(startIndex, text)
            } else {
                word = parseTrie(startIndex, text)
            }

            val length = word.length
            if(length == 0) {
                startIndex += 1
                errorword += ch
                continue
            }

            if (errorword.isNotEmpty()) {
                result.add(errorword)
                errorword = ""
            }

            result.add(word)
            startIndex += length
        }
        if (errorword.isNotEmpty()) {
            result.add(errorword)
            errorword = ""
        }

        return result
    }

    fun loadData(context: Context) {
        try {
            context.assets.open(Roman2KhmerApp.khmerWordsFile).bufferedReader().useLines { lines -> lines.forEach {
                val word = it.split("\\s".toRegex())//split(",")
                model.insert(word[0].trim())
            }
            }
        } catch (ex:Exception){
            Log.e("read_file", ex.localizedMessage)
        }
    }
}