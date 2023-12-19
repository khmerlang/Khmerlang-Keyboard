package com.rathanak.khmerroman.keyboard.smartbar

import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.LinearLayout
import androidx.core.graphics.drawable.DrawableCompat
import androidx.core.view.children
import com.rathanak.khmerroman.R
import com.rathanak.khmerroman.keyboard.R2KhmerService
import com.rathanak.khmerroman.keyboard.common.Styles
import kotlinx.android.synthetic.main.smartbar.view.btnOpenApp
import kotlinx.android.synthetic.main.smartbar.view.numbersList
import kotlinx.android.synthetic.main.smartbar.view.smartbar
import kotlinx.android.synthetic.main.spell_suggestion.view.spellSuggestionList

class SpellSuggestionManager(private val r_2_khmer: R2KhmerService) {
    var spellSuggestionView: LinearLayout? = null
    private var isDarkMood: Boolean = false
    fun createSpellSuggestionView(): LinearLayout {
        var spellSuggestionView = View.inflate(r_2_khmer.context, R.layout.spell_suggestion, null) as LinearLayout
        this.spellSuggestionView = spellSuggestionView

        val listView = spellSuggestionView.spellSuggestionList

        var arrayList: ArrayList<SpellSuggestionItem> = ArrayList()
        arrayList.add(SpellSuggestionItem(" Mashu", "987576443"))
        arrayList.add(SpellSuggestionItem(" Azhar", "8787576768"))
        arrayList.add(SpellSuggestionItem( " Niyaz", "65757657657"))
        arrayList.add(SpellSuggestionItem( " Niyaz", "65757657657"))
        arrayList.add(SpellSuggestionItem( " Niyaz", "65757657657"))
        arrayList.add(SpellSuggestionItem( " Niyaz", "65757657657"))
        var adapter = SpellSuggestionAdapter(r_2_khmer.context, arrayList)
        listView.adapter = adapter

        return spellSuggestionView
    }

    fun setDarkMood(darkMood: Boolean) {
        isDarkMood = darkMood
        if (this.spellSuggestionView != null) {
            updateByMood()
        }
    }

    private fun updateByMood() {
        this.spellSuggestionView!!.smartbar.setBackgroundColor(Styles.keyboardStyle.keyboardBackground)
        DrawableCompat.setTint(this.spellSuggestionView!!.smartbar.btnOpenApp.background, Styles.keyStyle.labelColor)
//        for (numberButton in this.spellSuggestionView!!.numbersList.children) {
//            if (numberButton is Button) {
//                DrawableCompat.setTint(numberButton.background, Styles.keyStyle.normalBackgroundColor)
//                numberButton.setTextColor(Styles.keyStyle.labelColor)
//            }
//        }
    }
    fun destroy() {
    }
}