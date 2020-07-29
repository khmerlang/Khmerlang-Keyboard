package com.rathanak.khmerroman.keyboard

import android.content.Context
import android.content.res.TypedArray
import android.inputmethodservice.InputMethodService
import android.inputmethodservice.Keyboard
import android.media.AudioManager
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.text.InputType
import android.util.Log
import android.util.SparseArray
import android.view.KeyEvent
import android.view.View
import android.view.inputmethod.*
import android.widget.LinearLayout
import androidx.annotation.ColorInt
import androidx.annotation.ColorRes
import androidx.annotation.RequiresApi
import com.rathanak.khmerroman.BuildConfig
import com.rathanak.khmerroman.R
import com.rathanak.khmerroman.data.KeyboardPreferences
import com.rathanak.khmerroman.data.KeyboardPreferences.Companion.KEY_NEEDS_RELOAD
import com.rathanak.khmerroman.keyboard.common.KeyData
import com.rathanak.khmerroman.keyboard.common.KeyStyle
import com.rathanak.khmerroman.keyboard.common.KeyboardStyle
import com.rathanak.khmerroman.keyboard.common.PageType.Companion.NORMAL
import com.rathanak.khmerroman.keyboard.common.PageType.Companion.NUMBER
import com.rathanak.khmerroman.keyboard.common.PageType.Companion.SHIFT
import com.rathanak.khmerroman.keyboard.common.PageType.Companion.SYMBOL
import com.rathanak.khmerroman.keyboard.common.PageType.Companion.SYMBOL_SHIFT
import com.rathanak.khmerroman.keyboard.common.Styles
import com.rathanak.khmerroman.keyboard.extensions.contains
import com.rathanak.khmerroman.keyboard.keyboardinflater.CustomKeyboard
import com.rathanak.khmerroman.keyboard.smartbar.SmartbarManager
import com.rathanak.khmerroman.segmentation.Segmentation
//import com.rathanak.khmerroman.spelling_corrector.SpellCorrector
import com.rathanak.khmerroman.spelling_corrector.bktree.SpellCorrector
import com.rathanak.khmerroman.view.inputmethodview.CustomInputMethodView
import com.rathanak.khmerroman.view.inputmethodview.KeyboardActionListener
import com.rathanak.nlp.LanguageModel
import com.rathanak.nlp.NGrams
import com.rathanak.nlp.StupidBackoffRanking
import kotlinx.coroutines.*
import kotlin.properties.Delegates

class R2KhmerService : InputMethodService(), KeyboardActionListener {

    //private lateinit var customInputMethodView: CustomInputMethodView
    private var customInputMethodView: CustomInputMethodView? = null

    private lateinit var keyboardNormal: CustomKeyboard
    private lateinit var keyboardShift: CustomKeyboard
    private lateinit var keyboardSymbol: CustomKeyboard
    private lateinit var keyboardSymbolShift: CustomKeyboard
    private lateinit var keyboardNumber: CustomKeyboard

    private var languageNames: MutableList<String> = mutableListOf()
    private var languageXmlRes: MutableList<Int> = mutableListOf()
    private var languageShiftXmlRes: MutableList<Int> = mutableListOf()
    private var languageSymbolXmlRes: MutableList<Int> = mutableListOf()
    private var languageSymbolShiftXmlRes: MutableList<Int> = mutableListOf()
    private var languageNumberXmlRes: MutableList<Int> = mutableListOf()

    private var keyboardsOfLanguages = SparseArray<SparseArray<CustomKeyboard>>()

    private var currentSelectedLanguageIdx = 0
    private var enableVibration = true
    private var enableSound = true
    private var candidateChoosed = false
    private var preCandidateKhmer = false
    private var composingText: String? = null
    private var composingTextStart: Int? = null
    private var isComposingEnabled: Boolean = false
    private var previousWords: MutableList<String> = mutableListOf()

//    val ngrams: NGrams by inject()
//    val languageModel: LanguageModel by inject()
//    val ngrams: NGrams = NGrams(StupidBackoffRanking())
//    var languageModel: LanguageModel = LanguageModel()
    var spellingCorrector: SpellCorrector = SpellCorrector()
    var segmentation: Segmentation = Segmentation()

