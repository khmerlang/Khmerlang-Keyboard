package com.rathanak.khmerroman

import android.os.Bundle
import android.view.MenuItem
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import io.realm.Realm
import io.realm.RealmObject

class RomanMapping : AppCompatActivity(), RomanItemAdapter.OnClickListener {
    private lateinit var realm: Realm

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        realm = Realm.getDefaultInstance()
        setContentView(R.layout.activity_roman_mapping)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

//        //load data from realm database
//        open class RomanDB(roman: String?= null, khmer: String?= null) :RealmObject() {}
//
//        realm.executeTransactionAsync({
//            it.createObject(RomanDB::class.java)
//        })
//        val romanItemData = realm.where(RomanDB::class.java).findAll()
        val romanItemList = ArrayList<RomanItem>()
//        romanItemData.forEach {
//            romanItemList.add(RomanItem("name", "nameKH"))
//        }

        for (i in 0..50){
            val name = "Roman $i"
            val nameKH = "Khmer $i"
            val contact = RomanItem(name, nameKH)
            romanItemList.add(contact)
        }

        var rvRomanList = findViewById<RecyclerView>(R.id.rvRomanList)
        rvRomanList.layoutManager = LinearLayoutManager(this)
        rvRomanList.adapter = RomanItemAdapter(romanItemList, this)
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
