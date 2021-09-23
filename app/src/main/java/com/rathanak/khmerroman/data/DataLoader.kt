package com.rathanak.khmerroman.data

import android.content.Context
import android.util.Log
import com.rathanak.khmerroman.serializable.NgramRecordSerializable
import com.rathanak.khmerroman.view.KhmerLangApp
import io.realm.Realm
import io.realm.RealmResults


class DataLoader(val context: Context ) {
    private var realm: Realm = Realm.getInstance(KhmerLangApp.dbConfig)

    private fun clearDBData(removeCustom: Boolean = false) {
        realm.beginTransaction()
        if (removeCustom) {
            realm.where<Ngram>(Ngram::class.java).findAll().deleteAllFromRealm()
        } else {
            realm.where<Ngram>(Ngram::class.java).equalTo("is_custom", false).findAll().deleteAllFromRealm()
        }
        realm.commitTransaction()
    }

    fun saveDataToDB(ngramRecords: Array<NgramRecordSerializable>) {
        clearDBData(false)
        var nextId = realm.where(Ngram::class.java).max("id") as Int + 1
        try {
            realm.beginTransaction()
            ngramRecords.forEach {
                val ngram = it.ngram
                if(ngram.keyword.isNotEmpty()) {
                    val ngramRow: Ngram = realm.createObject(Ngram::class.java, nextId)
                    ngramRow.keyword = ngram.keyword
                    ngramRow.count = ngram.count
                    ngramRow.lang = ngram.lang
                    ngramRow.gram = ngram.gram
                    ngramRow.roman = ngram.oth.joinToString(",")
                    ngramRow.is_custom = false
                    realm.insert(ngramRow)
                    nextId++
                }
            }
            realm.commitTransaction()
        } catch (ex:Exception){
            Log.e("read_file", ex.localizedMessage)
        }

    }

//    fun reInitRomanData(removeCustom: Boolean = false) {
//        // clear all data
//        // load default data
//        clearDBData(removeCustom)
//        // TODO merge and download from server
//        loadSpellCheckKH()
//        loadSpellCheckEN()
//    }

//    private fun loadSpellCheckKH() {
////        var nextId = realm.where(Ngram::class.java).max("id") as Int + 1
//        var nextId =  KhmerLangApp.getNextKey()
//        try {
//            realm.beginTransaction()
//            context.assets.open(KhmerLangApp.khmerWordsFile).bufferedReader().useLines { lines -> lines.forEach {
//                val word = it.split("\\s".toRegex())//split(",")
//                val keyword = word[0].trim()
//                val count = word[1].toInt()
//                val gram = 1
//                val roman = if (word.size == 3) {
//                    word[2]
//                } else {
//                    ""
//                }
//
//                if(keyword.isNotEmpty()) {
//                    val ngramData: Ngram = realm.createObject(Ngram::class.java, nextId)
//                    ngramData.keyword = keyword
//                    ngramData.roman = roman
//                    ngramData.lang = KhmerLangApp.LANG_KH
//                    ngramData.gram = gram
//                    ngramData.count = count
//                    ngramData.is_custom = false
//                    realm.insert(ngramData)
//                    nextId++
//                }
//            }
//            }
//            realm.commitTransaction()
//        } catch (ex:Exception){
//            Log.e("read_file", ex.localizedMessage)
//        }
//    }
//
//    private fun loadSpellCheckEN() {
////        var nextId = realm.where(Ngram::class.java).max("id") as Int + 1
//        var nextId =  KhmerLangApp.getNextKey()
//        try {
//            realm.beginTransaction()
//            context.assets.open(KhmerLangApp.englishWordsFile).bufferedReader().useLines { lines -> lines.forEach {
//                val word = it.split("\\s".toRegex())//split(",")
//                val keyword = word[0].trim()
//                val count = word[1].toInt()
//                val gram = 1
//                val roman = ""
//
//                if(keyword.isNotEmpty()) {
//                    val ngramData: Ngram = realm.createObject(Ngram::class.java, nextId)
//                    ngramData.keyword = keyword
//                    ngramData.roman = roman
//                    ngramData.lang = KhmerLangApp.LANG_EN
//                    ngramData.gram = gram
//                    ngramData.count = count
//                    ngramData.is_custom = false
//                    realm.insert(ngramData)
//                    nextId++
//                }
//            }
//            }
//            realm.commitTransaction()
//        } catch (ex:Exception){
//            Log.e("read_file", ex.localizedMessage)
//        }
//    }
}