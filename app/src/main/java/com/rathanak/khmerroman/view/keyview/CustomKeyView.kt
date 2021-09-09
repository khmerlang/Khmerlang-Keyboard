package com.rathanak.khmerroman.view.keyview

import android.content.Context
import android.util.AttributeSet
import android.view.Gravity
import android.widget.FrameLayout
import android.widget.LinearLayout
import com.rathanak.khmerroman.keyboard.keyboardinflater.CustomKey

class CustomKeyView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0,
    val isLandscape: Boolean = false,
    var key: CustomKey? = null
) : FrameLayout(context, attrs, defStyleAttr) {

    val repeatable: Boolean? = key?.repeatable
    val codes: IntArray? = key?.codes
    val label: String? = key?.label?.toString()
    val isChangeLanguage: Boolean? = key?.isChangeLanguageKey

    private lateinit var keyTextView: KeyView

    init {
        isClickable = true
        key?.let { key ->
            keyTextView = KeyView(
                context,
                key = key,
                isLandscape = isLandscape
            )

            // If it is the edge key expand the width to fill.
            layoutParams = if (key.isEdge()) {
                LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.MATCH_PARENT, 1.0F)
            } else {
                LinearLayout.LayoutParams(key.width, key.height)
            }
            // Tell the parent view how this to be laid out.
            val childLayoutParams = FrameLayout.LayoutParams(key.width, key.height)
            when {
                key.isLeftEdge() -> childLayoutParams.gravity = Gravity.END or Gravity.CENTER_VERTICAL
                key.isRightEdge() -> childLayoutParams.gravity = Gravity.START or Gravity.CENTER_VERTICAL
                else -> childLayoutParams.gravity = Gravity.CENTER
            }
            keyTextView.layoutParams = childLayoutParams
            addView(keyTextView)
        }
    }

    fun updateLabel(newLabel: String) {
        keyTextView.label = newLabel
    }

    fun updateColor() {

    }

}

class CustomKeyPreview @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : FrameLayout(
    context,
    attrs,
    defStyleAttr
) {
    var x = 0
    var y = 0
}