package com.canyoufix.ui.viewmodel

import androidx.lifecycle.SavedStateHandle
import com.canyoufix.domain.model.*
import com.canyoufix.domain.repository.*
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.test.*
import org.junit.Assert.*
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class EditorViewModelTest {
    private class Passwords : PasswordRepository {
        var calls = 0
        var existing: PasswordEntry? = null
        var lastSaved: PasswordEntry? = null
        var failure: Exception? = null
        var gate: CompletableDeferred<Unit>? = null

        override fun observeAll() = flowOf(emptyList<PasswordEntry>())

        override fun observeById(id: String) = flowOf(existing)

        override suspend fun save(entry: PasswordEntry) {
            calls++
            lastSaved = entry
            gate?.await()
            failure?.let { throw it }
        }

        override suspend fun delete(entry: PasswordEntry) = Unit

        override suspend fun findByDomain(domain: String) = emptyList<PasswordEntry>()
    }

    private val cards =
        object : CardRepository {
            override fun observeAll() = flowOf(emptyList<CardEntry>())

            override fun observeById(id: String) = flowOf<CardEntry?>(null)

            override suspend fun save(entry: CardEntry) = Unit

            override suspend fun delete(entry: CardEntry) = Unit
        }
    private val notes =
        object : NoteRepository {
            override fun observeAll() = flowOf(emptyList<NoteEntry>())

            override fun observeById(id: String) = flowOf<NoteEntry?>(null)

            override suspend fun save(entry: NoteEntry) = Unit

            override suspend fun delete(entry: NoteEntry) = Unit
        }

    @Test
    fun navigationWaitsForPersistenceAndRepeatedSaveIsIgnored() = runTest {
        Dispatchers.setMain(StandardTestDispatcher(testScheduler))
        try {
            val repo = Passwords().apply { gate = CompletableDeferred() }
            val handle = SavedStateHandle(mapOf("kind" to "PASSWORD"))
            val vm = EntryEditorViewModel(handle, repo, cards, notes)
            vm.edit { it.copy(title = "Account", password = "private") }
            vm.save()
            vm.save()
            runCurrent()
            assertEquals(1, repo.calls)
            assertFalse(vm.state.value.completed)
            assertTrue(vm.status.value.busy)
            assertEquals(setOf("kind"), handle.keys())
            repo.gate!!.complete(Unit)
            advanceUntilIdle()
            assertTrue(vm.state.value.completed)
            assertEquals("", vm.state.value.password)
        } finally {
            Dispatchers.resetMain()
        }
    }

    @Test
    fun failedSaveKeepsEditorOpenWithDraftIntact() = runTest {
        Dispatchers.setMain(StandardTestDispatcher(testScheduler))
        try {
            val repo = Passwords().apply { failure = IllegalStateException("Storage unavailable") }
            val vm =
                EntryEditorViewModel(
                    SavedStateHandle(mapOf("kind" to "PASSWORD")),
                    repo,
                    cards,
                    notes,
                )
            vm.edit { it.copy(title = "Account", password = "private") }
            vm.save()
            advanceUntilIdle()
            assertFalse(vm.state.value.completed)
            assertFalse(vm.status.value.busy)
            assertEquals("private", vm.state.value.password)
            assertNotNull(vm.status.value.message)
        } finally {
            Dispatchers.resetMain()
        }
    }

    @Test
    fun missingRecordFinishesLoadingInsteadOfShowingSpinnerForever() = runTest {
        Dispatchers.setMain(StandardTestDispatcher(testScheduler))
        try {
            val vm =
                EntryEditorViewModel(
                    SavedStateHandle(mapOf("kind" to "PASSWORD", "id" to "gone")),
                    Passwords(),
                    cards,
                    notes,
                )
            advanceUntilIdle()
            assertTrue(vm.state.value.missing)
            assertFalse(vm.state.value.loading)
        } finally {
            Dispatchers.resetMain()
        }
    }

    @Test
    fun editingPreservesTheLoadedRevisionForOptimisticConcurrencyChecks() = runTest {
        Dispatchers.setMain(StandardTestDispatcher(testScheduler))
        try {
            val repo =
                Passwords().apply {
                    existing = PasswordEntry("record", "Original", "site", "user", "secret", 123L)
                }
            val vm =
                EntryEditorViewModel(
                    SavedStateHandle(mapOf("kind" to "PASSWORD", "id" to "record")),
                    repo,
                    cards,
                    notes,
                )
            advanceUntilIdle()
            vm.edit { it.copy(title = "Changed") }
            vm.save()
            advanceUntilIdle()
            assertEquals(123L, repo.lastSaved!!.lastModified)
            assertEquals("Changed", repo.lastSaved!!.title)
        } finally {
            Dispatchers.resetMain()
        }
    }
}
