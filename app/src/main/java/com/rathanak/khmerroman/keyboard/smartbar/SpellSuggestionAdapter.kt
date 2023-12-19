package com.rathanak.khmerroman.keyboard.smartbar

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import com.rathanak.khmerroman.R

class SpellSuggestionAdapter(private val context: Context, private val arrayList: java.util.ArrayList<SpellSuggestionItem>) : BaseAdapter() {
    override fun getCount(): Int {
        return arrayList.size
    }

    override fun getItem(position: Int): Any {
        return arrayList[position]
    }

    override fun getItemId(position: Int): Long {
        return position.toLong()
    }

    override fun getView(position: Int, convertView: View?, parent: ViewGroup?): View {
        var convertView = convertView
        convertView = LayoutInflater.from(context).inflate(R.layout.spell_suggestion_item, parent, false)
//        serialNum = convertView.findViewById(R.id.serialNumber)
//        name = convertView.findViewById(R.id.studentName)
//        contactNum = convertView.findViewById(R.id.mobileNum)
//        serialNum.text = " " + arrayList[position].num
//        name.text = arrayList[position].name
//        contactNum.text = arrayList[position].mobileNumber
        return convertView
    }
}
