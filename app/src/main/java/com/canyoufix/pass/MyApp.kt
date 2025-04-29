package com.canyoufix.pass

import android.app.Application
import android.content.IntentSender
import com.canyoufix.autofill.auth.AuthIntentCreator
import com.canyoufix.autofill.auth.AutofillAuthBridge
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

        AutofillAuthBridge.initialize(object : AuthIntentCreator {
            override fun createAuthIntent(): IntentSender? {
                return AuthIntentProvider.createAuthIntent(this@MyApp)
            }
        })

    }
}