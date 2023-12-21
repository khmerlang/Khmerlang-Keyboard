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

class SpellSuggestionAdapter(private val context: Context, private val suggestionsList: java.util.ArrayList<SpellSuggestionItem>) : BaseAdapter() {
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
        serialNum.text = suggestionsList[position].typoWord

        val btnSpellItemClose = convertView.findViewById(R.id.btnSpellItemClose) as ImageButton
        btnSpellItemClose.setOnClickListener {
            suggestionsList.removeAt(position); // remove the item from the data list
            notifyDataSetChanged();
        }

        val wordsSuggestionList = convertView.findViewById(R.id.wordsSuggestionList) as FlexboxLayout
        val params = LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.WRAP_CONTENT,
            LinearLayout.LayoutParams.WRAP_CONTENT
        )
        params.setMargins(4,4,4,4)
        suggestionsList[position].wordsSuggestion.forEach {
            val btnWord = Button(ContextThemeWrapper(context, R.style.ButtonSpellSuggestion), null, R.style.ButtonSpellSuggestion)
            btnWord.minHeight = 0
            btnWord.minWidth = 0
            btnWord.text = it.word
            btnWord.setOnClickListener {
                suggestionsList.removeAt(position); // remove the item from the data list
                notifyDataSetChanged();
            }

            wordsSuggestionList.addView(btnWord, params)
        }

        return convertView
    }
}
