package com.rathanak.khmerroman.keyboard.common

class PageType {
    companion object {
        const val NORMAL = 0
        const val SHIFT = 1
        const val SYMBOL = 2
        const val SYMBOL_SHIFT = 3
        const val NUMBER = 4
        val PAGE_TYPES = listOf(NORMAL, SHIFT, SYMBOL, SYMBOL_SHIFT, NUMBER)
    }
}