package com.rathanak.khmerroman.core

import java.util.HashMap

class Node(var word : String) {
    var children: HashMap<Int,Node> = hashMapOf()
}