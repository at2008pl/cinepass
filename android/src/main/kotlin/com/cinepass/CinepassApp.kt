package com.cinepass

import android.app.Application
import com.cinepass.di.initKoin
import com.cinepass.ui.auth.RegisterScreenProvider
import com.cinepass.ui.auth.android.AndroidRegisterScreen
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
        
        // Register the Android implementation of RegisterScreen
        RegisterScreenProvider.content = { onSuccess, onLogin ->
            AndroidRegisterScreen(onSuccess, onLogin)
        }

        initKoin {
            androidLogger()
            androidContext(this@CinepassApp)
        }
    }
}
