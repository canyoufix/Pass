package com.canyoufix.data

import com.canyoufix.data.entity.NoteEntity
import com.canyoufix.data.serialization.storageJson
import kotlinx.serialization.json.*
import org.junit.Assert.*
import org.junit.Test

class StorageJsonTest {
    @Test
    fun wirePayloadAlwaysContainsIdentityRevisionAndDeletionState() {
        val entry =
            NoteEntity(
                id = "stable-id",
                title = "cipher",
                content = "cipher",
                lastModified = System.currentTimeMillis(),
            )
        val encoded = storageJson.encodeToString(entry)
        val fields = storageJson.parseToJsonElement(encoded).jsonObject
        assertEquals("stable-id", fields.getValue("id").jsonPrimitive.content)
        assertEquals(entry.lastModified, fields.getValue("lastModified").jsonPrimitive.long)
        assertFalse(fields.getValue("isDeleted").jsonPrimitive.boolean)
        assertEquals(entry, storageJson.decodeFromString<NoteEntity>(encoded))
    }
}
