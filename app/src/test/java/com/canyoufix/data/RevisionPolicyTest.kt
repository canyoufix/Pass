package com.canyoufix.data

import com.canyoufix.data.repository.requireUnchangedRevision
import org.junit.Assert.assertThrows
import org.junit.Test

class RevisionPolicyTest {
    @Test
    fun outdatedEditorCannotOverwriteRemoteChangeOrDeletedRecord() {
        assertThrows(IllegalStateException::class.java) { requireUnchangedRevision(10, 11, false) }
        assertThrows(IllegalStateException::class.java) { requireUnchangedRevision(10, 10, true) }
        requireUnchangedRevision(10, 10, false)
    }
}
