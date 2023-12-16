package com.rathanak.khmerroman.view.inputmethodview

import android.annotation.SuppressLint
import android.content.Context
import android.os.AsyncTask
import android.os.Handler
import android.os.Message
import android.util.AttributeSet
import android.util.DisplayMetrics
import android.util.Log
import android.util.SparseArray
import android.view.*
import android.widget.LinearLayout
import com.rathanak.khmerroman.BuildConfig
import com.rathanak.khmerroman.R
import com.rathanak.khmerroman.keyboard.common.PageType.Companion.NORMAL
import com.rathanak.khmerroman.keyboard.common.PageType.Companion.PAGE_TYPES
import com.rathanak.khmerroman.keyboard.common.Styles
import com.rathanak.khmerroman.keyboard.extensions.forEach
import com.rathanak.khmerroman.keyboard.keyboardinflater.CustomKeyboard
import com.rathanak.khmerroman.view.keyview.CustomKeyPreview
import com.rathanak.khmerroman.view.keyview.CustomKeyView


/**
 * The parent ViewGroup of the keyboard. The orientation is vertical.
 */
class CustomInputMethodView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : LinearLayout(context, attrs, defStyleAttr) {

    /**
     * A handler handler for sending repeated keys.
     */
    private lateinit var keyboardHandler: Handler
    /**
     * A handler for sending long pressed keys.
     */
    private lateinit var longClickHandler: Handler

    /**
     * Styling for each key from [R.styleable.CustomInputMethodView]
     */
    private var globalKeyTextSize: Float = 0.0f

    /**
     * Currently pressed keys. This is a map from touch finger id to the key view.
     */
    private val pressedKeys = SparseArray<CustomKeyView>()

    /**
     * Collection of views for the currently selected keyboard. 2D array is for pages and key views.
     */
    private var currentKeyboard = InputMethodKeyboard()

    /**
     * Collection of modifier/special keys. The integers are defined in the resources/integers
     */
    private lateinit var modifierKeys: List<Int>
    /**
     * Collection of currently rendered preview keys.
     */
    private val renderedPreviewKeys = mutableListOf<CustomKeyPreview>()

    /**
     * Collection of key views that has been already inflated for languages.
     */
    private var preloadedKeyboardViews = SparseArray<InputMethodKeyboard>()

    /**
     * Page of the currently selected keyboard [currentKeyboard]
     */
    private var currentKeyboardPage: Int = NORMAL

    /**
     * Listener for [CustomInputMethodService]
     */
    var keyboardViewListener: KeyboardActionListener? = null

    /**
     * Gesture detector for fling (changing language), down, up, and repeat
     */
    private lateinit var gestureDetector: GestureDetector

    // Current screen orientation
    private var isLandscape = false

    private var isLongPress = false

    private var oldLabel = ""

    init {
        setBackgroundColor(Styles.keyboardStyle.keyboardBackground)

        /*
        * Load the styles from the keyboard xml for the child keys. Keyboard should be the only place
        * where we set the styles for the children views.
        * */
        val a = context.obtainStyledAttributes(attrs, R.styleable.CustomInputMethodView)
        globalKeyTextSize = a.getDimension(
            R.styleable.CustomInputMethodView_keyTextSize, resources.getDimension(
                R.dimen.default_key_text_size
            )
        )

        // recycle the typed array
        a.recycle()

        // Set orientation for the rows
        orientation = VERTICAL

        // Determine the current screen orientation
        determineScreenMode()

        // Get list of modifier keys
        generateModKeysList()

        // Prepare paints for the key views
        Styles.keyStyle.subLabelPaint.textSize = resources.getDimension(R.dimen.default_sub_key_text_size)
        Styles.keyStyle.labelPaint.textSize = resources.getDimension(R.dimen.default_key_text_size)
    }

