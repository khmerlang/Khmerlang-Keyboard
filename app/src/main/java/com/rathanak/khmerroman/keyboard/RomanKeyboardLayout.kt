package com.rathanak.khmerroman.keyboard

import android.inputmethodservice.InputMethodService
import android.inputmethodservice.Keyboard
import android.inputmethodservice.KeyboardView
import android.view.KeyEvent
import android.view.View
import android.view.inputmethod.CompletionInfo
import android.view.inputmethod.EditorInfo
import com.rathanak.khmerroman.R

class RomanKeyboardLayout : InputMethodService(), KeyboardView.OnKeyboardActionListener {
    private var mInputView: KeyboardView? = null
    private var mCandidateView: CandidateView? = null
    private var mCurKeyboard: Keyboard? = null

    override fun onCreate() {
        super.onCreate()
    }

    override fun onInitializeInterface() {
        mCurKeyboard = Keyboard(this, R.xml.qwerty)
    }

    override fun onCreateInputView(): View {
        mInputView = layoutInflater.inflate(
            R.layout.input, null) as KeyboardView
        mInputView!!.setOnKeyboardActionListener(this)
        mInputView!!.keyboard = mCurKeyboard
        return mInputView as KeyboardView
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

        if (mInputView != null) {
            mInputView!!.closing()
        }
    }

    override fun onStartInputView(attribute: EditorInfo, restarting: Boolean) {
        super.onStartInputView(attribute, restarting)
        // Apply the selected keyboard to the input view.
        mInputView!!.closing()
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

    override fun onText(text: CharSequence) {
        val ic = currentInputConnection ?: return
        ic.beginBatchEdit()
        ic.commitText(text, 0)
        ic.endBatchEdit()
    }

    private fun handleCharacter(primaryCode: Int, keyCodes: IntArray?) {
        var primaryCode = primaryCode
        if (isInputViewShown) {
            if (mInputView!!.isShifted) {
                primaryCode = Character.toUpperCase(primaryCode)
            }
        }

        currentInputConnection.commitText(
            primaryCode.toChar().toString(), 1)
    }

    private fun handleClose() {
        requestHideSelf(0)
        mInputView!!.closing()
    }

    override fun swipeRight() {
    }

    override fun onPress(p0: Int) {
    }

    override fun onRelease(p0: Int) {
    }

    override fun swipeLeft() {
    }

    override fun swipeUp() {
    }

    override fun swipeDown() {
        handleClose()
    }
}