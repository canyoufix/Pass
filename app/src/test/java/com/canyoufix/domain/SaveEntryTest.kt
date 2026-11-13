package com.canyoufix.domain

import com.canyoufix.domain.model.NoteEntry
import com.canyoufix.domain.repository.*
import com.canyoufix.domain.usecase.SaveEntry
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.test.runTest
import org.junit.Assert.*
import org.junit.Test

class SaveEntryTest {
    private class Notes : NoteRepository {
        var saved: NoteEntry? = null

        override fun observeAll() = flowOf(emptyList<NoteEntry>())

        override fun observeById(id: String) = flowOf<NoteEntry?>(null)

        override suspend fun save(entry: NoteEntry) {
            saved = entry
        }

        override suspend fun delete(entry: NoteEntry) = Unit
    }

    @Test
    fun invalidTitleNeverReachesPersistence() = runTest {
        val repository = Notes()
        try {
            SaveEntry(repository)(NoteEntry(title = "  ", content = "secret"))
            fail("Expected validation error")
        } catch (_: IllegalArgumentException) {}
        assertNull(repository.saved)
    }

    @Test
    fun contentAndIdentityArePreserved() = runTest {
        val repository = Notes()
        val entry =
            NoteEntry(
                id = "existing",
                title = "Title",
                content = "  exact secret \n",
                lastModified = 123L,
            )
        SaveEntry(repository)(entry)
        assertEquals(entry, repository.saved)
    }

    @Test
    fun realAndDecoyPasswordsMustDiffer() {
        assertThrows(IllegalArgumentException::class.java) {
            SetupCredentials("same", "same", "same", "same").validate()
        }
        SetupCredentials("real", "real", "decoy", "decoy").validate()
    }

    @Test
    fun invalidPortsAndPathsAreRejected() {
        for (port in listOf("0", "65536", "abc", "")) assertThrows(
            IllegalArgumentException::class.java,
        ) {
            SyncSettings(ip = "localhost", port = port).validateEndpoint()
        }
        assertThrows(IllegalArgumentException::class.java) {
            SyncSettings(ip = "http://server/path", port = "8080").validateEndpoint()
        }
        SyncSettings(ip = "localhost", port = "8080").validateEndpoint()
    }
}
