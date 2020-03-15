package com.rathanak.khmerroman.keyboard

import android.content.Context
import android.content.res.Resources
import android.content.res.XmlResourceParser
import android.graphics.drawable.Drawable
import android.inputmethodservice.Keyboard
import android.view.inputmethod.EditorInfo
import android.view.inputmethod.InputMethodManager
import com.rathanak.khmerroman.R

class LatinKeyboard : Keyboard {

    private var mEnterKey: Keyboard.Key? = null
    private var mSpaceKey: Keyboard.Key? = null
    /**
     * Stores the current state of the mode change key. Its width will be dynamically updated to
     * match the region of [.mModeChangeKey] when [.mModeChangeKey] becomes invisible.
     */
    private var mModeChangeKey: Keyboard.Key? = null
    /**
     * Stores the current state of the language switch key (a.k.a. globe key). This should be
     * visible while [InputMethodManager.shouldOfferSwitchingToNextInputMethod]
     * returns true. When this key becomes invisible, its width will be shrunk to zero.
     */
    private var mLanguageSwitchKey: Keyboard.Key? = null
    /**
     * Stores the size and other information of [.mModeChangeKey] when
     * [.mLanguageSwitchKey] is visible. This should be immutable and will be used only as a
     * reference size when the visibility of [.mLanguageSwitchKey] is changed.
     */
    private var mSavedModeChangeKey: Keyboard.Key? = null
    /**
     * Stores the size and other information of [.mLanguageSwitchKey] when it is visible.
     * This should be immutable and will be used only as a reference size when the visibility of
     * [.mLanguageSwitchKey] is changed.
     */
    private var mSavedLanguageSwitchKey: Keyboard.Key? = null

    constructor(context: Context, xmlLayoutResId: Int) : super(context, xmlLayoutResId) {}

    constructor(context: Context, layoutTemplateResId: Int,
                characters: CharSequence, columns: Int, horizontalPadding: Int) : super(context, layoutTemplateResId, characters, columns, horizontalPadding) {
    }

    override fun createKeyFromXml(res: Resources, parent: Keyboard.Row, x: Int, y: Int,
                                  parser: XmlResourceParser): Keyboard.Key {
        val key = LatinKey(res, parent, x, y, parser)
        if (key.codes[0] == 10) {
            mEnterKey = key
        } else if (key.codes[0] == ' '.toInt()) {
            mSpaceKey = key
        } else if (key.codes[0] == Keyboard.KEYCODE_MODE_CHANGE) {
            mModeChangeKey = key
            mSavedModeChangeKey = LatinKey(res, parent, x, y, parser)
        } else if (key.codes[0] == LatinKeyboardView.KEYCODE_LANGUAGE_SWITCH) {
            mLanguageSwitchKey = key
            mSavedLanguageSwitchKey = LatinKey(res, parent, x, y, parser)
        }
        return key
    }

    /**
     * Dynamically change the visibility of the language switch key (a.k.a. globe key).
     * @param visible True if the language switch key should be visible.
     */
    internal fun setLanguageSwitchKeyVisibility(visible: Boolean) {
        if (visible) {
            // The language switch key should be visible. Restore the size of the mode change key
            // and language switch key using the saved layout.
            mModeChangeKey!!.width = mSavedModeChangeKey!!.width
            mModeChangeKey!!.x = mSavedModeChangeKey!!.x
            mLanguageSwitchKey!!.width = mSavedLanguageSwitchKey!!.width
            mLanguageSwitchKey!!.icon = mSavedLanguageSwitchKey!!.icon
            mLanguageSwitchKey!!.iconPreview = mSavedLanguageSwitchKey!!.iconPreview
        } else {
            // The language switch key should be hidden. Change the width of the mode change key
            // to fill the space of the language key so that the user will not see any strange gap.
            mModeChangeKey!!.width = mSavedModeChangeKey!!.width + mSavedLanguageSwitchKey!!.width
            mLanguageSwitchKey!!.width = 0
            mLanguageSwitchKey!!.icon = null
            mLanguageSwitchKey!!.iconPreview = null
        }
    }

    /**
     * This looks at the ime options given by the current editor, to set the
     * appropriate label on the keyboard's enter key (if it has one).
     */
    internal fun setImeOptions(res: Resources, options: Int) {
        if (mEnterKey == null) {
            return
        }

        when (options and (EditorInfo.IME_MASK_ACTION or EditorInfo.IME_FLAG_NO_ENTER_ACTION)) {
            EditorInfo.IME_ACTION_GO -> {
                mEnterKey!!.iconPreview = null
                mEnterKey!!.icon = null
                mEnterKey!!.label = res.getText(R.string.label_go_key)
            }
            EditorInfo.IME_ACTION_NEXT -> {
                mEnterKey!!.iconPreview = null
                mEnterKey!!.icon = null
                mEnterKey!!.label = res.getText(R.string.label_next_key)
            }
            EditorInfo.IME_ACTION_SEARCH -> {
                mEnterKey!!.icon = res.getDrawable(R.drawable.sym_keyboard_search)
                mEnterKey!!.label = null
            }
            EditorInfo.IME_ACTION_SEND -> {
                mEnterKey!!.iconPreview = null
                mEnterKey!!.icon = null
                mEnterKey!!.label = res.getText(R.string.label_send_key)
            }
            else -> {
                mEnterKey!!.icon = res.getDrawable(R.drawable.sym_keyboard_return)
                mEnterKey!!.label = null
            }
        }
    }

    internal fun setSpaceIcon(icon: Drawable) {
        if (mSpaceKey != null) {
            mSpaceKey!!.icon = icon
        }
    }

    internal class LatinKey(res: Resources, parent: Keyboard.Row, x: Int, y: Int,
                            parser: XmlResourceParser) : Keyboard.Key(res, parent, x, y, parser) {

        /**
         * Overriding this method so that we can reduce the target area for the key that
         * closes the keyboard.
         */
        override fun isInside(x: Int, y: Int): Boolean {
            return super.isInside(x, if (codes[0] == Keyboard.KEYCODE_CANCEL) y - 10 else y)
        }
    }

}