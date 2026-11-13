package com.canyoufix.data

import com.canyoufix.data.entity.QueueSyncEntity
import com.canyoufix.data.sync.*
import java.io.IOException
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.test.runTest
import okhttp3.ResponseBody.Companion.toResponseBody
import org.junit.Assert.*
import org.junit.Test
import retrofit2.Response

class SyncQueueProcessorTest {
    private fun item(id: String) = QueueSyncEntity(id, "note", "insert", "{}")

    @Test
    fun httpFailureIsNotAcknowledgedAndOtherItemsStillSend() = runTest {
        val acknowledged = mutableListOf<String>()
        try {
            SyncQueueProcessor()
                .flush(
                    listOf(item("failure"), item("success")),
                    send = {
                        if (it.id == "failure")
                            Response.error<Unit>(500, "error".toResponseBody()).requireSuccess()
                    },
                    acknowledge = { acknowledged += it.id },
                )
            fail("Expected retryable failure")
        } catch (_: IOException) {}
        assertEquals(listOf("success"), acknowledged)
    }

    @Test
    fun cancellationStopsQueueAndNeverAcknowledgesCancelledRequest() = runTest {
        var calls = 0
        val acknowledged = mutableListOf<String>()
        try {
            SyncQueueProcessor()
                .flush(
                    listOf(item("first"), item("second")),
                    send = {
                        calls++
                        throw CancellationException("cancel")
                    },
                    acknowledge = { acknowledged += it.id },
                )
            fail("Expected cancellation")
        } catch (_: CancellationException) {}
        assertEquals(1, calls)
        assertTrue(acknowledged.isEmpty())
    }

    @Test
    fun successfulRequestsAreAcknowledgedInOrder() = runTest {
        val acknowledged = mutableListOf<String>()
        SyncQueueProcessor()
            .flush(listOf(item("a"), item("b")), send = {}, acknowledge = { acknowledged += it.id })
        assertEquals(listOf("a", "b"), acknowledged)
    }

    @Test
    fun pendingLocalChangesWinEvenOverNewerRemoteData() {
        assertFalse(acceptRemoteChange(10L, 100L, true))
        assertFalse(acceptRemoteChange(10L, 9L, false))
        assertTrue(acceptRemoteChange(10L, 10L, false))
        assertTrue(acceptRemoteChange(10L, 11L, false))
        assertTrue(acceptRemoteChange(null, 0L, false))
    }

    @Test
    fun supersededMutationsAreAcknowledgedOnlyAfterLatestSucceeds() = runTest {
        val sent = mutableListOf<String>()
        val acknowledged = mutableListOf<String>()
        val entries = listOf(item("old"), item("latest"))
        SyncQueueProcessor()
            .flush(
                entries,
                send = { sent += it.id },
                acknowledge = { acknowledged += it.id },
                identity = { "same-record" },
            )
        assertEquals(listOf("latest"), sent)
        assertEquals(listOf("old", "latest"), acknowledged)
        acknowledged.clear()
        try {
            SyncQueueProcessor()
                .flush(
                    entries,
                    send = { throw IOException("offline") },
                    acknowledge = { acknowledged += it.id },
                    identity = { "same-record" },
                )
            fail("Expected retry")
        } catch (_: IOException) {}
        assertTrue(acknowledged.isEmpty())
    }

    @Test
    fun timestampsAdvanceEvenWithinOneMillisecondOrAfterClockRollback() {
        assertEquals(100L, nextLocalTimestamp(100, null))
        assertEquals(101L, nextLocalTimestamp(100, 100))
        assertEquals(101L, nextLocalTimestamp(90, 100))
        assertThrows(IllegalStateException::class.java) { nextLocalTimestamp(1, Long.MAX_VALUE) }
    }
}
