package com.canyoufix.data

import com.canyoufix.data.sync.upsertRemote
import kotlinx.coroutines.test.runTest
import okhttp3.ResponseBody.Companion.toResponseBody
import org.junit.Assert.*
import org.junit.Test
import retrofit2.HttpException
import retrofit2.Response

class RemoteUpsertTest {
    private data class Revision(val value: String, val modified: Long)

    @Test
    fun acknowledgedServerWriteIsNotPostedAgainAfterLostResponse() = runTest {
        var requests = 0
        val revision = Revision("ciphertext", 10)
        val result =
            upsertRemote(
                revision,
                revision,
                Revision::modified,
                {
                    requests++
                    Response.success(Unit)
                },
                {
                    requests++
                    Response.success(Unit)
                },
                { error("not needed") },
            )
        assertEquals(revision, result)
        assertEquals(0, requests)
    }

    @Test
    fun duplicateCreateRaceFetchesExistingRecordBeforeAcknowledging() = runTest {
        val revision = Revision("ciphertext", 10)
        var fetched = false
        val result =
            upsertRemote(
                revision,
                null,
                Revision::modified,
                { Response.error(400, "duplicate".toResponseBody()) },
                { error("must not overwrite") },
                {
                    fetched = true
                    Response.success(revision)
                },
            )
        assertTrue(fetched)
        assertEquals(revision, result)
    }

    @Test
    fun staleLocalMutationNeverOverwritesNewerServerVersion() = runTest {
        val remote = Revision("new", 20)
        val result =
            upsertRemote(
                Revision("old", 10),
                remote,
                Revision::modified,
                { error("must not create") },
                { error("must not update") },
                { error("not needed") },
            )
        assertEquals(remote, result)
    }

    @Test
    fun rejectedPayloadStaysAFailureWhenRecordDoesNotExist() = runTest {
        try {
            upsertRemote(
                Revision("bad payload", 10),
                null,
                Revision::modified,
                { Response.error(400, "invalid".toResponseBody()) },
                { error("not needed") },
                { Response.error(404, "not found".toResponseBody()) },
            )
            fail("Expected HTTP error")
        } catch (failure: HttpException) {
            assertEquals(400, failure.code())
        }
    }
}
