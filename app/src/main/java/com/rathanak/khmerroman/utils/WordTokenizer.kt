package com.rathanak.khmerroman.utils

class WordTokenizer {
    fun tokenize(inputText: String): Array<String> {
        val words: MutableList<String> = arrayListOf()
        inputText.split("[\n\t\b​ ]".toRegex()).forEach {
            var tmp = it
            if (tmp.length <= 1) {
                words.add(tmp)
            } else {
                if (CHAR_SYMBOL.contains(tmp[0])) {
                    words.add(tmp[0].toString())
                    tmp = tmp.substring(1, tmp.length)
                }
                if (CHAR_SYMBOL.contains(tmp[tmp.length - 1])) {
                    words.add(tmp.substring(0, tmp.length - 1))
                    words.add(tmp[tmp.length - 1].toString())
                } else {
                    words.add(tmp)
                }
            }
        }
        return words.toTypedArray()
    }

    companion object {
        var CHAR_SYMBOL = arrayOf('.', '?', '!', '"', ',', '(', ')')
    }
}