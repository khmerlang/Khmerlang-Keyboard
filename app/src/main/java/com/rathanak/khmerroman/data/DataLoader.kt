package com.rathanak.khmerroman.data

import android.content.Context
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.rathanak.khmerroman.migration.MyMigration
import io.realm.Realm
import io.realm.RealmConfiguration
import io.realm.RealmResults
import java.io.IOException
import java.util.*


class DataLoader(applicationContext: Context ) {
    private var realm: Realm
    private var context: Context

    init {
        realm = Realm.getDefaultInstance()
        context = applicationContext
    }

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
        val jsonFileString = getJsonDataFromAsset(context, "roman_khmer.json")
        val gson = Gson()
        val listPersonType = object : TypeToken<List<RomanData>>() {}.type

        var romans: List<RomanData> = gson.fromJson(jsonFileString, listPersonType)
        realm.beginTransaction()
        romans.forEach {
            var roman = it
            val romanKhmer: RomanItem = realm.createObject(RomanItem::class.java, UUID.randomUUID().toString())
            romanKhmer.khmer = roman.k
            romanKhmer.roman = roman.r
            realm.insert(romanKhmer)
        }
        realm.commitTransaction()
    }
    private fun getJsonDataFromAsset(context: Context, fileName: String): String? {
        val jsonString: String
        try {
            jsonString = context.assets.open(fileName).bufferedReader().use { it.readText() }
        } catch (ioException: IOException) {
            ioException.printStackTrace()
            return null
        }
        return jsonString
    }
}