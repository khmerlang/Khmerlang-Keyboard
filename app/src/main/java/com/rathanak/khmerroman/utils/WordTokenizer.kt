package com.rathanak.khmerroman.utils

import com.rathanak.khmerroman.segmentation.KhmerSegment

class WordTokenizer {
    fun baseSegment(inputText: String): MutableList<String> {
        val words: MutableList<String> = arrayListOf()
        val ks: KhmerSegment = KhmerSegment()
        inputText.split("[\n\t\b​ ]".toRegex()).forEach {
            var word = it
            if (word.length <= 1) {
                words.add(word)
                return@forEach
            }

            // if Khmer word
            //    if Khmer word and correct
            //       No segment
            //    else
            //       if long then max length
            //          break line and seg
            //       else
            //          break whole text
            if (word[0] in 'ក'..'ឳ') {
                words += ks.segmentWord(word)
            } else {
                words.add(word)
            }
        }
        return words
    }

    fun tokenize(inputText: String): Array<String> {
        val words: MutableList<String> = arrayListOf()
        baseSegment(inputText).forEach {
            var word = it
            if (word.length <= 1) {
                words.add(word)
                return@forEach
            }

            if (CHAR_SYMBOL.contains(word[0])) {
                words.add(word[0].toString())
                word = word.substring(1, word.length)
            }
            if (CHAR_SYMBOL.contains(word[word.length - 1])) {
                words.add(word.substring(0, word.length - 1))
                words.add(word[word.length - 1].toString())
            } else {
                words.add(word)
            }
        }
        return words.toTypedArray()
    }

    companion object {
        var CHAR_SYMBOL = arrayOf('.', '?', '!', '"', ',', '(', ')', '។', '៕', '៖', '៘', '៚')
    }
}