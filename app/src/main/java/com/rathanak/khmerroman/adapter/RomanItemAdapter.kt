package com.rathanak.khmerroman.adapter

import android.content.Context
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.View.GONE
import android.view.ViewGroup
import android.widget.Filter
import android.widget.Filterable
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView
import com.rathanak.khmerroman.R
import com.rathanak.khmerroman.data.Ngram
import com.rathanak.khmerroman.view.Roman2KhmerApp
import io.realm.Case
import io.realm.Realm
import io.realm.RealmResults
import kotlinx.android.synthetic.main.roman_item.view.*

class RomanItemAdapter(var isCustom: Boolean, private val appContext: Context): RecyclerView.Adapter<RomanItemAdapter.ContactViewHolder>(), Filterable {
    private var realm: Realm = Realm.getInstance(Roman2KhmerApp.dbConfig)
    var romanItemsList: RealmResults<Ngram>
    init {
        romanItemsList = realm.where(Ngram::class.java)
            .equalTo("is_custom", isCustom)
            .equalTo("gram", Roman2KhmerApp.ONE_GRAM)
            .equalTo("lang", Roman2KhmerApp.LANG_KH)
            .findAll()
            .sort("keyword")
    }
    class ContactViewHolder (val view : View) : RecyclerView.ViewHolder(view)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ContactViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.roman_item, parent, false)
        if (!isCustom) {
            view.btnDelete.visibility = GONE
        }

        return ContactViewHolder(view)
    }

    override fun getItemCount(): Int {
        return  romanItemsList.size
    }

    override fun onBindViewHolder(holder: ContactViewHolder, position: Int) {
        val item = romanItemsList[position]
        holder.view.txtRoman.text = item?.roman
        holder.view.txtKhmer.text = item?.keyword
        holder.view.btnDelete.setOnClickListener {
            Toast.makeText(appContext,item?.keyword + ":" + item?.roman + " deleted", Toast.LENGTH_LONG).show()
            realm.beginTransaction()
                var result = realm.where(Ngram::class.java)
                    .equalTo("id", item?.id)
                    .equalTo("is_custom", true)
                    .findAll()
                result.deleteAllFromRealm()
            realm.commitTransaction()
//            notifyItemRemoved(position)
            notifyDataSetChanged()
        }

    }

    override fun getFilter(): Filter {
        return object : Filter() {
            override fun performFiltering(p0: CharSequence?): FilterResults {
                return FilterResults()
            }

            override fun publishResults(newText: CharSequence?, results: FilterResults?) {
                romanItemsList = if (newText == null || newText.isEmpty()) {
                    realm.where(Ngram::class.java)
                        .equalTo("is_custom", isCustom)
                        .equalTo("gram", Roman2KhmerApp.ONE_GRAM)
                        .equalTo("lang", Roman2KhmerApp.LANG_KH)
                        .findAll()
                        .sort("keyword")
                } else {
                    realm.where(Ngram::class.java)
                        .beginGroup()
                            .like("keyword", "$newText*", Case.INSENSITIVE)
                            .or()
                            .like("roman", "$newText*", Case.INSENSITIVE)
                        .endGroup()
                        .and()
                        .equalTo("is_custom", isCustom)
                        .equalTo("gram", Roman2KhmerApp.ONE_GRAM)
                        .equalTo("lang", Roman2KhmerApp.LANG_KH)
                        .findAll()
                        .sort("keyword")
                }

                notifyDataSetChanged()
            }
        }
    }
}