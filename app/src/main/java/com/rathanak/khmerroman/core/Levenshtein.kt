package com.rathanak.khmerroman.core
import java.lang.Math.max

class Levenshtein (private val deleteCost: Int,
                   private val insertCost: Int,
                   private val replaceCost: Int,
                   private val swapCost: Int) {
    init {
        check(deleteCost >= 0)
        check(insertCost >= 0)
        check(replaceCost >= 0)
        check(swapCost >= 0)
        check(2 * swapCost >= insertCost + deleteCost)
    }

    fun Distance(source: String, target: String): Int {
        if (source.isEmpty()) {
            return target.length * insertCost
        }
        if (target.isEmpty()) {
            return source.length * deleteCost
        }
        val table = Array(source.length) { IntArray(target.length) }
        val sourceIndexByCharacter = HashMap<Char, Int>()
        if (source[0] != target[0]) {
            table[0][0] = Math.min(replaceCost, deleteCost + insertCost)
        }
        sourceIndexByCharacter.put(source[0], 0)
        for (i in 1 until source.length) {
            val deleteDistance = table[i - 1][0] + deleteCost
            val insertDistance = (i + 1) * deleteCost + insertCost
            val matchDistance  = i * deleteCost + if (source[i] == target[0]) 0 else replaceCost
            table[i][0] = intArrayOf(deleteDistance, insertDistance, matchDistance).min()!!
        }
        for (j in 1 until target.length) {
            val deleteDistance = table[0][j - 1] + insertCost
            val insertDistance = (j + 1) * insertCost + deleteCost
            val matchDistance = j * insertCost + if (source[0] == target[j]) 0 else replaceCost
            table[0][j] = intArrayOf(deleteDistance, insertDistance, matchDistance).min()!!
        }
        for (i in 1 until source.length) {
            var maxSourceLetterMatchIndex = if (source[i] == target[0]) 0 else -1
            for (j in 1 until target.length) {
                val candidateSwapIndex:Int? = sourceIndexByCharacter[target[j]]
                val jSwap = maxSourceLetterMatchIndex
                val deleteDistance = table[i - 1][j] + deleteCost
                val insertDistance = table[i][j - 1] + insertCost
                var matchDistance = table[i - 1][j - 1]
                if (source[i] != target[j]) {
                    matchDistance += replaceCost
                } else {
                    maxSourceLetterMatchIndex = j
                }
                var swapDistance =  Integer.MAX_VALUE;
                if (candidateSwapIndex != null && jSwap != -1) {
                    swapDistance = 0
                    if (candidateSwapIndex > 0 || jSwap > 0) {
                        swapDistance = table[max(0, candidateSwapIndex - 1)][max(0, jSwap - 1)]
                    }
                    swapDistance += (i - candidateSwapIndex - 1) * deleteCost
                    swapDistance += (j - jSwap - 1) * insertCost + swapCost
                }
                table[i][j] = intArrayOf(deleteDistance, insertDistance, matchDistance, swapDistance).min()!!
            }
            sourceIndexByCharacter.put(source[i], i)
        }
        return table[source.length - 1][target.length - 1]
    }
}