package com.canyoufix.ui.viewmodel

import com.canyoufix.domain.repository.*
import com.canyoufix.domain.usecase.ExportVault
import com.canyoufix.domain.usecase.ImportVault
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

class FilesViewModel(private val documents: EncryptedFilesRepository) : ActionViewModel() {
    private val mutableFiles = MutableStateFlow<List<VaultFile>>(emptyList())
    val files = mutableFiles.asStateFlow()

    init {
        refresh()
    }

    fun refresh() = perform { mutableFiles.value = documents.listFiles() }

    fun encrypt(uri: String) =
        perform("Файл зашифрован") {
            documents.encryptFile(uri)
            mutableFiles.value = documents.listFiles()
        }

    fun decrypt(name: String, uri: String) =
        perform("Файл расшифрован") { documents.decryptFile(name, uri) }

    fun delete(name: String) =
        perform("Файл удалён") {
            documents.deleteFile(name)
            mutableFiles.value = documents.listFiles()
        }
}

data class TransferForm(
    val masterPassword: String = "",
    val sourcePassword: String = "",
    val encrypted: Boolean = true,
    val mode: ImportMode = ImportMode.MERGE,
) {
    override fun toString(): String = "TransferForm(encrypted=$encrypted, mode=$mode, redacted)"
}

class TransferViewModel(
    private val exportVault: ExportVault,
    private val importVault: ImportVault,
) : ActionViewModel() {
    private val mutableForm = MutableStateFlow(TransferForm())
    val form = mutableForm.asStateFlow()

    fun edit(change: (TransferForm) -> TransferForm) {
        if (!status.value.busy) mutableForm.value = change(form.value)
    }

    fun export(uri: String) =
        perform("Экспорт завершён") {
            val value = form.value
            try {
                exportVault(uri, value.encrypted, value.masterPassword)
            } finally {
                mutableForm.value = mutableForm.value.copy(masterPassword = "", sourcePassword = "")
            }
        }

    fun importData(uri: String) =
        perform("Импорт завершён") {
            val value = form.value
            try {
                importVault(uri, value.mode, value.masterPassword, value.sourcePassword)
            } finally {
                mutableForm.value = mutableForm.value.copy(masterPassword = "", sourcePassword = "")
            }
        }
}
