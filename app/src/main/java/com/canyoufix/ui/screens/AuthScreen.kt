package com.canyoufix.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import com.canyoufix.pass.R
import com.canyoufix.ui.components.*
import com.canyoufix.ui.viewmodel.*

@Composable
internal fun AuthScreen(
    setup: Boolean,
    form: AuthForm,
    status: ActionStatus,
    onEdit: ((AuthForm) -> AuthForm) -> Unit,
    onSubmit: () -> Unit,
    onReset: () -> Unit,
    onDismissMessage: () -> Unit,
) {
    var confirmReset by rememberSaveable { mutableStateOf(false) }
    ScreenFrame(
        stringResource(if (setup) R.string.setup_master_password else R.string.authorization),
        status,
        onDismissMessage,
    ) { padding ->
        FormColumn(padding) {
            Text("TwicePass", style = MaterialTheme.typography.headlineLarge)
            Text(
                if (setup) stringResource(R.string.setup_prompt)
                else stringResource(R.string.auth_prompt),
            )
            SecretField(
                form.password,
                { text -> onEdit { it.copy(password = text) } },
                stringResource(R.string.master_password),
                !status.busy,
            )
            if (setup) {
                SecretField(
                    form.confirmation,
                    { text -> onEdit { it.copy(confirmation = text) } },
                    stringResource(R.string.confirm_password),
                    !status.busy,
                )
                SecretField(
                    form.decoy,
                    { text -> onEdit { it.copy(decoy = text) } },
                    stringResource(R.string.fake_master_password),
                    !status.busy,
                )
                SecretField(
                    form.decoyConfirmation,
                    { text -> onEdit { it.copy(decoyConfirmation = text) } },
                    stringResource(R.string.confirm_fake_password),
                    !status.busy,
                )
            }
            status.message?.let { Text(it, color = MaterialTheme.colorScheme.error) }
            Button(onClick = onSubmit, enabled = !status.busy, modifier = Modifier.fillMaxWidth()) {
                Text(stringResource(if (setup) R.string.save_and_continue else R.string.login))
            }
            if (!setup)
                OutlinedButton(
                    onClick = { confirmReset = true },
                    enabled = !status.busy,
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    Text(stringResource(R.string.reset_all))
                }
        }
    }
    if (confirmReset)
        ConfirmDialog(
            stringResource(R.string.reset_title),
            stringResource(R.string.reset_confirmation),
            onDismiss = { confirmReset = false },
            onConfirm = {
                confirmReset = false
                onReset()
            },
        )
}
