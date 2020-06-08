package com.rathanak.khmerroman.spelling_corrector

import android.content.Context
import android.util.Log
import java.util.*
import kotlin.collections.LinkedHashMap

class SpellCorrector() {
    var tst: TST = TST()
    private var EDIT_LIMIT: Int = 3
    private var SUGGESTED_WORD_LIST_LIMIT: Int = 5
    var inputString: String?= null
    var suggestedWords: PriorityQueue<PQElement> = PriorityQueue<PQElement>(5)

    fun loadData(context: Context, filePart: String) {
        try {
            context.assets.open(filePart).bufferedReader().useLines {
                    lines -> lines.forEach {
                    val token = it.split("\\s".toRegex())
//                    val token = it.split(",")
                    if (token.size == 2) {
                        tst.insert(token[0], token[1])
                    }
                }
            }
        } catch (ex:Exception){
            Log.e("read_file", ex.localizedMessage)
        }

    }

    fun correct(str: String) : LinkedHashMap<String, Int>{
        if (str.isNullOrEmpty()) {
            throw IllegalArgumentException("Input string is blank.")
        }

        if (tst.root == null) {
            throw IllegalArgumentException("Node not found.")
        }

        inputString = str
        tst.root?.let { traverse(it, "") }
        var outputMap: LinkedHashMap<String, Int> = LinkedHashMap<String, Int>()
        var i = 0
        while (!suggestedWords.isEmpty() && i < SUGGESTED_WORD_LIST_LIMIT) {
            val element = suggestedWords.poll()
            outputMap[element.word] = element.editDistance
            i++
        }
        return outputMap
    }

    fun traverse(root: Node, str: String) {
        if (root == null) {
            return
        }

        val distance: Int = getEditDistance(inputString!!, str + root.ch)
        if (str.length < inputString!!.length
            && getEditDistance(str, inputString!!.substring(0, str.length + 1)) > EDIT_LIMIT
        ) {
            return
        } else if (str.length > inputString!!.length + EDIT_LIMIT) {
            return
        } else if (Math.abs(str.length - inputString!!.length) <= EDIT_LIMIT && distance > EDIT_LIMIT) {
            return
        }

        // recursively traverse through the nodes for words
        root.left?.let { traverse(it, str) }
        if (root.isEnd === true && distance <= EDIT_LIMIT) {
            suggestedWords.add(root.frequency?.let { PQElement(str + root.ch, distance, it) })
        }
        root.mid?.let { traverse(it, str + root.ch) }
        root.right?.let { traverse(it, str) }
    }

    private fun  getEditDistance(a: String, b: String): Int {
        val costs = IntArray(b.length + 1)
        for (j in costs.indices) costs[j] = j
        for (i in 1..a.length) {
            costs[0] = i
            var nw = i - 1
            for (j in 1..b.length) {
                val cj = Math.min(
                    1 + Math.min(costs[j], costs[j - 1]),
                    if (a[i - 1] === b[j - 1]) nw else nw + 1
                )
                nw = costs[j]
                costs[j] = cj
            }
        }
        return costs[b.length]
    }
}