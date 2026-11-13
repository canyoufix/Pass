package com.canyoufix.ui.viewmodel

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.viewModelScope
import com.canyoufix.domain.model.*
import com.canyoufix.domain.repository.*
import com.canyoufix.domain.usecase.SaveEntry
import java.util.UUID
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

data class EditorState(
    val kind: EntryKind,
    val id: String,
    val title: String = "",
    val url: String = "",
    val username: String = "",
    val password: String = "",
    val number: String = "",
    val expiryDate: String = "",
    val cvc: String = "",
    val holderName: String = "",
    val content: String = "",
    val loading: Boolean = false,
    val missing: Boolean = false,
    val completed: Boolean = false,
    val loadError: String? = null,
    val revision: Long? = null,
) {
    override fun toString(): String = "EditorState(kind=$kind, id=$id, redacted)"
}

class EntryEditorViewModel(
    handle: SavedStateHandle,
    private val passwords: PasswordRepository,
    private val cards: CardRepository,
    private val notes: NoteRepository,
) : ActionViewModel() {
    private val kind =
        EntryKind.entries.firstOrNull { it.name == handle.get<String>("kind") }
            ?: EntryKind.PASSWORD
    private val entryId: String? = handle.get<String>("id")
    val isNew = entryId == null
    // Drafts live only in memory, never in SavedStateHandle/Bundle or logs.
    private val mutableState =
        MutableStateFlow(
            EditorState(kind, entryId ?: UUID.randomUUID().toString(), loading = !isNew),
        )
    val state = mutableState.asStateFlow()
    private var loadJob: Job? = null

    init {
        if (!isNew) load()
    }

    fun load() {
        val id = entryId ?: return
        loadJob?.cancel()
        loadJob = viewModelScope.launch {
            mutableState.value = mutableState.value.copy(loading = true, loadError = null)
            try {
                val entry =
                    when (kind) {
                        EntryKind.PASSWORD -> passwords.observeById(id).first()
                        EntryKind.CARD -> cards.observeById(id).first()
                        EntryKind.NOTE -> notes.observeById(id).first()
                    }
                mutableState.value =
                    when (entry) {
                        is PasswordEntry ->
                            EditorState(
                                kind,
                                id,
                                entry.title,
                                entry.url,
                                entry.username,
                                entry.password,
                                revision = entry.lastModified,
                            )
                        is CardEntry ->
                            EditorState(
                                kind,
                                id,
                                title = entry.title,
                                number = entry.number,
                                expiryDate = entry.expiryDate,
                                cvc = entry.cvc,
                                holderName = entry.holderName,
                                revision = entry.lastModified,
                            )
                        is NoteEntry ->
                            EditorState(
                                kind,
                                id,
                                title = entry.title,
                                content = entry.content,
                                revision = entry.lastModified,
                            )
                        null -> EditorState(kind, id, missing = true)
                    }
            } catch (cancelled: CancellationException) {
                throw cancelled
            } catch (failure: Exception) {
                mutableState.value =
                    mutableState.value.copy(loading = false, loadError = failure.userMessage())
            }
        }
    }

    fun edit(change: (EditorState) -> EditorState) {
        if (!status.value.busy && !state.value.loading)
            mutableState.value = change(mutableState.value)
    }

    fun save() = perform {
        val s = state.value
        when (kind) {
            EntryKind.PASSWORD ->
                SaveEntry(passwords)(
                    PasswordEntry(
                        s.id,
                        s.title.trim(),
                        s.url.trim(),
                        s.username,
                        s.password,
                        lastModified = s.revision ?: 0L,
                    ),
                )
            EntryKind.CARD ->
                SaveEntry(cards)(
                    CardEntry(
                        s.id,
                        s.title.trim(),
                        s.number,
                        s.expiryDate,
                        s.cvc,
                        s.holderName,
                        lastModified = s.revision ?: 0L,
                    ),
                )
            EntryKind.NOTE ->
                SaveEntry(notes)(
                    NoteEntry(s.id, s.title.trim(), s.content, lastModified = s.revision ?: 0L),
                )
        }
        mutableState.value =
            state.value.copy(completed = true, password = "", cvc = "", content = "")
    }

    fun delete() = perform {
        val s = state.value
        when (kind) {
            EntryKind.PASSWORD ->
                passwords.delete(
                    PasswordEntry(
                        s.id,
                        s.title,
                        s.url,
                        s.username,
                        s.password,
                        lastModified = s.revision ?: 0L,
                    ),
                )
            EntryKind.CARD ->
                cards.delete(
                    CardEntry(
                        s.id,
                        s.title,
                        s.number,
                        s.expiryDate,
                        s.cvc,
                        s.holderName,
                        lastModified = s.revision ?: 0L,
                    ),
                )
            EntryKind.NOTE ->
                notes.delete(NoteEntry(s.id, s.title, s.content, lastModified = s.revision ?: 0L))
        }
        mutableState.value =
            state.value.copy(completed = true, password = "", cvc = "", content = "")
    }
}
