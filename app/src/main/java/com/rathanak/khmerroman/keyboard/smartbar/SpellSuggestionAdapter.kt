package com.rathanak.khmerroman.keyboard.smartbar

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.Button
import android.widget.ImageButton
import android.widget.ListView
import android.widget.TextView
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
        suggestionsList[position].wordsSuggestion.forEach {
            val btnWord = Button(context)
            btnWord.text = it.word
            //TODO: set style

            btnWord.setOnClickListener {
                suggestionsList.removeAt(position); // remove the item from the data list
                notifyDataSetChanged();
            }

            wordsSuggestionList.addView(btnWord)
        }

        return convertView
    }
}
