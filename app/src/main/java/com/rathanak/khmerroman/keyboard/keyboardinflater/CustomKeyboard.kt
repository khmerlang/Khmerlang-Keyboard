package com.rathanak.khmerroman.keyboard.keyboardinflater

import android.content.Context
import android.content.res.Resources
import android.content.res.XmlResourceParser
import android.inputmethodservice.Keyboard

class CustomKeyboard(context: Context?, xmlLayoutResId: Int, val type: Int, val language: String) : Keyboard(context, xmlLayoutResId) {
    val formattedKeyList = mutableListOf<List<CustomKey>>()

    init {
        // Generate the keys in row format
        var row = mutableListOf<CustomKey>()
        @Suppress("UNCHECKED_CAST")
        (this.keys as MutableList<CustomKey>).forEach { key ->
            row.add(key)
            if (key.isRightEdge()) {
                formattedKeyList.add(row)
                row = mutableListOf()
            }
        }
    }

    override fun createKeyFromXml(res: Resources?, parent: Row?, x: Int, y: Int, parser: XmlResourceParser?): Key {
        return CustomKey(res, parent, x, y, parser)
    }
}
