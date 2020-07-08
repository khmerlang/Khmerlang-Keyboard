package com.rathanak.khmerroman.view

import android.app.Application
import com.rathanak.khmerroman.core.predictModule
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
                .schemaVersion(1)
                .build()
            try {
                Realm.migrateRealm(dbConfig, RealmMigrations())
            } catch (ignored: FileNotFoundException) {
                // If the Realm file doesn't exist, just ignore.
            }
        }

        // start Koin!
        startKoin(this, listOf(predictModule))
    }

    companion object {
        var dbConfig: RealmConfiguration? = null
        const val khmerWordsFile = "khmer_words_freq_roman.txt"
        const val englishWordsFile = "final_words_v2.txt"
    }
}