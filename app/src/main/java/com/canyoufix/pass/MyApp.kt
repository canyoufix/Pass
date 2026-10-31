package com.canyoufix.pass

import android.app.Application
import android.content.IntentSender
import android.util.Log
import androidx.work.*
import com.canyoufix.autofill.auth.AuthIntentCreator
import com.canyoufix.autofill.auth.AutofillAuthBridge
import com.canyoufix.data.SyncWorker
import com.canyoufix.data.database.AppDatabase
import com.canyoufix.domain.repository.SettingsRepository
import com.canyoufix.pass.di.*
import com.canyoufix.pass.sync.PassWorkerFactory
import java.util.concurrent.TimeUnit
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import org.koin.android.ext.koin.androidContext
import org.koin.core.context.startKoin

@OptIn(FlowPreview::class)
class MyApp : Application(), Configuration.Provider {
    private lateinit var workerFactory: PassWorkerFactory
    override val workManagerConfiguration: Configuration
        get() = Configuration.Builder().setWorkerFactory(workerFactory).build()

    private val applicationScope =
        CoroutineScope(
            SupervisorJob() +
                Dispatchers.Default +
                CoroutineExceptionHandler { _, _ ->
                    Log.e("Pass", "Background synchronization observer stopped")
                },
        )

    override fun onCreate() {
        super.onCreate()
        val koin = startKoin {
            androidContext(this@MyApp)
            modules(appModule)
        }
            .koin
        workerFactory = koin.get()
        AutofillAuthBridge.initialize(
            object : AuthIntentCreator {
                override fun createAuthIntent(): IntentSender =
                    AuthIntentProvider.createAuthIntent(this@MyApp)
            },
        )
        applicationScope.launch {
            koin
                .get<SettingsRepository>()
                .syncSettings
                .map { it.isEnabled }
                .distinctUntilChanged()
                .collect { enabled ->
                    val manager = WorkManager.getInstance(this@MyApp)
                    if (enabled) {
                        val constraints =
                            Constraints.Builder()
                                .setRequiredNetworkType(NetworkType.CONNECTED)
                                .build()
                        manager.enqueueUniquePeriodicWork(
                            "sync_periodic",
                            ExistingPeriodicWorkPolicy.UPDATE,
                            PeriodicWorkRequestBuilder<SyncWorker>(15, TimeUnit.MINUTES)
                                .setConstraints(constraints)
                                .setBackoffCriteria(BackoffPolicy.EXPONENTIAL, 30, TimeUnit.SECONDS)
                                .build(),
                        )
                        manager.enqueueUniqueWork(
                            "sync_on_start",
                            ExistingWorkPolicy.KEEP,
                            OneTimeWorkRequestBuilder<SyncWorker>()
                                .setConstraints(constraints)
                                .build(),
                        )
                    } else {
                        manager.cancelUniqueWork("sync_periodic")
                        manager.cancelUniqueWork("sync_on_start")
                        manager.cancelUniqueWork("sync_changes")
                    }
                }
        }
        applicationScope.launch {
            combine(
                    koin.get<SettingsRepository>().syncSettings,
                    koin.get<AppDatabase>().queueSyncDao().getAll(),
                ) { settings, pending ->
                    settings.isEnabled to pending.map { it.id }
                }
                .distinctUntilChanged()
                .debounce(500)
                .collect { (enabled, pending) ->
                    if (enabled && pending.isNotEmpty())
                        WorkManager.getInstance(this@MyApp)
                            .enqueueUniqueWork(
                                "sync_changes",
                                ExistingWorkPolicy.APPEND_OR_REPLACE,
                                OneTimeWorkRequestBuilder<SyncWorker>()
                                    .setConstraints(
                                        Constraints.Builder()
                                            .setRequiredNetworkType(NetworkType.CONNECTED)
                                            .build(),
                                    )
                                    .setBackoffCriteria(
                                        BackoffPolicy.EXPONENTIAL,
                                        30,
                                        TimeUnit.SECONDS,
                                    )
                                    .build(),
                            )
                }
        }
    }
}
