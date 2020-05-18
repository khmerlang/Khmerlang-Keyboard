package com.rathanak.khmerroman.keyboard

import android.content.res.TypedArray
import android.inputmethodservice.InputMethodService
import android.inputmethodservice.Keyboard
import android.os.Build
import android.util.SparseArray
import android.view.KeyEvent
import android.view.View
import android.view.inputmethod.CompletionInfo
import android.view.inputmethod.EditorInfo
import androidx.annotation.ColorInt
import androidx.annotation.ColorRes
import androidx.annotation.RequiresApi
import com.rathanak.khmerroman.R
import com.rathanak.khmerroman.data.KeyboardPreferences
import com.rathanak.khmerroman.data.KeyboardPreferences.Companion.KEY_NEEDS_RELOAD
import com.rathanak.khmerroman.keyboard.common.KeyStyle
import com.rathanak.khmerroman.keyboard.common.KeyboardStyle
import com.rathanak.khmerroman.keyboard.common.PageType.Companion.NORMAL
import com.rathanak.khmerroman.keyboard.common.PageType.Companion.SHIFT
import com.rathanak.khmerroman.keyboard.common.PageType.Companion.SYMBOL
import com.rathanak.khmerroman.keyboard.common.Styles
import com.rathanak.khmerroman.keyboard.keyboardinflater.CustomKeyboard
import com.rathanak.khmerroman.view.inputmethodview.CustomInputMethodView
import com.rathanak.khmerroman.view.inputmethodview.KeyboardActionListener

class R2KhmerService : InputMethodService(), KeyboardActionListener {
    // todo check here
    private var mCandidateView: CandidateView? = null
    // end here

    private lateinit var customInputMethodView: CustomInputMethodView
    private var currentKeyboard: CustomKeyboard? = null
    private lateinit var keyboardNormal: CustomKeyboard
    private lateinit var keyboardShift: CustomKeyboard
    private lateinit var keyboardSymbol: CustomKeyboard

    private lateinit var preferences: KeyboardPreferences
    private var enableVibration = true
    private var enableSound = true

    override fun onCreate() {
        super.onCreate()
        initSharedPreference()
//        loadKeyCodes()
        initKeyboards()
    }

    private fun initSharedPreference() {
        preferences = KeyboardPreferences(applicationContext)
    }

    override fun onWindowShown() {
        super.onWindowShown()
        if (preferences.getBoolean(KEY_NEEDS_RELOAD)) {
            loadSharedPreferences()
        }
    }

    override fun onInitializeInterface() {
        initKeyboards()
        super.onInitializeInterface()
    }


    private fun initKeyboards() {
        resetLoadedData()
        loadLanguages()
        loadStyles()
        loadSharedPreferences()
    }

    private fun resetLoadedData() {
        // laod data here
    }

    private fun loadLanguages() {
        keyboardNormal = CustomKeyboard(this, R.xml.qwerty, NORMAL, "English")
        keyboardShift = CustomKeyboard(this, R.xml.qwerty_shift, SHIFT, "English")
        keyboardSymbol = CustomKeyboard(this, R.xml.qwerty_symbol, SYMBOL, "English")
        currentKeyboard = keyboardNormal
    }

    private fun loadSharedPreferences() {
        enableVibration = preferences.getBoolean(KeyboardPreferences.KEY_ENABLE_VIBRATION)
        enableSound = preferences.getBoolean(KeyboardPreferences.KEY_ENABLE_SOUND)
    }

    private fun loadStyles() {
        // Load the styles and store them as Singleton values
        Styles.keyboardStyle = KeyboardStyle(getColorInt(R.color.default_keyboard_background_color))

        Styles.keyStyle = KeyStyle(
            getColorInt(R.color.default_key_normal_background_color),
            getColorInt(R.color.default_key_pressed_background_color),
            getColorInt(R.color.default_key_shadow_color),
            getColorInt(R.color.default_key_label_color),
            getColorInt(R.color.default_key_sub_label_color)
        )
    }

    @ColorInt
    private fun getColorInt(@ColorRes res: Int): Int {
        return resources.getColor(res, null)
    }

