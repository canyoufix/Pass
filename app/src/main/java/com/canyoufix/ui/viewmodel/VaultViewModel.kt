package com.canyoufix.ui.viewmodel

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.viewModelScope
import com.canyoufix.domain.CoroutineDispatchers
import com.canyoufix.domain.model.*
import com.canyoufix.domain.repository.*
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.*

data class VaultFilter(val query: String = "", val kind: EntryKind? = null)

data class VaultState(
    val entries: List<VaultEntry> = emptyList(),
    val loading: Boolean = true,
    val error: String? = null,
)

@OptIn(ExperimentalCoroutinesApi::class)
class VaultViewModel(
    handle: SavedStateHandle,
    passwords: PasswordRepository,
    cards: CardRepository,
    notes: NoteRepository,
    dispatchers: CoroutineDispatchers,
) : ActionViewModel() {
    val kind: EntryKind? =
        handle.get<String>("kind")?.let { value ->
            runCatching { EntryKind.valueOf(value) }.getOrNull()
        }
    private val mutableFilter = MutableStateFlow(VaultFilter(kind = kind))
    val filter = mutableFilter.asStateFlow()
    private val retries = MutableStateFlow(0)

    fun retry() {
        retries.value++
    }

    val state =
        retries
            .flatMapLatest {
                combine(passwords.observeAll(), cards.observeAll(), notes.observeAll(), filter) {
                        p,
                        c,
                        n,
                        f ->
                        val entries: List<VaultEntry> = p + c + n
                        VaultState(
                            entries
                                .filter { entry ->
                                    val kindMatches =
                                        when (f.kind) {
                                            EntryKind.PASSWORD -> entry is PasswordEntry
                                            EntryKind.CARD -> entry is CardEntry
                                            EntryKind.NOTE -> entry is NoteEntry
                                            null -> true
                                        }
                                    val searchable =
                                        when (entry) {
                                            is PasswordEntry ->
                                                entry.title + " " + entry.username + " " + entry.url
                                            is CardEntry -> entry.title + " " + entry.holderName
                                            is NoteEntry -> entry.title + " " + entry.content
                                        }
                                    kindMatches &&
                                        searchable.contains(f.query.trim(), ignoreCase = true)
                                }
                                .sortedByDescending { it.lastModified },
                            loading = false,
                        )
                    }
                    .catch { emit(VaultState(loading = false, error = it.userMessage())) }
            }
            .flowOn(dispatchers.computation)
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), VaultState())

    fun query(value: String) {
        mutableFilter.value = mutableFilter.value.copy(query = value)
    }

    fun filter(kind: EntryKind?) {
        mutableFilter.value = mutableFilter.value.copy(kind = kind)
    }
}
