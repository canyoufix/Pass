package com.canyoufix.pass.di

import com.canyoufix.crypto.*
import com.canyoufix.data.files.EncryptedFilesRepositoryImpl
import com.canyoufix.data.files.VaultFileLock
import com.canyoufix.data.json.VaultTransferRepositoryImpl
import com.canyoufix.data.mapping.EntryCodec
import com.canyoufix.data.repository.CardRepository as RoomCardRepository
import com.canyoufix.data.repository.NoteRepository as RoomNoteRepository
import com.canyoufix.data.repository.PasswordRepository as RoomPasswordRepository
import com.canyoufix.data.session.DefaultSessionRepository
import com.canyoufix.domain.repository.*
import com.canyoufix.settings.datastore.*
import org.koin.core.module.dsl.singleOf
import org.koin.dsl.bind
import org.koin.dsl.module

val repositoryModule = module {
    singleOf(::SecurePrefsManager).bind<CredentialStore>()
    single<CryptoEngine> { CryptoManager }
    single<SessionKeyStore> { SessionAESKeyHolder }
    singleOf(::VaultFileLock)
    singleOf(::MasterPasswordManager)
    singleOf(::EntryCodec)
    singleOf(::DefaultSessionRepository).bind<SessionRepository>()
    singleOf(::RoomPasswordRepository).bind<PasswordRepository>()
    singleOf(::RoomCardRepository).bind<CardRepository>()
    singleOf(::RoomNoteRepository).bind<NoteRepository>()
    singleOf(::SyncSettingsStore)
    singleOf(::DefaultSettingsRepository).bind<SettingsRepository>()
    singleOf(::VaultTransferRepositoryImpl).bind<VaultTransferRepository>()
    singleOf(::EncryptedFilesRepositoryImpl).bind<EncryptedFilesRepository>()
}
