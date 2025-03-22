package com.canyoufix.data.di

import com.canyoufix.data.viewmodel.CardViewModel
import com.canyoufix.data.viewmodel.NoteViewModel
import com.canyoufix.data.viewmodel.PasswordViewModel
import org.koin.dsl.module

val viewModelModule = module {
    single { CardViewModel(get()) }
    single { NoteViewModel(get()) }
    single { PasswordViewModel(get()) }
}