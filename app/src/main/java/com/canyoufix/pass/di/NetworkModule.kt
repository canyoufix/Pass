package com.canyoufix.pass.di

import com.canyoufix.settings.datastore.SyncSettingsStore
import com.canyoufix.sync.retrofit.RetrofitClientProvider
import org.koin.dsl.module

val networkModule = module {
    single { SyncSettingsStore(get()) }
    single { RetrofitClientProvider(get()) }
}