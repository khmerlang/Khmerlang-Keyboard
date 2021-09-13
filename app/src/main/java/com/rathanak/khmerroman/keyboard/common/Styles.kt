package com.rathanak.khmerroman.keyboard.common

import android.graphics.Color
import android.graphics.ColorFilter
import android.graphics.Paint
import androidx.annotation.ColorInt

object Styles {
    lateinit var keyboardStyle: KeyboardStyle
    lateinit var keyStyle: KeyStyle
}

data class KeyboardStyle(
    @ColorInt val keyboardBackground: Int
)

data class KeyStyle(
    @ColorInt val normalBackgroundColor: Int,
    @ColorInt val pressedBackgroundColor: Int,
    @ColorInt val shadowColor: Int,
    @ColorInt val labelColor: Int,
    @ColorInt val subLabelColor: Int,
    val iconFilter: ColorFilter,
    val cornerRadius: Float = 15F,
    val widthPaddingRatio: Float = 0.07F,
    var widthPadding: Float = 0F,
    val heightPaddingRatio: Float = 0.07F,
    var heightPadding: Float = 0F,
    val shadowHeightRatio: Float = 0.07F,
    var shadowHeight: Float = 0F
) {
    val labelPaint = Paint().apply {
        color = labelColor
        isAntiAlias = true
        textAlign = Paint.Align.CENTER
    }

    val subLabelPaint = Paint().apply {
        color = subLabelColor
        isAntiAlias = true
    }

    val shadowPaint = Paint().apply {
        color = shadowColor
    }

    val backgroundPaint = Paint().apply {
        color = normalBackgroundColor
    }

    val arrowPaint = Paint().apply {
        color = Color.LTGRAY
    }
}