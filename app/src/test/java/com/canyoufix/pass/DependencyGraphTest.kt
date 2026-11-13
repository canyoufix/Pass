package com.canyoufix.pass

import android.content.Context
import androidx.lifecycle.SavedStateHandle
import com.canyoufix.pass.di.appModule
import kotlinx.coroutines.CoroutineDispatcher
import org.junit.Test
import org.koin.core.annotation.KoinExperimentalAPI
import org.koin.test.verify.verify

@OptIn(KoinExperimentalAPI::class)
class DependencyGraphTest {
    @Test
    fun applicationGraphContainsEveryConstructorDependency() {
        // Context/SavedStateHandle come from Android; dispatchers are explicit platform constants
        // in DatabaseModule.
        appModule.verify(
            extraTypes =
                listOf(Context::class, SavedStateHandle::class, CoroutineDispatcher::class),
        )
    }
}
