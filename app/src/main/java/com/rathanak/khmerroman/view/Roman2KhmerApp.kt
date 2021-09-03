package com.rathanak.khmerroman.view

import android.app.Application
import android.net.ConnectivityManager
import android.util.Log
import androidx.lifecycle.coroutineScope
import com.rathanak.khmerroman.core.predictModule
import com.rathanak.khmerroman.data.DataLoader
import com.rathanak.khmerroman.data.KeyboardPreferences
import com.rathanak.khmerroman.data.Ngram
import com.rathanak.khmerroman.data.RealmMigrations
import com.rathanak.khmerroman.keyboard.R2KhmerService
import io.realm.Realm
import io.realm.RealmConfiguration
import kotlinx.coroutines.*
import org.koin.android.ext.android.startKoin
import java.io.FileNotFoundException
import java.net.URL
import org.json.JSONTokener

import org.json.JSONObject




class Roman2KhmerApp : Application() {

    override fun onCreate() {
        super.onCreate()
        Realm.init(this)
        if(dbConfig == null) {
            dbConfig = RealmConfiguration.Builder()
                .name("khmer_roman.realm")
                .schemaVersion(2)
                .build()
            try {
                Realm.migrateRealm(dbConfig, RealmMigrations())
                Realm.setDefaultConfiguration(dbConfig)
            } catch (ignored: FileNotFoundException) {
                // If the Realm file doesn't exist, just ignore.
            }
        }
        preferences = KeyboardPreferences(applicationContext)

        //  preferences!!.getString(KeyboardPreferences.KEY_BANNER_IDS, "")
        bannerData = ""
        GlobalScope.launch(Dispatchers.Main) {
            fetchData()
        }

//        val connectivityManager = getSystemService(ConnectivityManager::class.java)
//        connectivityManager.registerDefaultNetworkCallback(object : ConnectivityManager.NetworkCallback() {
//
//        }

        // start Koin!
        startKoin(this, listOf(predictModule))
    }

    private suspend fun fetchData() {
        coroutineScope {
            async(Dispatchers.IO) {
                val result = URL(BANNER_META).readText()
                val data = JSONTokener(result).nextValue() as JSONObject
                bannerData = data.getJSONArray("banner_ids").join(",")
            }
        }
    }

    companion object {
        var preferences: KeyboardPreferences? = null
        var dbConfig: RealmConfiguration? = null
        var bannerData: String = ""
        const val khmerWordsFile = "khmer_words_freq_roman.txt"
        const val englishWordsFile = "final_words_v2.txt"
        const val LANG_KH = 1
        const val LANG_EN = 2
        const val ONE_GRAM = 1
        const val TWO_GRAM = 2
        const val BANNER_META = "https://banner.khmerlang.com/mobile/meta"
        const val BANNER_IMAGE = "https://banner.khmerlang.com/mobile/images"
        const val BANNER_VISIT = "https://banner.khmerlang.com/mobile/visits"

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