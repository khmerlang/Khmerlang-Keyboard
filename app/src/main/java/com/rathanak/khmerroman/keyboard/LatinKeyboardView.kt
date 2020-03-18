package com.rathanak.khmerroman.keyboard

import android.content.Context
import android.inputmethodservice.Keyboard
import android.inputmethodservice.Keyboard.Key
import android.inputmethodservice.KeyboardView
import android.util.AttributeSet
import android.view.inputmethod.InputMethodSubtype

class LatinKeyboardView : KeyboardView {

    constructor(context: Context, attrs: AttributeSet) : super(context, attrs) {}

    constructor(context: Context, attrs: AttributeSet, defStyle: Int) : super(context, attrs, defStyle) {}

    override fun onLongPress(key: Key): Boolean {
        if (key.codes[0] == Keyboard.KEYCODE_CANCEL) {
            onKeyboardActionListener.onKey(KEYCODE_OPTIONS, null)
            return true
        } else {
            return super.onLongPress(key)
        }
    }

    companion object {

        internal val KEYCODE_OPTIONS = -100
        // TODO: Move this into android.inputmethodservice.Keyboard
        internal val KEYCODE_LANGUAGE_SWITCH = -101
    }
}