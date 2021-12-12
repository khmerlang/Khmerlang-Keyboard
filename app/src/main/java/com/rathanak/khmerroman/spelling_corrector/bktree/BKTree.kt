package com.rathanak.khmerroman.spelling_corrector.bktree

import com.rathanak.khmerroman.spelling_corrector.edit_distance.Levenshtein

class Bktree {
    var root : Node? = null
    private var SUGGESTED_WORD_LIST_LIMIT: Int = 5

    fun add(word : String, other : String) {
        if( root == null ){
            root = Node(word, other)
        }
        else {
            add(root, word, other)
        }
    }

    private fun add( node : Node? , word : String, other : String ) {
        if( node == null )
            return

        val distance =
            Levenshtein.distance(
                node.word,
                word
            )
        //it means that two string is the same
        if( distance == 0 ) {
            node.other = node.other + "_" + other
            return
        }

        if( node.children[distance] == null ){
            node.children.put(distance , Node(word, other) )
        }
        else{
            add(node.children[distance] , word, other )
        }
    }

    fun getSpellSuggestion( node : Node , word: String , tolerance : Int = 1 ) : ArrayList<Result> {
        val results = arrayListOf<Result>()
        val distance =
            Levenshtein.distance(
                word,
                node.word
            )
        if (distance <= tolerance) {
            results.add(Result(node.word, distance, node.other))
        }

        // iterate over the children having tolerance in range (distance-tolerance , distance+tolerance)
        val start = if ((distance - tolerance) < 0) 1 else distance - tolerance
        val end = distance + tolerance
        for (dist in start..end) {
            node.children[dist]?.let {
                val similarWordResult = getSpellSuggestion(it, word, tolerance)
                for (similarWord in similarWordResult) {
                    results.add(similarWord)
                }
            }
        }

        return results
    }
}