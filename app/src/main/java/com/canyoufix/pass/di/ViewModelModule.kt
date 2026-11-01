package com.canyoufix.pass.di

import com.canyoufix.domain.usecase.*
import com.canyoufix.ui.viewmodel.*
import org.koin.core.module.dsl.*
import org.koin.dsl.module

val viewModelModule = module {
    factory { GeneratePassword() }
    factoryOf(::ExportVault)
    factoryOf(::ImportVault)
    viewModelOf(::AuthViewModel)
    viewModelOf(::VaultViewModel)
    viewModelOf(::EntryEditorViewModel)
    viewModelOf(::SettingsViewModel)
    viewModelOf(::FilesViewModel)
    viewModelOf(::TransferViewModel)
    viewModelOf(::GeneratorViewModel)
}
