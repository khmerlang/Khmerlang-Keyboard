package com.rathanak.khmerroman.adapter

import android.content.Context
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
import com.rathanak.khmerroman.keyboard.R2KhmerService
import com.rathanak.khmerroman.view.KhmerLangApp
import io.realm.Case
import io.realm.Realm
import io.realm.RealmResults
import io.realm.Sort
import kotlinx.android.synthetic.main.roman_item.view.*
import kotlinx.coroutines.*

class RomanItemAdapter(var isCustom: Boolean, private val appContext: Context): RecyclerView.Adapter<RomanItemAdapter.ContactViewHolder>(), Filterable {
    private var realm: Realm = Realm.getInstance(KhmerLangApp.dbConfig)
    var romanItemsList: RealmResults<Ngram>
    init {
        romanItemsList = buildQuery()
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
        holder.view.txtRoman.text = item?.other
        holder.view.txtKhmer.text = item?.keyword
        holder.view.btnDelete.setOnClickListener {
            Toast.makeText(appContext,item?.keyword + ":" + item?.other + " deleted", Toast.LENGTH_LONG).show()

                var result = realm.where(Ngram::class.java)
                    .equalTo("id", item?.id)
                    .equalTo("custom", true)
                    .findFirst()
            if (result != null) {
                realm.beginTransaction()
                    result.deleteFromRealm()
                realm.commitTransaction()
                notifyItemRemoved(position)
                updateSpellCorrectModel()
            }
        }

    }

    override fun getFilter(): Filter {
        return object : Filter() {
            override fun performFiltering(p0: CharSequence?): FilterResults {
                return FilterResults()
            }

            override fun publishResults(newText: CharSequence?, results: FilterResults?) {
                if (newText == null || newText.isEmpty()) {
                    romanItemsList = buildQuery()
                } else {
                    romanItemsList = realm.where(Ngram::class.java)
                        .beginGroup()
                            .like("keyword", "$newText*", Case.INSENSITIVE)
                            .or()
                            .like("other", "*$newText*", Case.INSENSITIVE)
                        .endGroup()
                        .and()
                        .equalTo("custom", isCustom)
                        .equalTo("gram", KhmerLangApp.ONE_GRAM)
                        .equalTo("lang", KhmerLangApp.LANG_KH)
                        .notEqualTo("keyword", "<s>")
                        .notEqualTo("keyword", "<s> <s>")
                        .notEqualTo("keyword", "<s> <s> <s>")
                        .findAll()

                    romanItemsList = if (isCustom) {
                        romanItemsList.sort("id", Sort.DESCENDING)
                    } else {
                        romanItemsList.sort("count", Sort.DESCENDING)
                    }
                }

                notifyDataSetChanged()
            }
        }
    }

    private fun buildQuery() :RealmResults<Ngram> {
        var query = realm.where(Ngram::class.java)
            .equalTo("custom", isCustom)
            .equalTo("gram", KhmerLangApp.ONE_GRAM)
            .equalTo("lang", KhmerLangApp.LANG_KH)
            .notEqualTo("keyword", "<s>")
            .notEqualTo("keyword", "<s> <s>")
            .notEqualTo("keyword", "<s> <s> <s>")
            .findAll()

        query = if (isCustom) {
            query.sort("id", Sort.DESCENDING)
        } else {
            query.sort("count", Sort.DESCENDING)
        }

        return query
    }

    private fun updateSpellCorrectModel() {
        GlobalScope.launch(Dispatchers.Main) {
            reloadLoadSpelling()
        }
    }

    private suspend fun reloadLoadSpelling() {
        coroutineScope {
            async(Dispatchers.IO) {
                var realm: Realm = Realm.getInstance(KhmerLangApp.dbConfig)
                try {
                    R2KhmerService.spellingCorrector.reset()
                    R2KhmerService.spellingCorrector.loadData(realm)
                } finally {
                    realm.close()
                }
            }
        }
    }
}