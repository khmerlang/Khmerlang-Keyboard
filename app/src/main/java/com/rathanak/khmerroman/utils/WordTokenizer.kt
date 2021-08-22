package com.rathanak.khmerroman.utils

import android.content.Context
import android.util.Log
import com.rathanak.khmerroman.ml.WordSegModel
import org.tensorflow.lite.DataType
import org.tensorflow.lite.support.tensorbuffer.TensorBuffer
import kotlin.math.exp

class WordTokenizer(private val context: Context) {
    private val KHCONST = "កខគឃងចឆជឈញដឋឌឍណតថទធនបផពភមយរលវឝឞសហឡអឣឤឥឦឧឨឩឪឫឬឭឮឯឰឱឲឳ"
    private val KHVOWEL = "឴឵ាិីឹឺុូួើឿៀេែៃោៅំះៈ"
    private val KHSUB = "្"
    private val KHDIAC = "៉៊់៌៍៎៏័"
    private val KHSYM = "៕។៛ៗ៚៙៘,.? "
    private val KHNUMBER = "០១២៣៤៥៦៧៨៩0123456789"
    private val KHLUNAR = "᧠᧡᧢᧣᧤᧥᧦᧧᧨᧩᧪᧫᧬᧭᧮᧯᧰᧱᧲᧳᧴᧵᧶᧷᧸᧹᧺᧻᧼᧽᧾᧿"
    private val CHARS = "P" + "U" + KHCONST + KHVOWEL + KHSUB + KHDIAC + KHSYM + KHNUMBER + KHLUNAR
    private val MAX_WORD = 328
    private var model: WordSegModel = WordSegModel.newInstance(context)

    fun destroy() {
        // Releases model resources if no longer used.
        model.close()
    }

    private fun char2idx(str: String): IntArray {
        var arrIdx = IntArray(str.length)
        str.forEachIndexed { index, ch ->
            val num = CHARS.indexOf(ch)

            arrIdx[index] = if (num >= 0) num else 1
        }

        return arrIdx
    }

    private fun idx2char(arrIdx: IntArray): String {
        var str = ""
        arrIdx.forEach {
            str += CHARS.get(it)
        }
        return str
    }


    private fun sigmoid(x: Float): Double {
        return 1.0f / (1.0f + exp((-x).toDouble()))
    }

    private fun segmentKhmerWord(inputText: String) :MutableList<String> {
        val words: MutableList<String> = arrayListOf()
        var khmerWords = inputText
        khmerWords = khmerWords.replace(" ", "")
        khmerWords = khmerWords.replace("​", "")
        if (khmerWords.length > MAX_WORD) {
            words.add(khmerWords)
            return words
        }

        val inputIdx = char2idx(khmerWords)
        val inputFeature = TensorBuffer.createFixedSize(intArrayOf(1, 328), DataType.FLOAT32)
        var wordArr = IntArray(328) {0}
        for (i in inputIdx.indices) {
            wordArr[i] = inputIdx[i]
        }

        inputFeature.loadArray(wordArr)
        // Runs model inference and gets result.
        val outputs = model.process(inputFeature)
        val outputFeature = outputs.outputFeature0AsTensorBuffer

        var segWord = ""
        for (i in inputIdx.indices) {
            if (sigmoid(outputFeature.floatArray[i]) > 0.6) {
                if (segWord != "") {
                    words.add(segWord)
                }
                segWord = khmerWords[i].toString()
            } else {
                segWord += khmerWords[i].toString()
            }
        }

        if (segWord != "") {
            words.add(segWord)
        }
        return words
    }

    private fun baseSegment(inputText: String): MutableList<String> {
        val words: MutableList<String> = arrayListOf()
        inputText.split("[\n\t\b​ ]".toRegex()).forEach {
            var word = it
            if (word.length <= 1) {
                words.add(word)
                return@forEach
            }

            // if Khmer word
            if (word[0] in 'ក'..'ឳ') {
                words += segmentKhmerWord(word)
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