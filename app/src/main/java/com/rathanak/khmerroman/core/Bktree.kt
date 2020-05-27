package com.rathanak.khmerroman.core

class Bktree {
    private var root : Node? = null

    fun add(word : String) {
        if( root == null ){
            root = Node(word)
        }
        else {
            add(root, word)
        }
    }

    private fun add( node : Node? , word : String ) {
        if( node == null )
            return

        val distance = LevenshteinDistance(node?.word, word)
        //it means that two string is the same
        if( distance == 0 )
            return

        if( node.children[distance] == null ){
            node.children.put(distance , Node(word) )
        }
        else{
            add( node.children[distance] , word )
        }
    }

    fun getSpellSuggestion( word: String , tolerance : Int = 1 ) : List<String> {
        if( root != null ) {
            val suggestionStr: MutableList<String> = mutableListOf()
            var suggestion = getSpellSuggestion(root!!, word.decapitalize(), tolerance)
            suggestion = suggestion.sortedBy { it.distance }.take(10)
            suggestion.forEach {
                suggestionStr.add(it.word)
            }
            return suggestionStr
        }
        return listOf()
    }

    private fun getSpellSuggestion( node : Node , word: String , tolerance : Int = 1 ) : List<Result> {
        val result: MutableList<Result> = mutableListOf()
        val distance = LevenshteinDistance(word, node.word)
        if (distance <= tolerance)
            result.add(Result(node.word, distance))

        // iterate over the children having tolerance in range (distance-tolerance , distance+tolerance)
        val start = if ((distance - tolerance) < 0) 1 else distance - tolerance
        val end = distance + tolerance
        for (dist in start..end) {
            node.children[dist]?.let {
                val similarWordResult = getSpellSuggestion(it, word, tolerance)
                for (similarWord in similarWordResult)
                    result.add(similarWord)
            }
        }

        return result
    }
}