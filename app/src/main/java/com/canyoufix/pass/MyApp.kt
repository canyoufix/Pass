package com.canyoufix.pass

import android.app.Application
import android.content.IntentSender
import androidx.work.NetworkType
import androidx.work.WorkManager
import com.canyoufix.autofill.auth.AuthIntentCreator
import com.canyoufix.autofill.auth.AutofillAuthBridge
import com.canyoufix.data.SyncWorker
import com.canyoufix.pass.di.databaseModule
import com.canyoufix.pass.di.repositoryModule
import com.canyoufix.pass.di.viewModelModule
import com.canyoufix.pass.di.networkModule
import org.koin.android.ext.koin.androidContext
import org.koin.core.context.startKoin
import androidx.work.Constraints
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.ExistingWorkPolicy
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.PeriodicWorkRequestBuilder
import java.util.concurrent.TimeUnit

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

    private fun startSyncWorker() {
        val oneTimeRequest = OneTimeWorkRequestBuilder<SyncWorker>()
            .setConstraints(
                Constraints.Builder()
                    .setRequiredNetworkType(NetworkType.CONNECTED)
                    .build()
            )
            .build()

        val periodicRequest = PeriodicWorkRequestBuilder<SyncWorker>(
            15, TimeUnit.MINUTES
        ).setConstraints(
            Constraints.Builder()
                .setRequiredNetworkType(NetworkType.CONNECTED)
                .build()
        ).build()

        val workManager = WorkManager.getInstance(this)

        // Стартовая синхронизация
        workManager.enqueueUniqueWork(
            "sync_on_start",
            ExistingWorkPolicy.KEEP, // или REPLACE, если хочешь перезапуск при каждом старте
            oneTimeRequest
        )

        // Периодическая синхронизация
        workManager.enqueueUniquePeriodicWork(
            "sync_periodic",
            ExistingPeriodicWorkPolicy.KEEP,
            periodicRequest
        )
    }

}