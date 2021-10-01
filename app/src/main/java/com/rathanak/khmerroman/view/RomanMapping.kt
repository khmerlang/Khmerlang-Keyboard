package com.rathanak.khmerroman.view

import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.view.inputmethod.EditorInfo
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.SearchView
import androidx.recyclerview.widget.LinearLayoutManager
import com.bumptech.glide.Glide
import com.rathanak.khmerroman.R
import com.rathanak.khmerroman.adapter.DebouncingQueryTextListener
import com.rathanak.khmerroman.adapter.RomanItemAdapter
import com.rathanak.khmerroman.data.KeyboardPreferences
import com.rathanak.khmerroman.keyboard.R2KhmerService
import com.rathanak.khmerroman.serializable.NgramRecordSerializable
import com.rathanak.khmerroman.utils.DownloadData
import kotlinx.android.synthetic.main.activity_roman_mapping.*
import kotlinx.android.synthetic.main.smartbar.view.*
import kotlinx.coroutines.*
import java.io.FileInputStream
import java.io.ObjectInputStream

class RomanMapping : AppCompatActivity() {
    private var rAdapter: RomanItemAdapter? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_roman_mapping)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        rvRomanList.layoutManager = LinearLayoutManager(this)
        rAdapter = RomanItemAdapter(false, applicationContext)
        rvRomanList.adapter = rAdapter

        btnDownloadData.setOnClickListener {
            it.visibility = View.GONE
            downloadingData.visibility = View.VISIBLE
            R2KhmerService.downloadDataPrevStatus = R2KhmerService.downloadDataStatus
            R2KhmerService.downloadDataStatus = KeyboardPreferences.STATUS_DOWNLOADING
            val download = DownloadData()
            download.downloadKeyboardData()
            listenJobDone()
        }

        updateVisibility()
        listenJobDone()
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
        searchView.setQueryHint(getString(R.string.search))
        searchView.setImeOptions(EditorInfo.IME_ACTION_DONE)
        searchView.setOnQueryTextListener(
            DebouncingQueryTextListener(
                this@RomanMapping.lifecycle
            ) { newText ->
                newText?.let {
                    if (it.isEmpty()) {
                        rAdapter?.getFilter()?.filter("")
                    } else {
                        rAdapter?.getFilter()?.filter(it)
                    }
                }
            }
        )

        return true
    }

    private fun listenJobDone() {
        if (R2KhmerService.jobLoadData != null) {
            R2KhmerService.jobLoadData!!.invokeOnCompletion {
                updateVisibility()
                rvRomanList.adapter?.notifyDataSetChanged()
                if (R2KhmerService.downloadDataStatus == KeyboardPreferences.STATUS_DOWNLOAD_FAIL) {
                    R2KhmerService.downloadDataStatus = R2KhmerService.downloadDataPrevStatus
                    KhmerLangApp.preferences?.putInt(KeyboardPreferences.KEY_DATA_STATUS, R2KhmerService.downloadDataPrevStatus)
                }
            }
        }
    }

    private fun updateVisibility() {
        btnDownloadData!!.visibility = View.GONE
        downloadingData!!.visibility = View.GONE
        rvRomanList!!.visibility = View.GONE
        if (R2KhmerService.downloadDataStatus == KeyboardPreferences.STATUS_NONE) {
            Glide.with(applicationContext)
                .load(R.drawable.banner_download_data)
                .into(btnDownloadData);
            btnDownloadData.visibility = View.VISIBLE
        } else if (R2KhmerService.downloadDataStatus == KeyboardPreferences.STATUS_DOWNLOAD_FAIL) {
            Glide.with(applicationContext)
                .load(R.drawable.banner_download_data_fail)
                .into(btnDownloadData);
            btnDownloadData.visibility = View.VISIBLE
        } else if (R2KhmerService.downloadDataStatus == KeyboardPreferences.STATUS_DOWNLOADING) {
            downloadingData!!.visibility = View.VISIBLE
        } else {
            rvRomanList!!.visibility = View.VISIBLE
        }
    }
}
