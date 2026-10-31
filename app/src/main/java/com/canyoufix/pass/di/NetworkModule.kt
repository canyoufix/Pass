package com.canyoufix.pass.di

import com.canyoufix.data.SyncManager
import com.canyoufix.data.sync.SyncQueueProcessor
import com.canyoufix.domain.repository.SyncCoordinator
import com.canyoufix.pass.sync.PassWorkerFactory
import com.canyoufix.sync.retrofit.RetrofitClientProvider
import org.koin.core.module.dsl.singleOf
import org.koin.dsl.bind
import org.koin.dsl.module

val networkModule = module {
    singleOf(::RetrofitClientProvider)
    singleOf(::SyncQueueProcessor)
    singleOf(::SyncManager).bind<SyncCoordinator>()
    singleOf(::PassWorkerFactory)
}
