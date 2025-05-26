package com.canyoufix.pass.di

import android.content.Context
import androidx.room.Room
import com.canyoufix.data.database.AppDatabase
import com.canyoufix.data.database.DatabaseManager
import com.canyoufix.pass.R
import org.koin.dsl.module

val databaseModule = module {
    single {
        Room.databaseBuilder(
            get(),
            AppDatabase::class.java,
            get<Context>().getString(R.string.app_name)
        )
            .fallbackToDestructiveMigration()
            .build()
    }
    single { get<AppDatabase>().cardDao() }
    single { get<AppDatabase>().noteDao() }
    single { get<AppDatabase>().passwordDao() }
    single { get<AppDatabase>().queueSyncDao() }

    single { DatabaseManager(get()) }
}