    /**
     * Preload the key views for all of the provided [keyboards]. Load the current language first and
     * load the other languages in the background.
     */
    fun prepareAllKeyboardsForRendering(
        keyboards: SparseArray<SparseArray<CustomKeyboard>>,
        currentLanguageIdx: Int
    ) {
        // Generate other keyboard views in the background
        AsyncTask.execute {
            keyboards.forEach { key, value ->
                if (key != currentLanguageIdx) {
                    generateKeyboardViews(value, key)
                }
            }
        }
        generateKeyboardViews(keyboards[currentLanguageIdx], currentLanguageIdx)
    }

    /**
     * Generate keyboard views and add them to the preloaded keyboard views.
     */
    private fun generateKeyboardViews(keyboard: SparseArray<CustomKeyboard>, languageIdx: Int) {
        if (preloadedKeyboardViews.get(languageIdx) == null) {
            val inputMethodKeyboard = InputMethodKeyboard()
            PAGE_TYPES.forEach { type ->
                keyboard.get(type).let { page ->
                    page.formattedKeyList.let { keys ->
                        inputMethodKeyboard.generateKeyViews(
                            context,
                            type,
                            keys,
                            page.language,
                            isLandscape
                        )
                    }
                }
            }
            preloadedKeyboardViews.put(languageIdx, inputMethodKeyboard)
        }
    }

    override fun onAttachedToWindow() {
        super.onAttachedToWindow()
        initGestureDetector()
        keyboardHandler = Handler(Handler.Callback { msg ->
            when (msg.what) {
                MSG_REPEAT -> {
                    val pointerId = msg.obj
                    if (pointerId is Int && sendKey(pressedKeys[pointerId])) {
                        val repeatMsg = Message.obtain(keyboardHandler, MSG_REPEAT)
                        repeatMsg.obj = pointerId
                        keyboardHandler.sendMessageDelayed(repeatMsg, REPEAT_INTERVAL.toLong())
                    }
                }
            }
            false
        })

        // A handler to send after a delayed message from a long click.
        longClickHandler = Handler(Handler.Callback { lngClkMsg ->
            when (lngClkMsg.what) {
                MSG_LONG_CLICK -> {
                    val pointerId = lngClkMsg.obj
                    val msg = keyboardHandler.obtainMessage(MSG_REPEAT)
                    msg.obj = pointerId
                    keyboardHandler.sendMessage(msg)
                }
                MSG_LONG_CLICK_SHIFT -> {
                    val pointerId = lngClkMsg.obj
                    renderKeyPreview(pressedKeys[pointerId as Int])
                    isLongPress = true
                }
            }
            false
        })
    }

    private fun initGestureDetector() {
        gestureDetector = GestureDetector(
            context,
            object : GestureDetector.SimpleOnGestureListener() {
                override fun onFling(
                    e1: MotionEvent?,
                    e2: MotionEvent,
                    velocityX: Float,
                    velocityY: Float
                ): Boolean {
                    var isChangeLanguageSwipe = 0x000
                    var direction = 0
                    var swipeThreshold = width / 2
                    val e1PointerId = e1?.getPointerId(e1.actionIndex)
                    val e2PointerId = e2.getPointerId(e2.actionIndex)
                    if (e1PointerId != e2PointerId) return false
                    // Check if the swipe is within the area of the language switch key.
                    val e1Key = detectKey(e1.getX(e1.actionIndex), e1.getY(e1.actionIndex))
                    val e2Key = detectKey(e2.getX(e2.actionIndex), e2.getY(e1.actionIndex))
                    if (e1Key?.isChangeLanguage == true &&
                        e2Key?.isChangeLanguage == true
                    ) {
                        isChangeLanguageSwipe = 0x001 // 0x001 for being inside the key view.
                        swipeThreshold = e1Key.width / 4
                    }

                    var result = false
                    val distanceY = e2.y - e1.y
                    val distanceX = e2.x - e1.x
                    if (Math.abs(distanceX) > Math.abs(distanceY) &&
                        Math.abs(distanceX) > swipeThreshold &&
                        Math.abs(velocityX) > SWIPE_VELOCITY_THRESHOLD
                    ) {
                        direction = if (distanceX > 0) {
                            keyboardViewListener?.onSwipeRight()
                            KeyboardActionListener.SWIPE_DIRECTION_RIGHT
                        } else {
                            keyboardViewListener?.onSwipeLeft()
                            KeyboardActionListener.SWIPE_DIRECTION_LEFT
                        }
                        isChangeLanguageSwipe =
                            isChangeLanguageSwipe or 0x010 // 0x011 for both being in the change language key view and swiping right or left.
                    } else if (Math.abs(distanceY) > height / 2 &&
                        Math.abs(velocityY) > SWIPE_VELOCITY_THRESHOLD
                    ) {
                        if (distanceY > 0) {
                            keyboardViewListener?.onSwipeDown()
                        } else {
                            keyboardViewListener?.onSwipeUp()
                        }
                    }
                    if (isChangeLanguageSwipe == 0x011) {
                        keyboardViewListener?.onChangeKeyboardSwipe(direction)
                        result = true
                    }
                    return result
                }
            })
    }

