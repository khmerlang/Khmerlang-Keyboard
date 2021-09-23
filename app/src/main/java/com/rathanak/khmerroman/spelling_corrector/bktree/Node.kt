package com.rathanak.khmerroman.spelling_corrector.bktree

import java.util.HashMap

class Node(var word : String, var other: String) {
    var children: HashMap<Int,Node> = hashMapOf()
}