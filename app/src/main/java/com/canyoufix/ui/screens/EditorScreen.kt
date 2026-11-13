package com.canyoufix.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.canyoufix.domain.model.EntryKind
import com.canyoufix.pass.R
import com.canyoufix.ui.components.*
import com.canyoufix.ui.viewmodel.*
import org.koin.compose.viewmodel.koinViewModel

@Composable
internal fun EditorRoute(onBack: () -> Unit, viewModel: EntryEditorViewModel = koinViewModel()) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val status by viewModel.status.collectAsStateWithLifecycle()
    LaunchedEffect(state.completed) { if (state.completed) onBack() }
    EditorScreen(
        state,
        status,
        viewModel.isNew,
        viewModel::edit,
        viewModel::save,
        viewModel::delete,
        viewModel::load,
        viewModel::dismissMessage,
        onBack,
    )
}

@Composable
private fun EditorScreen(
    state: EditorState,
    status: ActionStatus,
    isNew: Boolean,
    onEdit: ((EditorState) -> EditorState) -> Unit,
    onSave: () -> Unit,
    onDelete: () -> Unit,
    onRetry: () -> Unit,
    onDismiss: () -> Unit,
    onBack: () -> Unit,
) {
    var reloadDialog by rememberSaveable { mutableStateOf(false) }
    var deleteDialog by rememberSaveable { mutableStateOf(false) }
    ScreenFrame(
        if (isNew) stringResource(R.string.entry_new) else stringResource(R.string.entry_edit),
        status,
        onDismiss,
        onBack,
        titleIcon = state.kind.icon(),
        actions = {
            if (!isNew)
                TextButton({ reloadDialog = true }, enabled = !status.busy && !state.loading) {
                    Text(stringResource(R.string.action_refresh))
                }
        },
    ) { padding ->
        FormColumn(padding) {
            when {
                state.loading -> CircularProgressIndicator()
                state.missing -> Text(stringResource(R.string.entry_missing))
                state.loadError != null -> {
                    Text(state.loadError, color = MaterialTheme.colorScheme.error)
                    Button(onRetry) { Text(stringResource(R.string.action_retry)) }
                }
                else -> {
                    val enabled = !status.busy
                    OutlinedTextField(
                        state.title,
                        { v -> onEdit { it.copy(title = v) } },
                        label = { Text(stringResource(R.string.field_title)) },
                        enabled = enabled,
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                    )
                    when (state.kind) {
                        EntryKind.PASSWORD -> {
                            OutlinedTextField(
                                state.url,
                                { v -> onEdit { it.copy(url = v) } },
                                label = { Text(stringResource(R.string.field_website)) },
                                enabled = enabled,
                                modifier = Modifier.fillMaxWidth(),
                                singleLine = true,
                            )
                            OutlinedTextField(
                                state.username,
                                { v -> onEdit { it.copy(username = v) } },
                                label = { Text(stringResource(R.string.field_username)) },
                                enabled = enabled,
                                modifier = Modifier.fillMaxWidth(),
                                singleLine = true,
                            )
                            SecretField(
                                state.password,
                                { v -> onEdit { it.copy(password = v) } },
                                stringResource(R.string.field_password),
                                enabled,
                            )
                        }
                        EntryKind.CARD -> {
                            SecretField(
                                state.number,
                                { v -> onEdit { it.copy(number = v) } },
                                stringResource(R.string.field_card_number),
                                enabled,
                            )
                            OutlinedTextField(
                                state.expiryDate,
                                { v -> onEdit { it.copy(expiryDate = v) } },
                                label = { Text(stringResource(R.string.field_expiry)) },
                                enabled = enabled,
                                modifier = Modifier.fillMaxWidth(),
                                singleLine = true,
                            )
                            SecretField(
                                state.cvc,
                                { v -> onEdit { it.copy(cvc = v) } },
                                "CVC / CVV",
                                enabled,
                            )
                            OutlinedTextField(
                                state.holderName,
                                { v -> onEdit { it.copy(holderName = v) } },
                                label = { Text(stringResource(R.string.field_cardholder)) },
                                enabled = enabled,
                                modifier = Modifier.fillMaxWidth(),
                                singleLine = true,
                            )
                        }
                        EntryKind.NOTE ->
                            OutlinedTextField(
                                state.content,
                                { v -> onEdit { it.copy(content = v) } },
                                label = { Text(stringResource(R.string.field_content)) },
                                enabled = enabled,
                                modifier = Modifier.fillMaxWidth(),
                                minLines = 5,
                            )
                    }
                    status.message?.let { Text(it, color = MaterialTheme.colorScheme.error) }
                    Button(onSave, enabled = enabled, modifier = Modifier.fillMaxWidth()) {
                        Text(stringResource(R.string.action_save))
                    }
                    if (!isNew)
                        OutlinedButton(
                            { deleteDialog = true },
                            enabled = enabled,
                            modifier = Modifier.fillMaxWidth(),
                        ) {
                            Text(stringResource(R.string.entry_delete))
                        }
                }
            }
        }
    }
    if (reloadDialog)
        ConfirmDialog(
            stringResource(R.string.editor_reload_title),
            stringResource(R.string.editor_reload_confirmation),
            { reloadDialog = false },
            {
                reloadDialog = false
                onRetry()
            },
        )
    if (deleteDialog)
        ConfirmDialog(
            stringResource(R.string.entry_delete_title),
            stringResource(R.string.entry_delete_confirmation),
            { deleteDialog = false },
            {
                deleteDialog = false
                onDelete()
            },
        )
}
