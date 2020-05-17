package com.rathanak.khmerroman.adapter

import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Filter
import android.widget.Filterable
import androidx.recyclerview.widget.RecyclerView
import com.rathanak.khmerroman.R
import com.rathanak.khmerroman.RomanMapping
import com.rathanak.khmerroman.data.RomanItem
import io.realm.Case
import io.realm.Realm
import io.realm.RealmResults
import kotlinx.android.synthetic.main.roman_item.view.*

class RomanItemAdapter(var custom: Boolean): RecyclerView.Adapter<RomanItemAdapter.ContactViewHolder>(), Filterable {
    private var realm: Realm
    private var isCustom: Boolean
    var romanItemsList: RealmResults<RomanItem>
    init {
        realm = Realm.getDefaultInstance()
        isCustom = custom
        romanItemsList = realm.where(RomanItem::class.java)
            .equalTo("custom", isCustom).findAll()
        Log.i("data", romanItemsList.asJSON())
    }
    class ContactViewHolder (val view : View) : RecyclerView.ViewHolder(view)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ContactViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.roman_item, parent, false)
        return ContactViewHolder(view)
    }

    override fun getItemCount(): Int {
        return  romanItemsList.size
    }

    override fun onBindViewHolder(holder: ContactViewHolder, position: Int) {
        val item = romanItemsList[position]
        holder.view.txtRoman.text = item?.roman
        holder.view.txtKhmer.text = item?.khmer
    }

    override fun getFilter(): Filter {
        return object : Filter() {
            override fun performFiltering(p0: CharSequence?): FilterResults {
                return FilterResults()
            }

            override fun publishResults(newText: CharSequence?, results: FilterResults?) {
                romanItemsList = if (newText == null || newText.isEmpty()) {
                    realm.where(RomanItem::class.java)
                        .equalTo("custom", isCustom).findAll()
                } else {
                    realm.where(RomanItem::class.java)
                        .like("khmer", "$newText*", Case.INSENSITIVE)
                        .or()
                        .like("roman", "$newText*", Case.INSENSITIVE)
                        .and()
                        .equalTo("custom", isCustom)
                        .findAll()
                }

                notifyDataSetChanged()
            }
        }
    }
}