    private val smartbarManager: SmartbarManager = SmartbarManager(this)
    var rootView: LinearLayout? = null
    val context: Context
        get() = rootView?.context ?: this

    var currentKeyboardPage by Delegates.observable<Int?>(null) { _, _, newPage ->
        newPage?.let {
            customInputMethodView?.updateKeyboardPage(newPage)
        }
    }
    var currentInputPassword: Boolean = false

    private lateinit var preferences: KeyboardPreferences

    override fun onCreate() {
        super.onCreate()
        initSharedPreference()
        loadKeyCodes()
        initKeyboards()

        loadSpellingData()
    }

    private fun loadSpellingData() {
        spellingCorrector.reset()
        segmentation.reset()
        val job= GlobalScope.launch(Dispatchers.Main) {
            loadSpelling(currentSelectedLanguageIdx == 1)
            loadSegmentation(currentSelectedLanguageIdx == 1)
        }

    }

    private suspend fun loadSegmentation(isKhmer: Boolean) {
        coroutineScope {
            async(Dispatchers.IO) {
                if (isKhmer) {
//                    segmentation.loadData(context)
                }
            }
        }
    }
    private suspend fun loadSpelling(isKhmer: Boolean) {
        coroutineScope {


            async(Dispatchers.IO) {
                if (isKhmer) {
                    spellingCorrector.loadData(context, false)
                } else {
                    spellingCorrector.loadData(context, true)
                }
            }
        }
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
        languageNames.clear()
        languageXmlRes.clear()
        languageShiftXmlRes.clear()
        languageSymbolXmlRes.clear()
        languageSymbolShiftXmlRes.clear()
        languageNumberXmlRes.clear()
        keyboardsOfLanguages.clear()
        currentKeyboardPage = null
    }

    private fun renderCurrentLanguage() {
        if (keyboardsOfLanguages.contains(currentSelectedLanguageIdx)) {
            customInputMethodView?.updateKeyboardLanguage(currentSelectedLanguageIdx)
        }
    }

    fun reRenderKeylayout() {
        if (keyboardsOfLanguages.contains(currentSelectedLanguageIdx)) {
            customInputMethodView?.updateKeyboardLanguage(currentSelectedLanguageIdx)
        }
    }

    private fun loadSharedPreferences() {
        currentSelectedLanguageIdx = preferences.getInt(KeyboardPreferences.KEY_CURRENT_LANGUAGE_IDX, 0)
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
        rootView = layoutInflater.inflate(R.layout.roman_2_khmer, null) as LinearLayout
        rootView?.addView(smartbarManager.createSmartbarView(), 0)
        customInputMethodView = layoutInflater.inflate(R.layout.keybaord, null) as CustomInputMethodView
        val keyboard = keyboardsOfLanguages[currentSelectedLanguageIdx]
        keyboard?.let {
            customInputMethodView?.prepareAllKeyboardsForRendering(keyboardsOfLanguages, currentSelectedLanguageIdx)
            customInputMethodView?.keyboardViewListener = this
            customInputMethodView?.updateKeyboardLanguage(currentSelectedLanguageIdx)
        }
        rootView!!.addView(customInputMethodView)
        return rootView
    }

