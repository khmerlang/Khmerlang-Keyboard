package com.rathanak.khmerroman.view

import android.app.Application
import com.rathanak.khmerroman.core.predictModule
import com.rathanak.khmerroman.data.KeyboardPreferences
import com.rathanak.khmerroman.data.RealmMigrations
import com.rathanak.khmerroman.keyboard.R2KhmerService
import io.realm.Realm
import io.realm.RealmConfiguration
import kotlinx.coroutines.*
import org.koin.android.ext.android.startKoin
import java.io.FileNotFoundException

class KhmerLangApp : Application() {

    override fun onCreate() {
        super.onCreate()
        Realm.init(this)
        if(dbConfig == null) {
            dbConfig = RealmConfiguration.Builder()
                .name("khmer_roman.realm")
                .schemaVersion(5)
                .compactOnLaunch()
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
            R2KhmerService.downloadDataStatus = preferences!!.getInt(KeyboardPreferences.KEY_DATA_STATUS, KeyboardPreferences.STATUS_NONE)
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
                var realm: Realm = Realm.getInstance(KhmerLangApp.dbConfig)
                try {
                    R2KhmerService.spellingCorrector.loadData(realm)
                } finally {
                    realm.close()
                }
            }
        }
    }

    companion object {
        var preferences: KeyboardPreferences? = null
        var dbConfig: RealmConfiguration? = null
        const val LANG_KH = 0
        const val LANG_EN = 1
        const val ONE_GRAM = 1
        const val TWO_GRAM = 2
        const val THREE_GRAM = 3
    }
}