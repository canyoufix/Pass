package com.canyoufix.data

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.canyoufix.domain.repository.SyncCoordinator
import kotlinx.coroutines.CancellationException

class SyncWorker(context: Context, params: WorkerParameters, private val sync: SyncCoordinator) :
    CoroutineWorker(context, params) {
    override suspend fun doWork(): Result =
        try {
            sync.sync()
            Result.success()
        } catch (cancelled: CancellationException) {
            throw cancelled
        } catch (_: Exception) {
            Result.retry()
        }
}
