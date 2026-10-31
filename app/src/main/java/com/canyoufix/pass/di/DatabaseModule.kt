package com.canyoufix.pass.di

import androidx.room.Room
import com.canyoufix.data.database.*
import com.canyoufix.domain.CoroutineDispatchers
import java.time.Clock
import kotlinx.coroutines.Dispatchers
import org.koin.dsl.module

val databaseModule = module {
    single { CoroutineDispatchers(Dispatchers.IO, Dispatchers.Default) }
    single<Clock> { Clock.systemUTC() }
    single { Room.databaseBuilder(get(), AppDatabase::class.java, "TwicePass").build() }
    single { DatabaseManager(get(), get()) }
}
