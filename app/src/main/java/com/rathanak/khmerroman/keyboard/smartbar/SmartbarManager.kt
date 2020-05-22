package com.rathanak.khmerroman.keyboard.smartbar

import android.view.View
import android.view.textservice.SentenceSuggestionsInfo
import android.view.textservice.SpellCheckerSession
import android.view.textservice.SuggestionsInfo
import android.widget.LinearLayout
import com.rathanak.khmerroman.R
import com.rathanak.khmerroman.keyboard.R2KhmerService

class SmartbarManager(
    private val r_2_khmer: R2KhmerService
) : SpellCheckerSession.SpellCheckerSessionListener {
    private var smartbarView: LinearLayout? = null
    
    override fun onGetSentenceSuggestions(p0: Array<out SentenceSuggestionsInfo>?) {
    }

    override fun onGetSuggestions(p0: Array<out SuggestionsInfo>?) {
    }

    fun createSmartbarView(): LinearLayout {
        val smartbarView = View.inflate(r_2_khmer.context, R.layout.smartbar, null) as LinearLayout
        this.smartbarView = smartbarView

        return smartbarView
    }
}