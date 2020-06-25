package com.rathanak.khmerroman.spelling_corrector.bktree

import android.util.Log
import com.rathanak.khmerroman.spelling_corrector.PQElement
import com.rathanak.khmerroman.spelling_corrector.edit_distance.LevenshteinDistance
import java.util.*

class Bktree {
    var root : Node? = null
    private var SUGGESTED_WORD_LIST_LIMIT: Int = 5

    fun add(word : String, range: Int, other : String) {
        if( root == null ){
            root = Node(word, range, other)
        }
        else {
            add(root, word, range, other)
        }
    }

    private fun add( node : Node? , word : String, range: Int, other : String ) {
        if( node == null )
            return

        val distance =
            LevenshteinDistance(
                node?.word,
                word
            )
        //it means that two string is the same
        if( distance == 0 )
            return

        if( node.children[distance] == null ){
            node.children.put(distance , Node(word, range, other) )
        }
        else{
            add( node.children[distance] , word, range, other )
        }
    }

    fun getSpellSuggestion( word: String , tolerance : Int = 1, limit: Int = 10 ) : List<String> {
        if( root != null ) {
            val suggestionStr: MutableList<String> = mutableListOf()
            var suggestion = getSpellSuggestion(root!!, word.decapitalize(), tolerance)
//            suggestion = suggestion.sortedWith(compareBy<Result>{ it.distance }.thenBy { it.freq })
//                .asReversed()

            var result: MutableMap<Int, PriorityQueue<PQElement>> = mutableMapOf()
            suggestion.forEach {
                if (result[it.distance].isNullOrEmpty()) {
                    result[it.distance] = PriorityQueue<PQElement>(5)
                }
                result[it.distance]?.add(PQElement(it.word, it.distance, it.freq.toString(), it.other))
            }

            val totalKeys = result.keys.size
            if (totalKeys > 0) {
                val takeEach = limit / totalKeys
                for(key in result.keys.sorted()) {
                    var i = 0
                    var suggestedWords = result[key]
                    if (suggestedWords != null) {
                        while (!suggestedWords.isEmpty() && i < takeEach) {

                            var element = suggestedWords.poll()
                            suggestionStr.add(element.word)
                            i++
                        }
                    }
                }
            }

            return suggestionStr//.take(limit)
        }
        return listOf()
    }

    fun getSpellSuggestion( node : Node , word: String , tolerance : Int = 1 ) : List<Result> {
        val result: MutableList<Result> = mutableListOf()
        val distance =
            LevenshteinDistance(
                word,
                node.word
            )
        if (distance <= tolerance) {
            result.add(Result(node.word, distance, node.range, node.other))
        }


        // iterate over the children having tolerance in range (distance-tolerance , distance+tolerance)
        val start = if ((distance - tolerance) < 0) 1 else distance - tolerance
        val end = distance + tolerance
        for (dist in start..end) {
            node.children[dist]?.let {
                val similarWordResult = getSpellSuggestion(it, word, tolerance)
                for (similarWord in similarWordResult) {
                    result.add(similarWord)
                }
            }
        }

        return result
    }
}