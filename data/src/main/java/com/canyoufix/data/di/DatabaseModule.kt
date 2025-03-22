package com.canyoufix.data.di

import androidx.room.Room
import com.canyoufix.data.database.AppDatabase
import org.koin.dsl.module

val databaseModule = module {
    single {
        Room.databaseBuilder(get(), AppDatabase::class.java, "secure_data_db")
            .fallbackToDestructiveMigration()
            .build()
    }
    single { get<AppDatabase>().cardDao() }
    single { get<AppDatabase>().noteDao() }
    single { get<AppDatabase>().passwordDao() }
}