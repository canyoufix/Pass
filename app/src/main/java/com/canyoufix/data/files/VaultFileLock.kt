package com.canyoufix.data.files

import kotlinx.coroutines.sync.Mutex

class VaultFileLock {
    internal val mutex = Mutex()
}
