package com.rathanak.khmerroman

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import kotlinx.android.synthetic.main.roman_item.view.*

class RomanItemAdapter(private val romanItemsList: ArrayList<RomanItem>,
                       private val listener: RomanMapping
): RecyclerView.Adapter<RomanItemAdapter.ContactViewHolder>() {
    class ContactViewHolder (val view : View) : RecyclerView.ViewHolder(view)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ContactViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.roman_item, parent, false)
        return ContactViewHolder(view)
    }

    override fun getItemCount(): Int {
        return  romanItemsList.size
    }

    override fun onBindViewHolder(holder: ContactViewHolder, position: Int) {
        val contact = romanItemsList[position]
        holder.view.txtRoman.text = contact.roman
        holder.view.txtKhmer.text = contact.khmer
        holder.view.setOnClickListener {
            listener.onRowClick(contact)
        }
    }
    interface OnClickListener {
        fun onRowClick(item : RomanItem)
    }
}