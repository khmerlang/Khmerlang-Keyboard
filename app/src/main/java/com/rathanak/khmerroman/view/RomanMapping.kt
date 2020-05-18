package com.rathanak.khmerroman.view

import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.view.inputmethod.EditorInfo
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.SearchView
import androidx.recyclerview.widget.LinearLayoutManager
import com.rathanak.khmerroman.R
import com.rathanak.khmerroman.adapter.RomanItemAdapter
import kotlinx.android.synthetic.main.activity_roman_mapping.*

class RomanMapping : AppCompatActivity() {
    private var rAdapter: RomanItemAdapter? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_roman_mapping)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        rvRomanList.layoutManager = LinearLayoutManager(this)
        rAdapter = RomanItemAdapter(false, applicationContext)
        rvRomanList.adapter = rAdapter
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
