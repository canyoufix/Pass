package com.canyoufix.pass.di

import com.canyoufix.data.SyncManager
import com.canyoufix.settings.datastore.SyncSettingsStore
import com.canyoufix.sync.retrofit.RetrofitClientProvider
import org.koin.dsl.module

val networkModule = module {
    single { SyncManager(get(), get(), get(), get(), get(), get()) }
    single { SyncSettingsStore(get()) }
    single { RetrofitClientProvider(get()) }
}