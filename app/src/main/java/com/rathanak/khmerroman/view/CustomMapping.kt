package com.rathanak.khmerroman.view

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.view.inputmethod.EditorInfo
import androidx.appcompat.widget.SearchView
import androidx.recyclerview.widget.LinearLayoutManager
import com.rathanak.khmerroman.R
import com.rathanak.khmerroman.adapter.DebouncingQueryTextListener
import com.rathanak.khmerroman.adapter.RomanItemAdapter
import com.rathanak.khmerroman.databinding.ActivityCustomMappingBinding
import com.rathanak.khmerroman.view.dialog.RomanDialog

class CustomMapping : AppCompatActivity() {
    private lateinit var binding: ActivityCustomMappingBinding
    private var rAdapter: RomanItemAdapter? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityCustomMappingBinding.inflate(layoutInflater)
        setContentView(binding.root)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        binding.rvRomanList.layoutManager = LinearLayoutManager(this)
        rAdapter = RomanItemAdapter(true, applicationContext)
        binding.rvRomanList.adapter = rAdapter

//        btnUploadData.setOnClickListener {
//            btnUploadData.visibility = View.GONE
//            uploadingData.visibility = View.VISIBLE
//        }
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            android.R.id.home -> {
                finish()
                return true
            }

            R.id.action_add -> {
                showRomanDialog()
                return true
            }
        }

        return super.onOptionsItemSelected(item)
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        val inflater = menuInflater
        inflater.inflate(R.menu.search_add_menu, menu)
        val searchItem = menu!!.findItem(R.id.action_search)
        val searchView: SearchView = searchItem.actionView as SearchView
        searchView.queryHint = getString(R.string.search)
        searchView.imeOptions = EditorInfo.IME_ACTION_DONE
        searchView.setOnQueryTextListener(
            DebouncingQueryTextListener(
                this@CustomMapping.lifecycle
            ) { newText ->
                newText?.let {
                    if (it.isEmpty()) {
                        rAdapter?.filter?.filter("")
                    } else {
                        rAdapter?.filter?.filter(it)
                    }
                }
            }
        )

        return true
    }

    fun showRomanDialog() {
        // Create an instance of the dialog fragment and show it
        val dialog =
            RomanDialog(
                "",
                "",
                1,
                applicationContext
            )
        dialog.show(supportFragmentManager, "RomanDialog")
    }
}
