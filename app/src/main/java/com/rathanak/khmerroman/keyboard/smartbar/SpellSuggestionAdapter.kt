package com.rathanak.khmerroman.keyboard.smartbar

import android.content.Context
import android.view.ContextThemeWrapper
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.Button
import android.widget.ImageButton
import android.widget.LinearLayout
import android.widget.ListView
import android.widget.TextView
import androidx.core.view.marginBottom
import androidx.core.view.marginEnd
import androidx.recyclerview.widget.RecyclerView
import com.google.android.flexbox.FlexboxLayout
import com.rathanak.khmerroman.R
import com.rathanak.khmerroman.keyboard.R2KhmerService
import com.rathanak.khmerroman.request.SpellCheckResultDTO

class SpellSuggestionAdapter(private val smartSpell: SpellSuggestionManager, private val context: Context, var suggestionsList: java.util.ArrayList<SpellCheckResultDTO>) : BaseAdapter() {
    override fun getCount(): Int {
        return suggestionsList.size
    }

    override fun getItem(position: Int): Any {
        return suggestionsList[position]
    }

    override fun getItemId(position: Int): Long {
        return position.toLong()
    }

    override fun getView(position: Int, convertView: View?, parent: ViewGroup?): View {
        var convertView = convertView
        convertView = LayoutInflater.from(context).inflate(R.layout.spell_suggestion_item, parent, false)
        val serialNum = convertView.findViewById(R.id.typoText) as TextView
        val typoWord = suggestionsList[position].word
        serialNum.text = typoWord

        val btnSpellItemClose = convertView.findViewById(R.id.btnSpellItemClose) as ImageButton
        btnSpellItemClose.setOnClickListener {
            suggestionsList.removeAt(position); // remove the item from the data list
            handleAfterRemoveItem()
            notifyDataSetChanged();
        }

        val wordsSuggestionList = convertView.findViewById(R.id.wordsSuggestionList) as FlexboxLayout
        val params = LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.WRAP_CONTENT,
            LinearLayout.LayoutParams.WRAP_CONTENT
        )
        params.setMargins(4,4,4,4)
        val startIndex = suggestionsList[position].startIndex
        val endIndex = suggestionsList[position].endIndex
        suggestionsList[position].suggestions.forEach {
            val btnWord = Button(ContextThemeWrapper(context, R.style.ButtonSpellSuggestion), null, R.style.ButtonSpellSuggestion)
            btnWord.minHeight = 0
            btnWord.minWidth = 0
            val word = it
            btnWord.text = word
            btnWord.setOnClickListener {
                smartSpell.setCurrentText(typoWord, word, startIndex, endIndex + 1)
                suggestionsList.removeAt(position)
                if(typoWord.length != word.length) {
                    updateNextListStartEndPos(position, word.length - typoWord.length)
                }

                handleAfterRemoveItem()
                notifyDataSetChanged();
            }

            wordsSuggestionList.addView(btnWord, params)
        }

        return convertView
    }

    private fun handleAfterRemoveItem() {
        if(suggestionsList.isNullOrEmpty()) {
            smartSpell.onCallEmptyResult()
        }
    }

    private fun updateNextListStartEndPos(position: Int, updateLength: Int) {
        for (index in position until suggestionsList.size) {
            suggestionsList[index].startIndex += updateLength
            suggestionsList[index].endIndex += updateLength
        }
    }
}
