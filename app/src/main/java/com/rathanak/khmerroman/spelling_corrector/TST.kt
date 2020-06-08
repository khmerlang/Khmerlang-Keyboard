package com.rathanak.khmerroman.spelling_corrector

class TST {
    var root: Node? = null

    fun insert(key: String, freq: String) {
        root = addNode(root, key, freq, 0)
    }

    fun isWord(key: String): Boolean {
        return search(root, key, 0)
    }


    fun search(node: Node?, word: String, pos: Int): Boolean {
        var tmpNode = node
        var tmpPos = pos
        while (tmpNode != null) {
            if (word[tmpPos] < tmpNode.ch) {
                tmpNode = tmpNode.left
            } else if (word[tmpPos] > tmpNode.ch) {
                tmpNode = tmpNode.right
            } else {
                if (tmpNode.isEnd && tmpPos == word.length - 1) {
                    return true
                }

                tmpPos++
                tmpNode = tmpNode.mid
            }
        }

        return false
    }

    private fun addNode(node: Node?, word: String, freq: String, pos: Int ): Node? {
        var nodeRoot = node
        val ch = word[pos]
        if(nodeRoot == null) {
            if (word.length <= pos) {
                return node
            }
            nodeRoot = Node()
            nodeRoot.ch = ch
            if(pos + 1 == word.length) {
                nodeRoot.isEnd = true
                nodeRoot.frequency = freq
                return nodeRoot
            }
        }

        if(ch < nodeRoot.ch) {
            nodeRoot.left = addNode(nodeRoot.left, word, freq, pos)
        } else if(ch > nodeRoot.ch) {
            nodeRoot.right = addNode(nodeRoot.right, word, freq, pos)
        } else if(pos < word.length -1) {
            nodeRoot.mid = addNode(nodeRoot.mid, word, freq, pos+1)
        } else {
            nodeRoot.isEnd = true
            nodeRoot.frequency = freq
        }

        return nodeRoot
    }
}