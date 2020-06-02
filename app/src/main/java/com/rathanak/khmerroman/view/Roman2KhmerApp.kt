package com.rathanak.khmerroman.view

import android.app.Application
import com.rathanak.khmerroman.core.predictModule
import org.koin.android.ext.android.startKoin

class Roman2KhmerApp : Application() {

    override fun onCreate() {
        super.onCreate()
        // start Koin!
        startKoin(this, listOf(predictModule))
    }
}