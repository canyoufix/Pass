package com.canyoufix.data.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.canyoufix.data.entity.NoteEntity
import com.canyoufix.data.repository.NoteRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class NoteViewModel(private val repository: NoteRepository) : ViewModel() {
    val allNotes: StateFlow<List<NoteEntity>> = repository.allNotes.stateIn(
        viewModelScope,
        SharingStarted.Lazily,
        emptyList()
    )

    fun insert(note: NoteEntity) = viewModelScope.launch {
        repository.insert(note)
    }

    fun update(note: NoteEntity) = viewModelScope.launch {
        repository.update(note)
    }

    fun delete(note: NoteEntity) = viewModelScope.launch {
        repository.delete(note)
    }

    fun getNoteById(id: String): Flow<NoteEntity?> {
        return repository.getById(id)
    }
}
