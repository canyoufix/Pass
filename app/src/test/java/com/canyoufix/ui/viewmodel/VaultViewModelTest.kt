package com.canyoufix.ui.viewmodel

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.viewModelScope
import com.canyoufix.domain.CoroutineDispatchers
import com.canyoufix.domain.model.*
import com.canyoufix.domain.repository.*
import java.io.IOException
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.test.*
import org.junit.Assert.*
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class VaultViewModelTest {
    private class Passwords : PasswordRepository {
        var fail = false
        private val entries =
            listOf(
                PasswordEntry(
                    "record",
                    "Mail",
                    "https://example.com",
                    "alice",
                    "do-not-search-this",
                    10L,
                ),
            )

        override fun observeAll(): Flow<List<PasswordEntry>> = flow {
            if (fail) throw IOException("unavailable")
            emit(entries)
        }

        override fun observeById(id: String) = flowOf(entries.firstOrNull { it.id == id })

        override suspend fun save(entry: PasswordEntry) = Unit

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
    fun searchUsesMetadataButNeverThePassword() = runTest {
        val dispatcher = UnconfinedTestDispatcher(testScheduler)
        Dispatchers.setMain(dispatcher)
        var owned: VaultViewModel? = null
        try {
            val vm =
                VaultViewModel(
                    SavedStateHandle(),
                    Passwords(),
                    cards,
                    notes,
                    CoroutineDispatchers(dispatcher, dispatcher),
                )
            owned = vm
            backgroundScope.launch(dispatcher) { vm.state.collect() }
            advanceUntilIdle()
            vm.query("alice")
            advanceUntilIdle()
            assertEquals(1, vm.state.value.entries.size)
            vm.query("do-not-search-this")
            advanceUntilIdle()
            assertTrue(vm.state.value.entries.isEmpty())
        } finally {
            backgroundScope.coroutineContext.cancelChildren()
            owned?.viewModelScope?.cancel()
            runCurrent()
            Dispatchers.resetMain()
        }
    }

    @Test
    fun retryRecoversAfterAReadFailure() = runTest {
        val dispatcher = UnconfinedTestDispatcher(testScheduler)
        Dispatchers.setMain(dispatcher)
        var owned: VaultViewModel? = null
        try {
            val repository = Passwords().apply { fail = true }
            val vm =
                VaultViewModel(
                    SavedStateHandle(),
                    repository,
                    cards,
                    notes,
                    CoroutineDispatchers(dispatcher, dispatcher),
                )
            owned = vm
            backgroundScope.launch(dispatcher) { vm.state.collect() }
            advanceUntilIdle()
            assertNotNull(vm.state.value.error)
            repository.fail = false
            vm.retry()
            advanceUntilIdle()
            assertNull(vm.state.value.error)
            assertFalse(vm.state.value.loading)
            assertEquals(1, vm.state.value.entries.size)
        } finally {
            backgroundScope.coroutineContext.cancelChildren()
            owned?.viewModelScope?.cancel()
            runCurrent()
            Dispatchers.resetMain()
        }
    }
}
