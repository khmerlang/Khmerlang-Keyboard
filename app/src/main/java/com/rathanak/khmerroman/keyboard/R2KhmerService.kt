package com.rathanak.khmerroman.keyboard

import android.content.Context
import android.content.res.TypedArray
import android.graphics.Color
import android.graphics.PorterDuff
import android.graphics.PorterDuffColorFilter
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
import android.view.Window
import android.view.inputmethod.*
import android.widget.LinearLayout
import androidx.annotation.ColorInt
import androidx.annotation.ColorRes
import androidx.annotation.RequiresApi
import androidx.appcompat.content.res.AppCompatResources
import androidx.core.graphics.drawable.DrawableCompat
import com.rathanak.khmerroman.R
import com.rathanak.khmerroman.data.KeyboardPreferences
import com.rathanak.khmerroman.data.KeyboardPreferences.Companion.KEY_NEEDS_RELOAD
import com.rathanak.khmerroman.data.KeyboardPreferences.Companion.KEY_NEEDS_RELOAD_STYLE
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
import com.rathanak.khmerroman.spelling_corrector.SpellCorrector
import com.rathanak.khmerroman.utils.WordTokenizer
import com.rathanak.khmerroman.view.inputmethodview.CustomInputMethodView
import com.rathanak.khmerroman.view.inputmethodview.KeyboardActionListener
import kotlinx.coroutines.*
import org.json.JSONObject
import org.json.JSONTokener
import java.net.URL
import java.util.*
import kotlin.properties.Delegates

class R2KhmerService : InputMethodService(), KeyboardActionListener {
    private var customInputMethodView: CustomInputMethodView? = null
    private lateinit var keyboardNormal: CustomKeyboard
    private lateinit var keyboardShift: CustomKeyboard
    private lateinit var keyboardSymbol: CustomKeyboard
    private lateinit var keyboardSymbolShift: CustomKeyboard
    private lateinit var keyboardNumber: CustomKeyboard
    private lateinit var wordTokenize: WordTokenizer
    private var languageNames: MutableList<String> = mutableListOf()
    private var languageXmlRes: MutableList<Int> = mutableListOf()
    private var languageShiftXmlRes: MutableList<Int> = mutableListOf()
    private var languageSymbolXmlRes: MutableList<Int> = mutableListOf()
    private var languageSymbolShiftXmlRes: MutableList<Int> = mutableListOf()
    private var languageNumberXmlRes: MutableList<Int> = mutableListOf()
    private var keyboardsOfLanguages = SparseArray<SparseArray<CustomKeyboard>>()

    private var currentSelectedLanguageIdx = 0
    private var enableVibration = false
    private var enableSound = false
    private var isDarkMood= false
    private var candidateChoosed = false
    private var firstCommitCandidate = false
    private var preCandidateKhmer = false
    private var composingText: String? = null
    private var composingTextStart: Int? = null
    private var isComposingEnabled: Boolean = false
    private var previousWord = ""
    private var isKeyDown: Boolean = false
    var currentInputPassword: Boolean = false
    private lateinit var preferences: KeyboardPreferences
    var bannerIdsData: MutableList<String> = mutableListOf()
    var currentBannerIndex = 0
    var bannerTargetUrl = "http://khmerlang.com/"
    var lastFetchBannerAt: Date? = null

    private val smartbarManager: SmartbarManager = SmartbarManager(this)
    var rootView: LinearLayout? = null
    val context: Context
        get() = rootView?.context ?: this

    var currentKeyboardPage by Delegates.observable<Int?>(null) { _, _, newPage ->
        newPage?.let {
            customInputMethodView?.updateKeyboardPage(newPage)
        }
    }

    override fun onCreate() {
        super.onCreate()
        initSharedPreference()
        loadKeyCodes()
        initKeyboards()
        wordTokenize = WordTokenizer(context)
    }

    override fun onDestroy() {
        super.onDestroy()
        wordTokenize.destroy()
    }

    private fun initSharedPreference() {
        preferences = KeyboardPreferences(applicationContext)
    }

