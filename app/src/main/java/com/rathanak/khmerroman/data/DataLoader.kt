package com.rathanak.khmerroman.data

import android.content.Context
import android.util.Log
import com.rathanak.khmerroman.serializable.NgramRecordSerializable
import com.rathanak.khmerroman.view.KhmerLangApp
import io.realm.Realm
import io.realm.RealmResults


class DataLoader() {
    private var realm: Realm = Realm.getInstance(KhmerLangApp.dbConfig)

    private fun clearDBData(removeCustom: Boolean = false) {
        realm.beginTransaction()
        if (removeCustom) {
            realm.where<Ngram>(Ngram::class.java).findAll().deleteAllFromRealm()
        } else {
            realm.where<Ngram>(Ngram::class.java).equalTo("custom", false).findAll().deleteAllFromRealm()
        }
        realm.commitTransaction()
    }

    fun saveDataToDB(ngramRecords: ArrayList<NgramRecordSerializable>) {
        clearDBData(false)
        var nextId = getNextKey()
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
                    ngramRow.other = ngram.oth.joinToString(SEPERATOR)
                    ngramRow.custom = false
                    realm.insert(ngramRow)
                    nextId++
                }
            }
            realm.commitTransaction()
        } catch (ex:Exception) {
            Log.e("read_file", ex.localizedMessage)
        }
    }

    companion object {
        val SEPERATOR = ","
        fun getNextKey(): Int {
            return try {
                val number: Number? = Realm.getDefaultInstance().where(Ngram::class.java).max("id")
                if (number != null) {
                    number.toInt() + 1
                } else {
                    0
                }
            } catch (e: ArrayIndexOutOfBoundsException) {
                0
            }
        }
    }
}