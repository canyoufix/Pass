package com.canyoufix.data.di

import com.canyoufix.data.repository.CardRepository
import com.canyoufix.data.repository.NoteRepository
import com.canyoufix.data.repository.PasswordRepository
import org.koin.dsl.module

val repositoryModule = module {
    single { CardRepository(get()) }
    single { NoteRepository(get()) }
    single { PasswordRepository(get()) }
}