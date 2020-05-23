package com.rathanak.khmerroman.keyboard.smartbar

import android.view.View
import android.view.textservice.SentenceSuggestionsInfo
import android.view.textservice.SpellCheckerSession
import android.view.textservice.SuggestionsInfo
import android.widget.CompoundButton
import android.widget.LinearLayout
import android.widget.ToggleButton
import com.rathanak.khmerroman.R
import com.rathanak.khmerroman.keyboard.R2KhmerService
import kotlinx.android.synthetic.main.smartbar.view.*

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
        this.smartbarView!!.toggleOption!!.setOnCheckedChangeListener { buttonView, isChecked ->
            if (isChecked) {
                buttonView.setBackgroundResource(R.drawable.ic_mini_logo)
            } else {
                buttonView.setBackgroundResource(R.drawable.ic_chevron_right)
            }
        }

        return smartbarView
    }
}