package com.canyoufix.data.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.canyoufix.data.entity.PasswordEntity
import com.canyoufix.data.repository.PasswordRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class PasswordViewModel(private val repository: PasswordRepository) : ViewModel() {
    val allPasswords: StateFlow<List<PasswordEntity>> = repository.getAllPasswords.stateIn(
        viewModelScope,
        SharingStarted.Lazily,
        emptyList()
    )

    fun insert(password: PasswordEntity) = viewModelScope.launch {
        repository.insert(password)
    }

    fun update(password: PasswordEntity) = viewModelScope.launch {
        repository.update(password)
    }

    fun delete(password: PasswordEntity) = viewModelScope.launch {
        repository.delete(password)
    }

    fun getPasswordById(id: String): Flow<PasswordEntity?> {
        return repository.getById(id)
    }
}