    /**
     * Update the input method view to a different language keyboard with [NORMAL] page as starting
     * page and call [updateKeyboardPage] to redraw.
     */
    fun updateKeyboardLanguage(languageIdx: Int) {
        // Get the preloadedKyeboardViews with the new languageIdx and assign it to current keyboard
        // to render.
        preloadedKeyboardViews[languageIdx]?.let {
            currentKeyboard = it
        }
        // Set the starting page to Normal
        currentKeyboardPage = NORMAL

        updateKeyboardPage(currentKeyboardPage)
    }

    /**
     * Update the current keyboard page to a specified page type and redraw.
     */
    fun updateKeyboardPage(pageType: Int, isForce: Boolean = false) {
        currentKeyboardPage = pageType
        invalidate()
        populateKeyViews(pageType, isForce)
    }

    /**
     * Populate the key views based on the type of the page
     * @Param type This is from CustomMainKeyboardView.Type
     * */
    private fun populateKeyViews(type: Int, isForce: Boolean) {
        // TODO Debug why this can be null
        if (currentKeyboard.pages[type] == null) {
            return
        }

        // Check if the size of the rows of the pages are the same. If so, reuse the previous
        // row layout.
        if (currentKeyboard.pages[type].size == childCount && !isForce) {
            // Use the existing row linear layouts.
            currentKeyboard.pages[type].forEachIndexed { idx, row ->
                val rowLinearLayout = getChildAt(idx) as LinearLayout
                rowLinearLayout.removeAllViews()
                row.forEach { key ->
                    if(key.parent != null) {
                        (key.parent as ViewGroup).removeView(key)
                    }
                    rowLinearLayout.addView(key)
                }
            }
        } else {
            removeAllKeyViews() // Remove keys from the parent view
            removeAllViews() // Remove all of the row Linear Layout
            currentKeyboard.pages[type].forEach { row ->
                val rowLinearLayout = LinearLayout(context)
                rowLinearLayout.layoutParams = LayoutParams(
                    LayoutParams.MATCH_PARENT,
                    LayoutParams.WRAP_CONTENT
                )
                rowLinearLayout.orientation = HORIZONTAL
                rowLinearLayout.gravity = Gravity.CENTER
                row.forEach { key ->
                    if(key.parent != null) {
                        (key.parent as ViewGroup).removeView(key)
                    }
                    rowLinearLayout.addView(key)
                }
                addView(rowLinearLayout)
            }
        }
    }

    /*
    * Remove the parents of the keys in order to add new keys to the view.
    * */
    private fun removeAllKeyViews() {
        currentKeyboard.pages[currentKeyboardPage].forEach { row ->
            row.forEach { key ->
                val parent = key.parent as ViewGroup?
                parent?.removeView(key)
            }
        }
    }

    /*
    * Returns the size of the display.
    * */
    private fun getDisplayMetrics(): DisplayMetrics {
        val displayMetrics = DisplayMetrics()
        val windowManager = context.getSystemService(Context.WINDOW_SERVICE) as WindowManager
        windowManager.defaultDisplay.getMetrics(displayMetrics)
        return displayMetrics
    }

    private fun determineScreenMode() {
        val displayMetrics = getDisplayMetrics()
        isLandscape = displayMetrics.heightPixels < displayMetrics.widthPixels
    }