    private fun loadLanguages() {
        val languagesArray = resources.obtainTypedArray(R.array.languages)
        val keyboards: SparseArray<CustomKeyboard> = SparseArray()
        var eachLanguageTypedArray: TypedArray? = null
        for (i in 0 until languagesArray.length()) {
            val id = languagesArray.getResourceId(i, -1)
            if (id == -1) {
                throw IllegalStateException("Invalid language array resource")
            }
            eachLanguageTypedArray = resources.obtainTypedArray(id)
            eachLanguageTypedArray?.let {
                val nameIdx = 0

                val languageName = it.getString(nameIdx)
                val xmlRes = it.getResourceId(RES_IDX, -1)
                val shiftXmlRes = it.getResourceId(SHIFT_IDX, -1)
                val symbolXmlRes = it.getResourceId(SYM_IDX, -1)
                val symbolShiftXmlRes = it.getResourceId(SYM_SHIFT_IDX, -1)
                val numberXmlRes = it.getResourceId(NUMBER_IDX, -1)

                if (languageName == null || xmlRes == -1 || shiftXmlRes == -1 || symbolXmlRes == -1 || symbolShiftXmlRes == -1 || numberXmlRes == -1) {
                    throw IllegalStateException("Make sure the arrays resources contain name, xml, and shift xml")
                }

                languageNames.add(languageName)
                languageXmlRes.add(xmlRes)
                languageShiftXmlRes.add(shiftXmlRes)
                languageSymbolXmlRes.add(symbolXmlRes)
                languageSymbolShiftXmlRes.add(symbolShiftXmlRes)
                languageNumberXmlRes.add(numberXmlRes)
            }

            keyboardNormal = CustomKeyboard(this, languageXmlRes.last(), NORMAL, languageNames.last())
            keyboardShift = CustomKeyboard(this, languageShiftXmlRes.last(), SHIFT, languageNames.last())
            keyboardSymbol = CustomKeyboard(this, languageSymbolXmlRes.last(), SYMBOL, languageNames.last())
            keyboardSymbolShift = CustomKeyboard(this, languageSymbolShiftXmlRes.last(), SYMBOL_SHIFT, languageNames.last())
            keyboardNumber = CustomKeyboard(this, languageNumberXmlRes.last(), NUMBER, languageNames.last())

            keyboards.clear()
            keyboards.append(NORMAL, keyboardNormal)
            keyboards.append(SHIFT, keyboardShift)
            keyboards.append(SYMBOL, keyboardSymbol)
            keyboards.append(SYMBOL_SHIFT, keyboardSymbolShift)
            keyboards.append(NUMBER, keyboardNumber)
            keyboardsOfLanguages.put(i, keyboards.clone())
        }

        eachLanguageTypedArray?.recycle()
        languagesArray.recycle()
    }

    override fun onSwipeRight() {
        if (BuildConfig.DEBUG) {
            Log.d("///AMOS", "SWIPE RIGHT")
        }
    }

    override fun onSwipeLeft() {
        if (BuildConfig.DEBUG) {
            Log.d("///AMOS", "SWIPE LEFT")
        }
    }

    override fun onSwipeUp() {
        if (BuildConfig.DEBUG) {
            Log.d("///AMOS", "SWIPE UP")
        }
    }

    override fun onSwipeDown() {
        if (BuildConfig.DEBUG) {
            Log.d("///AMOS", "SWIPE DOWN")
        }
    }

    override fun onChangeKeyboardSwipe(direction: Int) {
        val mgr = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager?
        mgr?.showInputMethodPicker()
//        changeLanguage(direction)
    }

    private fun saveCurrentState() {
        preferences.putInt(KeyboardPreferences.KEY_CURRENT_LANGUAGE_IDX, currentSelectedLanguageIdx)
    }

    private fun changeLanguage(direction: Int) {
        currentSelectedLanguageIdx = ((currentSelectedLanguageIdx + direction) + languageNames.size) % languageNames.size
        if (BuildConfig.DEBUG) {
            Log.d("///AMOS", "CHANGE DIRECTION $currentSelectedLanguageIdx")
        }
        saveCurrentState()
        renderCurrentLanguage()
        loadSpellingData()
    }

