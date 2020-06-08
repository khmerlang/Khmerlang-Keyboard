package com.rathanak.khmerroman.spelling_corrector

class Node {
    var ch: Char = '\u0000'
    var isEnd: Boolean = false
    var frequency: String? = null
    var left: Node? = null
    var mid: Node?= null
    var right: Node?= null
}