package com.rathanak.khmerroman

import android.os.Bundle
import android.view.MenuItem
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView

class RomanMapping : AppCompatActivity(), RomanItemAdapter.OnClickListener {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_roman_mapping)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        val romanItemList = ArrayList<RomanItem>()
        for (i in 0..50){
            val name = "Roman $i"
            val nameKH = "Khmer $i"
            val contact = RomanItem(name, nameKH)
            romanItemList.add(contact)
        }

        var rvContact = findViewById<RecyclerView>(R.id.rvRomanList)
        rvContact.layoutManager = LinearLayoutManager(this)
        rvContact.adapter = RomanItemAdapter(romanItemList, this)
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

    override fun onRowClick(item: RomanItem) {
        Toast.makeText(this, item.roman, Toast.LENGTH_SHORT).show()
    }
}