    @RequiresApi(Build.VERSION_CODES.LOLLIPOP)
    override fun onStartInput(attribute: EditorInfo?, restarting: Boolean) {
        currentInputConnection.requestCursorUpdates(InputConnection.CURSOR_UPDATE_MONITOR)
        super.onStartInput(attribute, restarting)
        currentInputPassword = false
        isComposingEnabled = false
        when ((attribute?.inputType)?.and(InputType.TYPE_MASK_CLASS)) {
            InputType.TYPE_CLASS_DATETIME ->
                currentKeyboardPage = SYMBOL
            InputType.TYPE_CLASS_PHONE, InputType.TYPE_CLASS_NUMBER ->
                currentKeyboardPage = NUMBER
            InputType.TYPE_CLASS_TEXT -> {
                currentKeyboardPage = NORMAL
                isComposingEnabled = true
                when (attribute.inputType and InputType.TYPE_MASK_VARIATION) {
                    InputType.TYPE_TEXT_VARIATION_EMAIL_ADDRESS,
                    InputType.TYPE_TEXT_VARIATION_WEB_EMAIL_ADDRESS -> {
                        isComposingEnabled = false
                    }
                    InputType.TYPE_TEXT_VARIATION_URI -> {
                        isComposingEnabled = false
                    }
                    InputType.TYPE_TEXT_VARIATION_PASSWORD,
                    InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD,
                    InputType.TYPE_TEXT_VARIATION_WEB_PASSWORD -> {
                        currentInputPassword = true
                        isComposingEnabled = false
                    }
                }
            }
            else -> {
                currentKeyboardPage = NORMAL
            }
        }
        smartbarManager.onStartInputView(isComposingEnabled)
        smartbarManager!!.toggleBarLayOut(true)
        // update label on Enter key here
    }

    @RequiresApi(Build.VERSION_CODES.LOLLIPOP)
    override fun onFinishInput() {
        currentInputConnection.requestCursorUpdates(0)
        super.onFinishInput()
        currentKeyboardPage = NORMAL
        resetComposingText()
        smartbarManager.onFinishInputView()
    }

    override fun onKeyTouchDown() {
        smartbarManager.setTypeing(true)
    }

    override fun onKeyTouchUp() {
        smartbarManager.setTypeing(false)
    }

    override fun onKey(primaryCode: Int, keyCodes: IntArray?) {
        val inputConnection = currentInputConnection
        if (enableVibration) vibrate()
        if (enableSound) playClick(primaryCode)
        when (primaryCode) {
            Keyboard.KEYCODE_DELETE -> {
                handleDelete()
            }
            KEYCODE_ABC -> {
                currentKeyboardPage = NORMAL
                return
            }
            Keyboard.KEYCODE_SHIFT -> {
                currentKeyboardPage = SHIFT
                return
            }
            KEYCODE_UNSHIFT -> {
                currentKeyboardPage = NORMAL
                return
            }
            KEYCODE_123 -> {
                currentKeyboardPage = SYMBOL
                return
            }
            KEYCODE_123_SHIFT -> {
                currentKeyboardPage = SYMBOL_SHIFT
                return
            }
            KEYCODE_123_UNSHIFT -> {
                currentKeyboardPage = SYMBOL
                return
            }

            KEYCODE_NUMBER_SHIFT -> {
                currentKeyboardPage = NUMBER
                return
            }

            KEYCODE_LANGUAGE -> {
//                val mgr = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager?
//                mgr?.showInputMethodPicker()
                changeLanguage(1)
            }
            Keyboard.KEYCODE_DONE -> {
                handleEnter()
            }
            else -> {
                inputConnection.beginBatchEdit()
                resetComposingText()
                var space = ""
                if (candidateChoosed) {
                    if ((primaryCode.toChar() in 'ក'..'ឳ') || isAlphabet(primaryCode)) {
                        Log.i("hello", preCandidateKhmer.toString())
                        if(preCandidateKhmer) {
                            space = "​"
                        } else {
                            space = " "
                        }
                    }
                }
                inputConnection.commitText(space + primaryCode.toChar().toString(), 1)
                inputConnection.endBatchEdit()
            }
        }
        candidateChoosed = false
        // Switch back to normal if the selected page type is shift.
        if (currentKeyboardPage == SHIFT) {
            currentKeyboardPage = NORMAL
        }
    }

    fun commitCandidate(candidateText: String) {
        candidateChoosed = true
        preCandidateKhmer = !(candidateText[0] in 'a'..'z' || candidateText[0] in 'A'..'Z')
        val ic = currentInputConnection
        ic.setComposingText(candidateText, 1)
        ic.finishComposingText()

    }

