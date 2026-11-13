package com.canyoufix.domain.usecase

import com.canyoufix.domain.model.*
import com.canyoufix.domain.repository.EntryRepository

class SaveEntry<T : VaultEntry>(private val repository: EntryRepository<T>) {
    suspend operator fun invoke(entry: T) {
        require(entry.title.isNotBlank()) { "Введите название" }
        require(entry.title.length <= 500) { "Название слишком длинное" }
        repository.save(entry)
    }
}
