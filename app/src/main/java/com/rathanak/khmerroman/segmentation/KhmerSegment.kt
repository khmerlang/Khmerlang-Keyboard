package com.rathanak.khmerroman.segmentation

import com.rathanak.khmerroman.ml.WordSegModel
import org.tensorflow.lite.DataType
import org.tensorflow.lite.support.tensorbuffer.TensorBuffer
import kotlin.math.exp

class KhmerSegment {
    private val KHCONST = "កខគឃងចឆជឈញដឋឌឍណតថទធនបផពភមយរលវឝឞសហឡអឣឤឥឦឧឨឩឪឫឬឭឮឯឰឱឲឳ"
    private val KHVOWEL = "឴឵ាិីឹឺុូួើឿៀេែៃោៅំះៈ"
    private val KHSUB = "្"
    private val KHDIAC = "៉៊់៌៍៎៏័"
    private val KHSYM = "៕។៛ៗ៚៙៘,.? "
    private val KHNUMBER = "០១២៣៤៥៦៧៨៩0123456789"
    private val KHLUNAR = "᧠᧡᧢᧣᧤᧥᧦᧧᧨᧩᧪᧫᧬᧭᧮᧯᧰᧱᧲᧳᧴᧵᧶᧷᧸᧹᧺᧻᧼᧽᧾᧿"
    private val CHARS = "P" + "U" + KHCONST + KHVOWEL + KHSUB + KHDIAC + KHSYM + KHNUMBER + KHLUNAR

    fun char2idx(str: String): IntArray {
        var arrIdx = IntArray(str.length)
        str.forEachIndexed { index, ch ->
            val num = CHARS.indexOf(ch)

            arrIdx[index] = if (num >= 0) num else 1
        }

        return arrIdx
    }

    fun idx2char(arrIdx: IntArray): String {
        var str = ""
        arrIdx.forEach {
            str += CHARS.get(it)
        }
        return str
    }

    fun sigmoid(x: Double): Double {
        return 1.0f / (1.0f + exp((-x).toDouble()))
    }

    fun segmentWord(inputText: String) :MutableList<String> {
//        //     val str = "ការស្រឡាញ់ដែលអាចមានភាពឋិតឋេរចំពោះគ្នា"
//        //     var input = char2idx(str)
//        //     println(input.joinToString(","))
//        //     println(idx2char(input))
//        //     println(sigmoid(0.7))
//        //     println(sigmoid(-0.48))
//        val model = WordSegModel.newInstance(context)
//
//        // Creates inputs for reference.
//        val inputFeature0 = TensorBuffer.createFixedSize(intArrayOf(1, 328), DataType.FLOAT32)
//        inputFeature0.loadBuffer(byteBuffer)
//
//        // Runs model inference and gets result.
//        val outputs = model.process(inputFeature0)
//        val outputFeature0 = outputs.outputFeature0AsTensorBuffer
//
//        // Releases model resources if no longer used.
//        model.close()

//                if (true) {
//
//                } else {
//
//                }
        val words: MutableList<String> = arrayListOf()
        words.add(inputText)
        return words
    }
}