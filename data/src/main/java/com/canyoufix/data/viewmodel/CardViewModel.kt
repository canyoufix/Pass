package com.canyoufix.data.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.canyoufix.data.entity.CardEntity
import com.canyoufix.data.repository.CardRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class CardViewModel(private val repository: CardRepository) : ViewModel() {
    val allCards: StateFlow<List<CardEntity>> = repository.allCards.stateIn(
        viewModelScope,
        SharingStarted.Lazily,
        emptyList()
    )

    fun insert(card: CardEntity) = viewModelScope.launch {
        repository.insert(card)
    }

    fun update(card: CardEntity) = viewModelScope.launch {
        repository.update(card)
    }

    fun delete(card: CardEntity) = viewModelScope.launch {
        repository.delete(card)
    }
}
