package com.cinepass

import android.app.Application
import com.cinepass.di.initKoin
import org.koin.android.ext.koin.androidContext
import org.koin.android.ext.koin.androidLogger

class CinepassApp : Application() {
    companion object {
        lateinit var instance: CinepassApp
            private set
    }

    override fun onCreate() {
        super.onCreate()
        instance = this

        initKoin {
            androidLogger()
            androidContext(this@CinepassApp)
        }
    }
}
