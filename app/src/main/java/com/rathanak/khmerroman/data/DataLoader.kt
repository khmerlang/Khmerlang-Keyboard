package com.rathanak.khmerroman.data

import android.content.Context
import android.util.Log
import com.rathanak.khmerroman.serializable.NgramRecordSerializable
import com.rathanak.khmerroman.view.KhmerLangApp
import io.realm.Realm


class DataLoader {
//    private var realm: Realm = Realm.getInstance(KhmerLangApp.dbConfig)

    fun clearDBData(removeCustom: Boolean = false) {
        var realm: Realm = Realm.getInstance(KhmerLangApp.dbConfig)
        realm.beginTransaction()
        if (removeCustom) {
            realm.where<Ngram>(Ngram::class.java).findAll().deleteAllFromRealm()
        } else {
            realm.where<Ngram>(Ngram::class.java).equalTo("custom", false).findAll().deleteAllFromRealm()
        }
        realm.commitTransaction()
        realm.close()
    }

    fun saveDataToDB(ngramRecords: ArrayList<NgramRecordSerializable>, isRemoveCustom: Boolean) {
        clearDBData(isRemoveCustom)
        var nextId = getNextKey()
        try {
            var realm: Realm = Realm.getInstance(KhmerLangApp.dbConfig)
            realm.beginTransaction()
            ngramRecords.forEach {
                val ngram = it.ngram
                if(ngram.keyword.isNotEmpty()) {
                    val ngramRow: Ngram = realm.createObject(Ngram::class.java, nextId)
                    ngramRow.keyword = ngram.keyword
                    ngramRow.count = ngram.count
                    ngramRow.lang = ngram.lang
                    ngramRow.gram = ngram.gram
                    ngramRow.other = ngram.oth.joinToString(SEPERATOR)
                    ngramRow.custom = false
                    realm.insert(ngramRow)
                    nextId++
                }
            }
            realm.commitTransaction()
            realm.close()
        } catch (ex:Exception) {
            Log.e("read_file", ex.localizedMessage)
        }
    }

    companion object {
        val SEPERATOR = ","
        fun getNextKey(): Int {
            val realm: Realm = Realm.getInstance(KhmerLangApp.dbConfig)
            try {
                val number: Number? = realm.where(Ngram::class.java).max("id")
                return if (number != null) {
                    number.toInt() + 1
                } else {
                    0
                }
            } catch (e: ArrayIndexOutOfBoundsException) {
                return 0
            } finally {
                realm.close()
            }
        }
    }
}