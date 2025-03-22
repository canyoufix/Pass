package com.canyoufix.pass

import android.app.Application
import com.canyoufix.data.di.databaseModule
import com.canyoufix.data.di.repositoryModule
import com.canyoufix.data.di.viewModelModule
import org.koin.android.ext.koin.androidContext
import org.koin.core.context.startKoin

class MyApp : Application() {
    override fun onCreate() {
        super.onCreate()
        startKoin {
            androidContext(this@MyApp)
            modules(databaseModule, repositoryModule, viewModelModule)
        }
    }
}