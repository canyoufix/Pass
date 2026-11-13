package com.canyoufix.pass.sync

import android.content.Context
import androidx.work.*
import com.canyoufix.data.SyncWorker
import com.canyoufix.domain.repository.SyncCoordinator

class PassWorkerFactory(private val sync: SyncCoordinator) : WorkerFactory() {
    override fun createWorker(
        appContext: Context,
        workerClassName: String,
        workerParameters: WorkerParameters,
    ): ListenableWorker? =
        if (workerClassName == SyncWorker::class.java.name)
            SyncWorker(appContext, workerParameters, sync)
        else null
}
