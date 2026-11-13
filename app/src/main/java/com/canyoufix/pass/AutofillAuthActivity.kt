package com.canyoufix.pass

import android.app.assist.AssistStructure
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.service.autofill.FillResponse
import android.view.WindowManager
import android.view.autofill.AutofillManager
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.lifecycle.lifecycleScope
import com.canyoufix.autofill.service.AutofillResponseFactory
import com.canyoufix.domain.repository.PasswordRepository
import com.canyoufix.ui.navigation.AuthNavigation
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.launch
import org.koin.android.ext.android.inject

class AutofillAuthActivity : ComponentActivity() {
    private val passwords: PasswordRepository by inject()
    private var completing = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        window.addFlags(WindowManager.LayoutParams.FLAG_SECURE)
        enableEdgeToEdge()
        setResult(
            RESULT_CANCELED,
            Intent().putExtra(AutofillManager.EXTRA_AUTHENTICATION_RESULT, null as FillResponse?),
        )
        setContent { AuthNavigation(onAuthenticated = ::completeAuthentication) }
    }

    @Suppress("DEPRECATION")
    private fun completeAuthentication() {
        if (completing) return
        completing = true
        lifecycleScope.launch {
            try {
                val structure =
                    if (Build.VERSION.SDK_INT >= 33)
                        intent.getParcelableExtra(
                            AutofillManager.EXTRA_ASSIST_STRUCTURE,
                            AssistStructure::class.java,
                        )
                    else
                        intent.getParcelableExtra<AssistStructure>(
                            AutofillManager.EXTRA_ASSIST_STRUCTURE,
                        )
                val fields = structure?.let(AutofillResponseFactory::fields)
                val response = fields?.let {
                    AutofillResponseFactory.unlocked(
                        this@AutofillAuthActivity,
                        it,
                        passwords.findByDomain(it.domain),
                    )
                }
                setResult(
                    RESULT_OK,
                    Intent().putExtra(AutofillManager.EXTRA_AUTHENTICATION_RESULT, response),
                )
                finish()
            } catch (cancelled: CancellationException) {
                completing = false
                throw cancelled
            } catch (_: Exception) {
                finish()
            }
        }
    }
}
