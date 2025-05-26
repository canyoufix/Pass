package com.canyoufix.pass

import android.app.Application
import android.content.IntentSender
import com.canyoufix.autofill.auth.AuthIntentCreator
import com.canyoufix.autofill.auth.AutofillAuthBridge
import com.canyoufix.pass.di.databaseModule
import com.canyoufix.pass.di.repositoryModule
import com.canyoufix.pass.di.viewModelModule
import com.canyoufix.pass.di.networkModule
import org.koin.android.ext.koin.androidContext
import org.koin.core.context.startKoin

class MyApp : Application() {
    override fun onCreate() {
        super.onCreate()
        startKoin {
            androidContext(this@MyApp)
            modules(
                databaseModule,
                repositoryModule,
                viewModelModule,
                networkModule
            )
        }

        AutofillAuthBridge.initialize(object : AuthIntentCreator {
            override fun createAuthIntent(): IntentSender? {
                return AuthIntentProvider.createAuthIntent(this@MyApp)
            }
        })

    }
}