    @SuppressLint("ClickableViewAccessibility")
    override fun onInterceptTouchEvent(event: MotionEvent?): Boolean {
        if (event == null) {
            return false
        }
        processTouchEvent(event)
        return super.onInterceptTouchEvent(event)
    }

    private fun processTouchEvent(event: MotionEvent) {
        if (!gestureDetector.onTouchEvent(event)) {
            val pointerIndex = event.actionIndex
            val pointerId = event.getPointerId(pointerIndex)
            when (event.actionMasked) {
                MotionEvent.ACTION_MOVE -> {
                    detectKey(event.getX(pointerIndex), event.getY(pointerIndex), pointerId, true)
                }
                MotionEvent.ACTION_DOWN,
                MotionEvent.ACTION_POINTER_DOWN -> {
                    keyboardViewListener?.onKeyTouchDown()
                    detectKey(event.getX(pointerIndex), event.getY(pointerIndex), pointerId, false)
                }
                MotionEvent.ACTION_HOVER_EXIT,
                MotionEvent.ACTION_UP,
                MotionEvent.ACTION_POINTER_UP -> {
                    keyboardViewListener?.onKeyTouchUp()
                    sendKey(pressedKeys.get(pointerId))
                    isLongPress = false
                    pressedKeys.remove(pointerId)
                    removeMessages()
                    reStoreKeyPreview()
                }
            }
        }
    }

    private fun detectKey(x: Float, y: Float, pointerId: Int, isMove: Boolean) {
        detectKey(x, y)?.let { pressedKey ->
            // Prevent the repeatable key from repeating upon touch moving out of its view
            if (isMove) {
                if (pressedKeys.get(pointerId) != pressedKey
                    && pressedKeys.get(pointerId).repeatable == true) {
                    removeMessages()
                }
            }

            addPressedKey(pointerId, pressedKey)
            if (!isMove) {
                if (pressedKey.repeatable == true) {
                    /*
                    * Determine if the click is a long press on the repeatable keys.
                    * */
                    val msg = longClickHandler.obtainMessage(MSG_LONG_CLICK)
                    msg.obj = pointerId
                    longClickHandler.sendMessageDelayed(msg, LONG_PRESS_DELAY.toLong())
                }  else {
                    val msg = longClickHandler.obtainMessage(MSG_LONG_CLICK_SHIFT)
                    msg.obj = pointerId
                    longClickHandler.sendMessageDelayed(msg, LONG_PRESS_SHIFT_DELAY.toLong())
                }
            }
        }
    }

    private fun addPressedKey(id: Int, keyView: CustomKeyView) {
        pressedKeys.append(id, keyView)
        renderKeyPreview(keyView)
    }

    private fun removeMessages() {
        longClickHandler.removeMessages(MSG_LONG_CLICK)
        longClickHandler.removeMessages(MSG_LONG_CLICK_SHIFT)
        keyboardHandler.removeMessages(MSG_REPEAT)
    }

    /*
    * Key views are stored in 2d format. First, we check if the tap position is within the parent of
    * the key view's bounds. If so, find the key in that row.
    * */
    private fun detectKey(x: Float, y: Float): CustomKeyView? {
        currentKeyboard.pages[currentKeyboardPage].forEach { row ->
            // Each row is composed in linear layout. Thus, we have to use it to find which row
            // the pointer falls into.
            val rowLinearLayout = row.first().parent as LinearLayout?
            rowLinearLayout?.let {
                val layoutLeft = rowLinearLayout.left.toFloat()
                val layoutRight = rowLinearLayout.right.toFloat()
                val layoutTop = rowLinearLayout.top.toFloat()
                val layoutBottom = rowLinearLayout.bottom.toFloat()
                if (x in layoutLeft..layoutRight &&
                    y in layoutTop..layoutBottom) {
                    // Normalize the tap location because the position of the children view are
                    // relative to the parent's.
                    row.forEach { key ->
                        val keyLeft = key.left.toFloat()
                        val keyRight = key.right.toFloat()
                        val keyTop = key.top.toFloat()
                        val keyBottom = key.bottom.toFloat()
                        if (x - layoutLeft in keyLeft..keyRight &&
                            y - layoutTop in keyTop..keyBottom) {
                            return key
                        }
                    }
                }
            }
        }
        return null
    }

