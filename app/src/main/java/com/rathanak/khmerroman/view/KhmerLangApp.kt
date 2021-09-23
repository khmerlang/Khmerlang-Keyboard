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




class KhmerLangApp : Application() {

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
        // load status
        if(preferences != null) {
            R2KhmerService.dataStatus = preferences!!.getInt(KeyboardPreferences.KEY_DATA_STATUS, KeyboardPreferences.STATUS_NONE)
        }

        loadSpellingData()
        // start Koin!
        startKoin(this, listOf(predictModule))
    }

    //  load spell suggestion data
    private fun loadSpellingData() {
        GlobalScope.launch(Dispatchers.Main) {
            loadSpelling()
        }
    }

    private suspend fun loadSpelling() {
        coroutineScope {
            async(Dispatchers.IO) {
                R2KhmerService.spellingCorrector.loadData(applicationContext)
            }
        }
    }

    companion object {
        var preferences: KeyboardPreferences? = null
        var dbConfig: RealmConfiguration? = null
        const val khmerWordsFile = "khmer_words_freq_roman.txt"
        const val englishWordsFile = "final_words_v2.txt"
        const val mobiledataFile = "mobile-keyboard-data.bin"
        const val LANG_KH = 1
        const val LANG_EN = 2
        const val ONE_GRAM = 1
        const val TWO_GRAM = 2

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