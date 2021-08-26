package com.rathanak.khmerroman.view

import android.app.Application
import android.util.Log
import com.rathanak.khmerroman.core.predictModule
import com.rathanak.khmerroman.data.DataLoader
import com.rathanak.khmerroman.data.KeyboardPreferences
import com.rathanak.khmerroman.data.RealmMigrations
import io.realm.Realm
import io.realm.RealmConfiguration
import org.koin.android.ext.android.startKoin
import java.io.FileNotFoundException

class Roman2KhmerApp : Application() {

    override fun onCreate() {
        super.onCreate()
        Realm.init(this)
        if(dbConfig == null) {
            dbConfig = RealmConfiguration.Builder()
                .name("khmer_roman.realm")
                .schemaVersion(3)
                .build()
            try {
                Realm.migrateRealm(dbConfig, RealmMigrations())
                Realm.setDefaultConfiguration(dbConfig)
            } catch (ignored: FileNotFoundException) {
                // If the Realm file doesn't exist, just ignore.
            }
        }
        preferences = KeyboardPreferences(applicationContext)

//        TODO check here in thread
//        // check is first open app
//        // should init data
//        if (!preferences!!.getBoolean(KeyboardPreferences.KEY_NOT_FIRST_RUN)) {
//            // load data to db
//            var dataLoader = DataLoader(this)
//            dataLoader.reInitRomanData()
//            preferences!!.putBoolean(KeyboardPreferences.KEY_NOT_FIRST_RUN, true)
//        }

        // start Koin!
        startKoin(this, listOf(predictModule))
    }

    companion object {
        var preferences: KeyboardPreferences? = null
        var dbConfig: RealmConfiguration? = null
        const val khmerWordsFile = "khmer_words_freq_roman.txt"
        const val englishWordsFile = "final_words_v2.txt"
        const val LANG_KH = 1
        const val LANG_EN = 2
        const val ONE_GRAM = 1
        const val TWO_GRAM = 2
    }
}