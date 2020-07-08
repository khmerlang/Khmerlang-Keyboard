package com.rathanak.khmerroman.data

import android.content.Context
import android.util.Log
import com.rathanak.khmerroman.view.Roman2KhmerApp
import io.realm.Realm
import io.realm.RealmResults
import java.util.*


class DataLoader(val context: Context ) {
    private var realm: Realm = Realm.getInstance(Roman2KhmerApp.dbConfig)

    data class RomanData(val r: String, val k: String) {}
    fun reInitRomanData() {
        // clear all data
        // load default data
        clearData()
        loadRoman2DB()
    }

    fun clearData() {
        val results: RealmResults<RomanItem> = realm.where<RomanItem>(RomanItem::class.java).findAll()
        realm.beginTransaction()
        results.deleteAllFromRealm()
        realm.commitTransaction()
    }

    fun loadRoman2DB() {
        try {
            realm.beginTransaction()
            context.assets.open(Roman2KhmerApp.khmerWordsFile).bufferedReader().useLines { lines -> lines.forEach {
                val word = it.split("\\s".toRegex())//split(",")
                val kh = word[0].trim()
                val freq = word[1].toInt()
                val rm = if (word.size == 3) {
                    word[2]
                } else {
                    ""
                }
                val romanKhmer: RomanItem = realm.createObject(RomanItem::class.java, UUID.randomUUID().toString())
                romanKhmer.khmer = kh
                romanKhmer.roman = rm
                romanKhmer.freq = freq
                realm.insert(romanKhmer)
            }
            }
            realm.commitTransaction()
        } catch (ex:Exception){
            Log.e("read_file", ex.localizedMessage)
        }
    }
}