    fun sendKeyPress(keyData: KeyData) {
        val ic = currentInputConnection
        when (keyData.code) {
            Keyboard.KEYCODE_DELETE -> handleDelete()
            Keyboard.KEYCODE_DONE -> handleEnter()
            else -> {
                ic.beginBatchEdit()
                resetComposingText()
                val text = keyData.code.toChar().toString()
                ic.commitText(text, 1)
                ic.endBatchEdit()
            }
        }
    }

    @RequiresApi(Build.VERSION_CODES.LOLLIPOP)
    override fun onUpdateCursorAnchorInfo(cursorAnchorInfo: CursorAnchorInfo?) {
        cursorAnchorInfo ?: return
        //super.onUpdateCursorAnchorInfo(cursorAnchorInfo)
        val ic = currentInputConnection

        if (isComposingEnabled) {
            var inputText = ""
            if (cursorAnchorInfo.selectionEnd - cursorAnchorInfo.selectionStart == 0) {
                val newCursorPos = cursorAnchorInfo.selectionStart
                val prevComposingText = (cursorAnchorInfo.composingText ?: "").toString()
                inputText =
                    (ic.getExtractedText(ExtractedTextRequest(), 0)?.text ?: "").toString()
                var oldStart = composingTextStart
                var oldEnd = composingTextStart?.plus(composingText!!.length)
                setComposingTextBasedOnInput(inputText, newCursorPos)
                var newEnd = composingTextStart?.plus(composingText!!.length)
                if ((oldStart == composingTextStart) && (oldEnd == newEnd)) {
                    // Ignore this, as nothing has changed
                } else {
                    if (composingText != null && composingTextStart != null) {
                        ic.setComposingRegion(
                            composingTextStart!!,
                            composingTextStart!! + composingText!!.length
                        )
                    } else {
                        resetComposingText()
                    }
                }
            } else {
                resetComposingText()
            }

//            Log.i("hello", previousWords.toString())
            smartbarManager.generateCandidatesFromComposing(inputText, composingText)
        }
    }

    private fun setComposingTextBasedOnInput(inputText: String, inputCursorPos: Int) {
        // goal by given input and current cursor
        // findTextIngroup of cursor position
        // get its start and end index
//        val words = inputText.split("[^\\p{L}]".toRegex())
        val words = inputText.split("[​.,!@#$%^&*()\"\' ]".toRegex())
        var pos = 0
        Log.i("hello", inputText)
        Log.i("hello", words.toString())
        resetComposingText(false)
        previousWords = mutableListOf()
        previousWords.add("START")
        for (word in words) {
            if (inputCursorPos >= pos && inputCursorPos <= pos + word.length && word.isNotEmpty()) {
                composingText = word
                composingTextStart = pos
                break
            } else {
                pos += word.length + 1
                if ((word == ".") or (word == "!") or (word == "?")) {
                    previousWords.add("START")
                } else if(word.isNotEmpty()) {
                    previousWords.add(word.toLowerCase())
                }

            }
        }
    }

    private fun handleDelete() {
        val ic = currentInputConnection
        ic.beginBatchEdit()
        resetComposingText()
        ic.sendKeyEvent(
            KeyEvent(
                KeyEvent.ACTION_DOWN,
                KeyEvent.KEYCODE_DEL
            )
        )
        ic.endBatchEdit()
    }
    private fun handleEnter() {
        val ic = currentInputConnection
        ic.beginBatchEdit()
        resetComposingText()
        val action = currentInputEditorInfo.imeOptions
        if (action and EditorInfo.IME_FLAG_NO_ENTER_ACTION > 0) {
            currentInputConnection.sendKeyEvent(
                KeyEvent(
                    KeyEvent.ACTION_DOWN,
                    KeyEvent.KEYCODE_ENTER
                )
            )
        } else {
            when (action and EditorInfo.IME_MASK_ACTION) {
                EditorInfo.IME_ACTION_DONE,
                EditorInfo.IME_ACTION_GO,
                EditorInfo.IME_ACTION_NEXT,
                EditorInfo.IME_ACTION_PREVIOUS,
                EditorInfo.IME_ACTION_SEARCH,
                EditorInfo.IME_ACTION_SEND -> {
                    currentInputConnection.performEditorAction(action)
                }
                else -> {
                    currentInputConnection.sendKeyEvent(
                        KeyEvent(
                            KeyEvent.ACTION_DOWN,
                            KeyEvent.KEYCODE_ENTER
                        )
                    )
                }
            }
        }
        ic.endBatchEdit()
    }
    private fun resetComposingText(notifyInputConnection: Boolean = true) {
        if (notifyInputConnection) {
            val ic = currentInputConnection
            ic.finishComposingText()
        }
        composingText = null
        composingTextStart = null
    }

