package com.rathanak.khmerroman

import android.os.Bundle
import android.view.MenuItem
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import io.realm.Realm
import com.rathanak.khmerroman.data.RomanItem
import com.rathanak.khmerroman.adapter.RomanItemAdapter

class RomanMapping : AppCompatActivity() {
    private lateinit var realm: Realm

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        realm = Realm.getDefaultInstance()
        setContentView(R.layout.activity_roman_mapping)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        val romanItemData = realm.where(RomanItem::class.java).findAll()
        var rvRomanList = findViewById<RecyclerView>(R.id.rvRomanList)
        rvRomanList.layoutManager = LinearLayoutManager(this)
        rvRomanList.adapter = RomanItemAdapter(romanItemData, this)
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
}
