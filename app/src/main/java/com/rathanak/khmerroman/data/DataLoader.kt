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

        // TODO merge and download from server
        loadSpellCheckKH()
        loadSpellCheckEN()
    }

    fun clearData() {
        val results: RealmResults<RomanItem> = realm.where<RomanItem>(RomanItem::class.java).findAll()
        realm.beginTransaction()
        results.deleteAllFromRealm()
        realm.commitTransaction()
    }

    private fun loadSpellCheckKH() {
        try {
            realm.beginTransaction()
            context.assets.open(Roman2KhmerApp.khmerWordsFile).bufferedReader().useLines { lines -> lines.forEach {
                val word = it.split("\\s".toRegex())//split(",")
                val keyword = word[0].trim()
                val count = word[1].toInt()
                val gram = 1
                val roman = if (word.size == 3) {
                    word[2]
                } else {
                    ""
                }

                if(keyword.isNotEmpty()) {
                    val ngramData: Ngram = realm.createObject(Ngram::class.java, UUID.randomUUID().toString())
                    ngramData.keyword = keyword
                    ngramData.roman = roman
                    ngramData.lang = Roman2KhmerApp.LANG_KH
                    ngramData.gram = gram
                    ngramData.count = count
                    ngramData.is_custom = false
                    realm.insert(ngramData)
                }
            }
            }
            realm.commitTransaction()
        } catch (ex:Exception){
            Log.e("read_file", ex.localizedMessage)
        }
    }

    private fun loadSpellCheckEN() {
        try {
            realm.beginTransaction()
            context.assets.open(Roman2KhmerApp.englishWordsFile).bufferedReader().useLines { lines -> lines.forEach {
                val word = it.split("\\s".toRegex())//split(",")
                val keyword = word[0].trim()
                val count = word[1].toInt()
                val gram = 1
                val roman = ""

                if(keyword.isNotEmpty()) {
                    val ngramData: Ngram = realm.createObject(Ngram::class.java, UUID.randomUUID().toString())
                    ngramData.keyword = keyword
                    ngramData.roman = roman
                    ngramData.lang = Roman2KhmerApp.LANG_EN
                    ngramData.gram = gram
                    ngramData.count = count
                    ngramData.is_custom = false
                    realm.insert(ngramData)
                }
            }
            }
            realm.commitTransaction()
        } catch (ex:Exception){
            Log.e("read_file", ex.localizedMessage)
        }
    }

    private fun loadRoman2DB() {
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