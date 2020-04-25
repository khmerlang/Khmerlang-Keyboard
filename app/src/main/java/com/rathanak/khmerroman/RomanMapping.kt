package com.rathanak.khmerroman

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.Menu
import android.view.MenuItem
import android.view.inputmethod.EditorInfo
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.SearchView
import androidx.core.widget.addTextChangedListener
import androidx.recyclerview.widget.LinearLayoutManager
import com.rathanak.khmerroman.adapter.RomanItemAdapter
import com.rathanak.khmerroman.data.RomanItem
import io.realm.Realm
import kotlinx.android.synthetic.main.activity_roman_mapping.*

class RomanMapping : AppCompatActivity() {
    private lateinit var realm: Realm
    private var rAdapter: RomanItemAdapter? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        realm = Realm.getDefaultInstance()
        setContentView(R.layout.activity_roman_mapping)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        rvRomanList.layoutManager = LinearLayoutManager(this)
        rAdapter = RomanItemAdapter()
        rvRomanList.adapter = rAdapter

//        txtSearch.addTextChangedListener {object : TextWatcher {
//            override fun afterTextChanged(p0: Editable?) {
//            }
//
//            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
//            }
//
//            override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
//                rAdapter.filter.filter(newText)
//            }
//        }
//        }
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            android.R.id.home -> {
                finish()
                return true
            }
        }

        return super.onOptionsItemSelected(item)
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        val inflater = menuInflater
        inflater.inflate(R.menu.search_menu, menu)
        val searchItem = menu!!.findItem(R.id.action_search)
        val searchView: SearchView = searchItem.actionView as SearchView
        searchView.setImeOptions(EditorInfo.IME_ACTION_DONE)
        searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(p0: String?): Boolean {
                return false
            }

            override fun onQueryTextChange(newText: String?): Boolean {
                rAdapter?.getFilter()?.filter(newText)
                return false
            }
        })

        return true
    }
}
