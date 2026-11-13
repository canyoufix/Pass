package com.canyoufix.data.sync

import com.canyoufix.data.entity.QueueSyncEntity
import java.io.IOException
import kotlinx.coroutines.CancellationException

/** Acknowledge only successful requests; a failed item never disappears from the outbox. */
class SyncQueueProcessor {
    suspend fun flush(
        items: List<QueueSyncEntity>,
        send: suspend (QueueSyncEntity) -> Unit,
        acknowledge: suspend (QueueSyncEntity) -> Unit,
        identity: (QueueSyncEntity) -> String = { it.id },
    ) {
        var failed = 0
        for (mutations in items.groupBy(identity).values) {
            val item = mutations.last()
            try {
                send(item)
                // Older mutations are superseded, but only after the latest one succeeds.
                for (mutation in mutations) acknowledge(mutation)
            } catch (cancelled: CancellationException) {
                throw cancelled
            } catch (_: Exception) {
                failed++
            }
        }
        if (failed != 0)
            throw IOException("Не отправлено изменений: " + failed + ". Они сохранены в очереди")
    }
}