    private fun sendKey(key: CustomKeyView?): Boolean {
        key?.codes?.let { codes ->
            if (codes.isEmpty()) return false
            codes.first().let { primaryCode ->
                // if long press key and key have long press value
                if (isLongPress) {
                    if (BuildConfig.DEBUG) {
                        Log.d(LOG_TAG, "pressed : ${key.subLabel}")
                    }
                    key.longPressCode?.let { longPressCode ->
                        if (longPressCode != 0) {
                            keyboardViewListener?.onKey(longPressCode, codes)
                            return true
                        }
                    }
                }

                if (BuildConfig.DEBUG) {
                    Log.d(LOG_TAG, "pressed : ${key.label}")
                }
                keyboardViewListener?.onKey(primaryCode, codes)
                return true
            }
        }
        return false
    }

    private fun renderKeyPreview(pressedKey: CustomKeyView) {
        pressedKey.key?.let { key ->
            // If the key isn't any of the modifying key, render it
            var isModKey = false
            key.codes.forEach {
                isModKey = isModKey or modifierKeys.contains(it)
                if (!isModKey) return@forEach
            }

            if (!isModKey) {
                val label = if (isLongPress && !key.subLabel.isNullOrEmpty()) {
                    key.subLabel.toString()
                } else {
                    key.label.toString()
                }
//                currentKeyboard.pages[currentKeyboardPage]
                currentKeyboard.changeLanguageKeys.forEach {
                    oldLabel = it.label.toString()
                    it.previewKey(true, label)
                }
            }
        }
    }
    private fun reStoreKeyPreview() {
        currentKeyboard.changeLanguageKeys.forEach {
//            it.previewKey(false, oldLabel)
            it.previewKey(false, "")
        }
    }

    fun generateModKeysList() {
        context.resources.let { res ->
            modifierKeys = listOf(
                res.getInteger(R.integer.keycode_delete),
                res.getInteger(R.integer.keycode_abc),
                res.getInteger(R.integer.keycode_alt),
                res.getInteger(R.integer.keycode_cancel),
                res.getInteger(R.integer.keycode_done),
                res.getInteger(R.integer.keycode_mode_change),
                res.getInteger(R.integer.keycode_shift),
                res.getInteger(R.integer.keycode_space),
                res.getInteger(R.integer.keycode_switch_next_keyboard),
                res.getInteger(R.integer.keycode_unshift),
                res.getInteger(R.integer.keycode_sym)
            )
        }
    }

    private fun clearRenderedKeys() {
        val windowManager: WindowManager = context.getSystemService(Context.WINDOW_SERVICE) as WindowManager
        renderedPreviewKeys.let {
            renderedPreviewKeys.map { keyView -> windowManager.removeView(keyView) }
            renderedPreviewKeys.clear()
        }
    }

    companion object {
        // Messages for handler
        const val MSG_REPEAT = 0
        const val MSG_LONG_CLICK = 1
        const val MSG_LONG_CLICK_SHIFT = 2
        // Time intervals
        const val LONG_PRESS_SHIFT_DELAY = 500
        const val LONG_PRESS_DELAY = 500
        const val REPEAT_INTERVAL = 50 // ~20 keys per second

        const val LOG_TAG = "**KHMERLANG**"

        const val SWIPE_VELOCITY_THRESHOLD = 50
    }
}

interface KeyboardActionListener {
    fun onKeyTouchDown()
    fun onKeyTouchUp()
    fun onKey(primaryCode: Int, keyCodes: IntArray?)
    fun onSwipeLeft()
    fun onSwipeRight()
    fun onSwipeUp()
    fun onSwipeDown()
    fun onChangeKeyboardSwipe(direction: Int)

    companion object {
        const val SWIPE_DIRECTION_LEFT = -1
        const val SWIPE_DIRECTION_RIGHT = 1
    }
}
