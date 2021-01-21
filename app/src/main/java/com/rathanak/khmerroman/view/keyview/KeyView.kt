package com.rathanak.khmerroman.view.keyview

import android.content.Context
import android.graphics.*
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View
import com.rathanak.khmerroman.data.KeyboardPreferences
import com.rathanak.khmerroman.keyboard.common.Styles
import com.rathanak.khmerroman.keyboard.keyboardinflater.CustomKey
import com.rathanak.khmerroman.view.Roman2KhmerApp
import kotlin.math.min

class KeyView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0,
    val key: CustomKey,
    val isLandscape: Boolean
) : View(
    context,
    attrs,
    defStyleAttr
) {
    private var isKeyPressed: Boolean = false

    // Settings for the key
    private val cornerRadius = 15F

    private val widthPaddingRatio = 0.07F
    private var widthPadding = 0F

    private val heightPaddingRatio = 0.05F
    private var heightPadding = 0F

    private val shadowHeightRatio = 0.06F
    private var shadowHeight = 0F

    init {
        // Set this view's background to transparent
        setBackgroundColor(Color.TRANSPARENT)
    }

    var label = key.label as String?
        set(value) {
            field = value
            invalidate()
        }

    override fun onTouchEvent(me: MotionEvent?): Boolean {
        when (me?.actionMasked) {
            MotionEvent.ACTION_DOWN -> {
                isKeyPressed = true
                invalidate()
            }
            MotionEvent.ACTION_MOVE -> {
                // Construct a rect of the view's bounds
                val rect = Rect(left, top, right, bottom)
                if (!rect.contains(left + me.x.toInt(), top + me.y.toInt())) {
                    // User moved outside bounds
                    invalidate()
                    isKeyPressed = false
                }
            }
            MotionEvent.ACTION_CANCEL,
            MotionEvent.ACTION_UP -> {
                isKeyPressed = false
                invalidate()
            }
        }
        return true
    }


    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        drawBackground(canvas)

        if (key.icon !== null) {
            drawIcon(canvas)
        } else {
            drawLabel(canvas)
        }

        // Draw arrows for a change language key
        if (key.isChangeLanguageKey) {
            drawArrowsForLanguage(canvas)
        }
    }

    private fun drawBackground(canvas: Canvas) {
        // Set canvas background to transparent
        canvas.drawColor(Color.TRANSPARENT, PorterDuff.Mode.OVERLAY)

        // Change color based on key press state
        Styles.keyStyle.backgroundPaint.color = if (isKeyPressed) {
            Styles.keyStyle.pressedBackgroundColor
        } else {
            Styles.keyStyle.normalBackgroundColor
        }

        // Calculate the padding for the background
        widthPadding = canvas.width * widthPaddingRatio
        heightPadding = canvas.height * heightPaddingRatio

        shadowHeight = canvas.height * shadowHeightRatio

        // Draw rounded rectangle background
        canvas.drawRoundRect(
            widthPadding,
            heightPadding,
            canvas.width.toFloat() - widthPadding,
            canvas.height.toFloat() - heightPadding,
            cornerRadius,
            cornerRadius,
            Styles.keyStyle.shadowPaint
        )
        canvas.drawRoundRect(
            widthPadding,
            heightPadding,
            canvas.width.toFloat() - widthPadding,
            canvas.height.toFloat() - heightPadding - shadowHeight,
            cornerRadius,
            cornerRadius,
            Styles.keyStyle.backgroundPaint
        )
    }

    private fun drawIcon(canvas: Canvas) {
        var size = 0.6
        val dimension = min(canvas.width, canvas.height) * size
        val widthOffset = ((canvas.width - dimension) / 2).toInt()
        val heightOffset = ((canvas.height - dimension) / 2).toInt()
        key.icon.setBounds(
            widthOffset,
            heightOffset,
            (widthOffset + dimension).toInt(),
            (heightOffset + dimension).toInt()
        )
        key.icon.draw(canvas)
    }

    private fun drawLabel(canvas: Canvas) {
        // Draw the label on the key
        label?.let {
            drawCenter(canvas, Styles.keyStyle.labelPaint, it)
        }

        // Draw the sub label on the key
        val isShowLabel =Roman2KhmerApp.preferences?.getBoolean(KeyboardPreferences.KEY_SHOW_KEY_LABEL_VIEW, false)
        if (key.subLabel != null && isShowLabel!!) {
            var multiplicativeWidthRatio = 1.5F
            var multiplicativeHeightRatio = 1.5F
            if (canvas.width / canvas.height < 0.75) {
                multiplicativeWidthRatio = 1.1F
            }

//            val xPadding = multiplicativeWidthRatio * Styles.keyStyle.subLabelPaint.textSize//canvas.width - 10 - multiplicativeWidthRatio * Styles.keyStyle.subLabelPaint.textSize
            val xPadding = multiplicativeWidthRatio * 20
            val yPadding = multiplicativeHeightRatio * Styles.keyStyle.subLabelPaint.textSize

            canvas.drawText(
                key.subLabel as String,
                xPadding,
                yPadding,
                Styles.keyStyle.subLabelPaint
            )
        }

    }

    private fun drawArrowsForLanguage(canvas: Canvas) {
        val height = (canvas.height * 0.20).toInt()
        // Left side arrow
        drawTriangle(
            (height + canvas.width * widthPaddingRatio).toInt(),
            ((canvas.height / 2) - height / 2),
            height,
            false,
            Styles.keyStyle.arrowPaint,
            canvas
        )
        // Right side arrow
        drawTriangle(
            (canvas.width - (2 * height + canvas.width * widthPaddingRatio)).toInt(),
            ((canvas.height / 2) - height / 2),
            height,
            true,
            Styles.keyStyle.arrowPaint,
            canvas
        )
    }

    private fun drawTriangle(
        x: Int,
        y: Int,
        width: Int,
        isRight: Boolean,
        paint: Paint,
        canvas: Canvas
    ) {
        // Right
        var pA = Point(x, y)
        var pB = Point(x + width, y + width / 2)
        var pC = Point(x, y + width)

        // Left
        if (!isRight) {
            pA = Point(x + width, y)
            pB = Point(x, y + width / 2)
            pC = Point(x + width, y + width)
        }

        val path = Path().apply {
            fillType = Path.FillType.EVEN_ODD
            moveTo(pA.x.toFloat(), pA.y.toFloat())
            lineTo(pB.x.toFloat(), pB.y.toFloat())
            lineTo(pC.x.toFloat(), pC.y.toFloat())
            close()
        }
        canvas.drawPath(path, paint)
    }

    private fun drawCenter(canvas: Canvas, paint: Paint, text: String) {
        val xPos = canvas.width / 2.0F
        val yPos = (canvas.height / 2.0F - (paint.descent() + paint.ascent()) / 2)
        canvas.drawText(text, xPos, yPos, paint)
    }
}