package com.canyoufix.pass.di

import org.koin.dsl.module

val appModule = module {
    includes(databaseModule, repositoryModule, networkModule, viewModelModule)
}