    override fun onWindowShown() {
        super.onWindowShown()
        if (preferences.getBoolean(KEY_NEEDS_RELOAD)) {
            loadSharedPreferences()
            loadStyles()
        }

        // check if banner recent or long load
        val currentDate = Date()
        if(lastFetchBannerAt == null || (currentDate.time - lastFetchBannerAt!!.time >= 30*60*1000)) {
            val job= GlobalScope.launch(Dispatchers.Main) {
                loadBannerData()
            }
        }

        if(bannerIdsData.isNotEmpty()) {
            currentBannerIndex = (currentBannerIndex + 1) % bannerIdsData.size
            smartbarManager.setBannerImage(BANNER_IMAGE + bannerIdsData[currentBannerIndex])
            bannerTargetUrl = BANNER_VISIT + bannerIdsData[currentBannerIndex]
        }
    }

    private suspend fun loadBannerData() {
        coroutineScope {
            async(Dispatchers.IO) {
                try {
                    val result = URL(BANNER_META).readText()
                    val data = JSONTokener(result).nextValue() as JSONObject
                    val bannerIds = data.getJSONArray("banner_ids")
                    if (bannerIds != null) {
                        bannerIdsData.clear()
                        for (i in 0 until bannerIds.length()) {
                            bannerIdsData.add(bannerIds.getString(i))
                        }
                    }
                    if(bannerIdsData.isNotEmpty()) {
                        lastFetchBannerAt = Date()
                    }
                } catch (e: Exception) {
                    bannerIdsData.clear()
                }
            }
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

    private fun loadSharedPreferences() {
        currentSelectedLanguageIdx = preferences.getInt(KeyboardPreferences.KEY_CURRENT_LANGUAGE_IDX, 0)
        enableVibration = preferences.getBoolean(KeyboardPreferences.KEY_ENABLE_VIBRATION)
        enableSound = preferences.getBoolean(KeyboardPreferences.KEY_ENABLE_SOUND)
        isDarkMood = preferences.getBoolean(KeyboardPreferences.KEY_ENABLE_DARK_MOOD)
        preferences.putBoolean(KeyboardPreferences.KEY_NEEDS_RELOAD, false)
    }

    private fun loadStyles() {
        // Load the styles and store them as Singleton values
        if (isDarkMood) {
            Styles.keyboardStyle = KeyboardStyle(getColorInt(R.color.dark_keyboard_background_color))
            Styles.keyStyle = KeyStyle(
                getColorInt(R.color.dark_key_normal_background_color),
                getColorInt(R.color.dark_key_pressed_background_color),
                getColorInt(R.color.dark_key_shadow_color),
                getColorInt(R.color.dark_key_label_color),
                getColorInt(R.color.dark_key_sub_label_color),
                PorterDuffColorFilter(getColorInt(R.color.dark_key_label_color), PorterDuff.Mode.SRC_IN)
            )
        } else {
            Styles.keyboardStyle = KeyboardStyle(getColorInt(R.color.default_keyboard_background_color))
            Styles.keyStyle = KeyStyle(
                getColorInt(R.color.default_key_normal_background_color),
                getColorInt(R.color.default_key_pressed_background_color),
                getColorInt(R.color.default_key_shadow_color),
                getColorInt(R.color.default_key_label_color),
                getColorInt(R.color.default_key_sub_label_color),
                PorterDuffColorFilter(getColorInt(R.color.default_key_label_color), PorterDuff.Mode.SRC_IN)
            )
        }
        Styles.keyStyle.subLabelPaint.textSize = resources.getDimension(R.dimen.default_sub_key_text_size)
        Styles.keyStyle.labelPaint.textSize = resources.getDimension(R.dimen.default_key_text_size)
        customInputMethodView?.setBackgroundColor(Styles.keyboardStyle.keyboardBackground)
        smartbarManager.setDarkMood(isDarkMood)
    }

    @ColorInt
    fun getColorInt(@ColorRes res: Int): Int {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            resources.getColor(res, null)
        } else {
            context.resources.getColor(res);
        }
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

    override fun onSwipeRight() {}
    override fun onSwipeLeft() {}
    override fun onSwipeUp() {}
    override fun onSwipeDown() {}

    override fun onChangeKeyboardSwipe(direction: Int) {
//        val mgr = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager?
//        mgr?.showInputMethodPicker()
        changeLanguage(direction)
    }

    private fun saveCurrentState() {
        preferences.putInt(KeyboardPreferences.KEY_CURRENT_LANGUAGE_IDX, currentSelectedLanguageIdx)
    }

    private fun changeLanguage(direction: Int) {
        currentSelectedLanguageIdx = ((currentSelectedLanguageIdx + direction) + languageNames.size) % languageNames.size
        saveCurrentState()
        renderCurrentLanguage()
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
//        currentInputConnection.requestCursorUpdates(0)
        super.onFinishInput()
        currentInputConnection.requestCursorUpdates(0)
        currentKeyboardPage = NORMAL
        resetComposingText()
        smartbarManager.onFinishInputView()
    }

    override fun onKeyTouchDown() {
        isKeyDown = true
        smartbarManager.setTypeing(true)
        if (enableVibration) vibrate()
    }

    override fun onKeyTouchUp() {
        isKeyDown = false
        smartbarManager.setTypeing(false)
    }

    override fun onKey(primaryCode: Int, keyCodes: IntArray?) {
        val inputConnection = currentInputConnection
//        if (enableVibration) vibrate()
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

            KEYCODE_MODE_CHANGE -> {
                changeLanguage(1)
            }
            KEYCODE_LANGUAGE -> {
                val mgr = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager?
                mgr?.showInputMethodPicker()
            }
            Keyboard.KEYCODE_DONE -> {
                handleEnter()
            }
            else -> {
                var space = ""
                if (candidateChoosed) {
                    if ((primaryCode.toChar() in 'ក'..'ឳ') || isAlphabet(primaryCode)) {
                        space = getSpaceBy(preCandidateKhmer)
                    }
                }

                var keyCh = if(primaryCode == KEYCODE_OM) {
                    "ុំ"
                } else if (primaryCode == KEYCODE_AM) {
                    "ាំ"
                } else if (primaryCode == KEYCODE_AS) {
                    "េះ"
                } else if (primaryCode == KEYCODE_OS) {
                    "ោះ"
                } else if (primaryCode == KEYCODE_ORS) {
                    "ុះ"
                } else {
                    primaryCode.toChar().toString();
                }
                inputConnection.beginBatchEdit()
                resetComposingText()
                inputConnection.commitText(space + keyCh, 1)
                inputConnection.endBatchEdit()
            }
        }
        candidateChoosed = false
        firstCommitCandidate = false
        // Switch back to normal if the selected page type is shift.
        if (currentKeyboardPage == SHIFT) {
            currentKeyboardPage = NORMAL
        }
    }

    fun commitCandidate(candidateText: String) {
        preCandidateKhmer = !(candidateText[0] in 'a'..'z' || candidateText[0] in 'A'..'Z')
        val ic = currentInputConnection
        var text = candidateText
        ic.beginBatchEdit()
        ic.setComposingText(text, 1)
        ic.finishComposingText()
        ic.endBatchEdit()
        candidateChoosed = true
        firstCommitCandidate = true
        smartbarManager.generateCandidatesFromComposing("", "", "")
    }

    private fun getSpaceBy(isKhmer: Boolean): String {
      return if(isKhmer) {
            "​"
        } else {
            " "
        }
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

    /**
     * Deal with the editor reporting movement of its cursor.
     */
    override fun onUpdateSelection(
        oldSelStart: Int, oldSelEnd: Int,
        newSelStart: Int, newSelEnd: Int,
        candidatesStart: Int, candidatesEnd: Int
    ) {
        super.onUpdateSelection(
            oldSelStart, oldSelEnd,
            newSelStart, newSelEnd,
            candidatesStart, candidatesEnd
        )
        if (candidateChoosed && firstCommitCandidate) {
            firstCommitCandidate = false
            return
        }

        if (isKeyDown) {
            return
        }

        val ic = currentInputConnection
        if (isComposingEnabled) {
            var inputText = ""
            if (newSelEnd - newSelStart == 0) {
                inputText =
                    (ic.getExtractedText(ExtractedTextRequest(), 0)?.text ?: "").toString()
                var oldStart = composingTextStart
                var oldEnd = composingTextStart?.plus(composingText!!.length)
                setComposingTextBasedOnInput(inputText, newSelStart)

                var newEnd = composingTextStart?.plus(composingText!!.length)
                if (((oldStart == composingTextStart) && (oldEnd == newEnd))) {
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
            smartbarManager.generateCandidatesFromComposing(inputText, previousWord, composingText)
        }
    }

    private fun setComposingTextBasedOnInput(inputText: String, inputCursorPos: Int) {
        // goal by given input and current cursor
        // findTextIngroup of cursor position
        // get its start and end index
        var startSlice = inputCursorPos - 40
        var endSlice = inputCursorPos + 20
        if (startSlice < 0) {
            startSlice = 0
        }
        if (endSlice > inputText.length - 1) {
            endSlice = inputText.length - 1
        }
        val selectInput = inputText.slice(startSlice..endSlice)
        val words = wordTokenize.tokenize(selectInput)
        var pos = startSlice
        resetComposingText(false)
        previousWord = "START"
        var currentWord = ""
        var wordIndex = 0
        for (i in startSlice..endSlice) {
            if(wordIndex >= words.size) {
                break
            }

            if (WordTokenizer.SEG_SYMBOL.contains(inputText[i].toString())) {
                currentWord = ""
                pos = i + 1
                continue
            }
            currentWord += inputText[i].toString()
            if (currentWord == words[wordIndex]) {
                if (WordTokenizer.CHAR_SYMBOL.contains(inputText[i])) {
                    currentWord = ""
                    pos = i + 1
                    wordIndex += 1
                    continue
                } else if ((inputCursorPos >= pos) && inputCursorPos <= (pos + words[wordIndex].length) && words[wordIndex].isNotEmpty()) {
                    composingText = words[wordIndex]
                    composingTextStart = pos
                    break
                } else {
                    if (words[wordIndex].length == 1 && WordTokenizer.CHAR_SYMBOL.contains(words[wordIndex][0])) {
                        previousWord = "START"
                    } else if(words[wordIndex].isNotEmpty()) {
                        previousWord = words[wordIndex].toLowerCase()
                    }
                    wordIndex += 1
                }
                pos = i + 1
                currentWord = ""
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
        val inputText =
            (ic.getExtractedText(ExtractedTextRequest(), 0)?.text ?: "").toString()
        if(inputText.isEmpty()) {
            resetComposingText()
            smartbarManager.generateCandidatesFromComposing(inputText, previousWord, composingText)
        }
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
        KEYCODE_MODE_CHANGE = -2
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
        KEYCODE_AM = resources.getInteger(R.integer.keycode_am)
        KEYCODE_OM = resources.getInteger(R.integer.keycode_om)
        KEYCODE_OS = resources.getInteger(R.integer.keycode_os)
        KEYCODE_AS = resources.getInteger(R.integer.keycode_as)
        KEYCODE_ORS = resources.getInteger(R.integer.keycode_ors)
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
        var KEYCODE_MODE_CHANGE = KEYCODE_NONE
        var KEYCODE_NA_PO = KEYCODE_NONE
        var KEYCODE_MYA_NA = KEYCODE_NONE
        var KEYCODE_MYA_TI = KEYCODE_NONE
        var KEYCODE_AM = KEYCODE_NONE
        var KEYCODE_OM = KEYCODE_NONE
        var KEYCODE_OS = KEYCODE_NONE
        var KEYCODE_AS = KEYCODE_NONE
        var KEYCODE_ORS = KEYCODE_NONE

        const val RES_IDX = 1
        const val SHIFT_IDX = 2
        const val SYM_IDX = 3
        const val SYM_SHIFT_IDX = 4
        const val NUMBER_IDX = 5
        const val BANNER_META = "https://banner.khmerlang.com/mobile/meta"
        const val BANNER_IMAGE = "https://banner.khmerlang.com/mobile/images/"
        const val BANNER_VISIT = "https://banner.khmerlang.com/mobile/visits/"

        var spellingCorrector: SpellCorrector = SpellCorrector()
        var jobLoadData: Job? = null
        var dataStatus = KeyboardPreferences.STATUS_NONE
    }
}