    private fun isAlphabet(code: Int): Boolean {
        return Character.isLetter(code)
    }

    private fun vibrate() {
        val vibrator = getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            vibrator.vibrate(VibrationEffect.createOneShot(20, 150))
        } else {
            vibrator.vibrate(20)
        }
    }

    private fun playClick(keyCode: Int) {
        val audioManager = getSystemService(Context.AUDIO_SERVICE) as AudioManager
        when (keyCode) {
            32 -> audioManager.playSoundEffect(AudioManager.FX_KEYPRESS_SPACEBAR)
            Keyboard.KEYCODE_DONE, 10 -> audioManager.playSoundEffect(AudioManager.FX_KEYPRESS_RETURN)
            Keyboard.KEYCODE_DELETE -> audioManager.playSoundEffect(AudioManager.FX_KEYPRESS_DELETE)
            else -> audioManager.playSoundEffect(AudioManager.FX_KEYPRESS_STANDARD)
        }
    }

    private fun loadKeyCodes() {
        KEYCODE_UNSHIFT = resources.getInteger(R.integer.keycode_unshift)
        KEYCODE_ABC = resources.getInteger(R.integer.keycode_abc)
        KEYCODE_123 = resources.getInteger(R.integer.keycode_sym)
        KEYCODE_123_SHIFT = resources.getInteger(R.integer.keycode_sym_shift)
        KEYCODE_123_UNSHIFT = resources.getInteger(R.integer.keycode_sym_unshift)
        KEYCODE_NUMBER_SHIFT = resources.getInteger(R.integer.keycode_number_shift)
        KEYCODE_SPACE = resources.getInteger(R.integer.keycode_space)
        KEYCODE_LANGUAGE = resources.getInteger(R.integer.keycode_switch_next_keyboard)
        KEYCODE_NA_PO_MYA_NA = resources.getInteger(R.integer.keycode_na_po_mya_na)
        KEYCODE_MYA_TI_MYA_NA = resources.getInteger(R.integer.keycode_mya_ti_mya_na)
        KEYCODE_MYA_TI = resources.getInteger(R.integer.keycode_mya_ti)
        KEYCODE_MYA_NA = resources.getInteger(R.integer.keycode_mya_na)
        KEYCODE_NA_PO = resources.getInteger(R.integer.keycode_na_po)
    }

    companion object {
        var KEYCODE_NONE = -777
        var KEYCODE_UNSHIFT = KEYCODE_NONE
        var KEYCODE_ABC = KEYCODE_NONE
        var KEYCODE_123 = KEYCODE_NONE
        var KEYCODE_123_SHIFT = KEYCODE_NONE
        var KEYCODE_123_UNSHIFT = KEYCODE_NONE
        var KEYCODE_NUMBER_SHIFT = KEYCODE_NONE
        var KEYCODE_SPACE = KEYCODE_NONE
        var KEYCODE_NA_PO_MYA_NA = KEYCODE_NONE
        var KEYCODE_MYA_TI_MYA_NA = KEYCODE_NONE
        var KEYCODE_LANGUAGE = KEYCODE_NONE
        var KEYCODE_NA_PO = KEYCODE_NONE
        var KEYCODE_MYA_NA = KEYCODE_NONE
        var KEYCODE_MYA_TI = KEYCODE_NONE

        const val RES_IDX = 1
        const val SHIFT_IDX = 2
        const val SYM_IDX = 3
        const val SYM_SHIFT_IDX = 4
        const val NUMBER_IDX = 5
    }
}