    override fun onCreateInputView(): View? {
        customInputMethodView = layoutInflater.inflate(
            R.layout.input, null) as CustomInputMethodView
        currentKeyboard?.let {
//            customInputMethodView.prepareAllKeyboardsForRendering(keyboardsOfLanguages, currentSelectedLanguageIdx)
//            customInputMethodView.keyboardViewListener = this
//            customInputMethodView.updateKeyboardLanguage(currentSelectedLanguageIdx)

//            customInputMethodView.keyboardViewListener = this
            customInputMethodView.updateKeyboardLanguage(it)
            //customInputMethodView.setOnKeyboardActionListener(this)
            //customInputMethodView.keyboard = it
        }

        return customInputMethodView
    }



    override fun onCreateCandidatesView(): View {
        mCandidateView = CandidateView(this)
        mCandidateView!!.setService(this)
        return mCandidateView as CandidateView
    }

    override fun onStartInput(attribute: EditorInfo, restarting: Boolean) {
        super.onStartInput(attribute, restarting)
    }

    override fun onFinishInput() {
        super.onFinishInput()

        setCandidatesViewShown(false)

//        if (mInputView != null) {
//            mInputView!!.closing()
//        }
    }

    override fun onStartInputView(attribute: EditorInfo, restarting: Boolean) {
        super.onStartInputView(attribute, restarting)
        // Apply the selected keyboard to the input view.
//        mInputView!!.closing()
    }

    /**
     * Deal with the editor reporting movement of its cursor.
     */
    override fun onUpdateSelection(oldSelStart: Int, oldSelEnd: Int,
                                   newSelStart: Int, newSelEnd: Int,
                                   candidatesStart: Int, candidatesEnd: Int) {
        super.onUpdateSelection(oldSelStart, oldSelEnd, newSelStart, newSelEnd,
            candidatesStart, candidatesEnd)
    }

    /**
     * This tells us about completions that the editor has determined based
     * on the current text in it.  We want to use this in fullscreen mode
     * to show the completions ourself, since the editor can not be seen
     * in that situation.
     */
    override fun onDisplayCompletions(completions: Array<CompletionInfo>?) {
        super.onDisplayCompletions(completions)
    }

    /**
     * Use this to monitor key events being delivered to the application.
     * We get first crack at them, and can either resume them or let them
     * continue to the app.
     */
    override fun onKeyDown(keyCode: Int, event: KeyEvent): Boolean {
        return super.onKeyDown(keyCode, event)
    }

    /**
     * Use this to monitor key events being delivered to the application.
     * We get first crack at them, and can either resume them or let them
     * continue to the app.
     */
    override fun onKeyUp(keyCode: Int, event: KeyEvent): Boolean {
        return super.onKeyUp(keyCode, event)
    }

    /**
     * Helper to send a key down / key up pair to the current editor.
     */
    private fun keyDownUp(keyEventCode: Int) {
        currentInputConnection.sendKeyEvent(
            KeyEvent(KeyEvent.ACTION_DOWN, keyEventCode))
        currentInputConnection.sendKeyEvent(
            KeyEvent(KeyEvent.ACTION_UP, keyEventCode))
    }

    override fun onKey(primaryCode: Int, keyCodes: IntArray?) {
        if (primaryCode == Keyboard.KEYCODE_CANCEL) {
            handleClose()
            return
        } else {
            handleCharacter(primaryCode, keyCodes)
        }
    }

    override fun onSwipeLeft() {
//        TODO("Not yet implemented")
    }

    override fun onSwipeRight() {
//        TODO("Not yet implemented")
    }

    override fun onSwipeUp() {
//        TODO("Not yet implemented")
    }

    override fun onSwipeDown() {
//        TODO("Not yet implemented")
    }

    override fun onChangeKeyboardSwipe(direction: Int) {
//        TODO("Not yet implemented")
    }


    private fun handleCharacter(primaryCode: Int, keyCodes: IntArray?) {
        var primaryCode = primaryCode
        if (isInputViewShown) {
//            if (mInputView!!.isShifted) {
//                primaryCode = Character.toUpperCase(primaryCode)
//            }
        }

        currentInputConnection.commitText(
            primaryCode.toChar().toString(), 1)
    }

    private fun handleClose() {
        requestHideSelf(0)
//        mInputView!!.closing()
    }

}