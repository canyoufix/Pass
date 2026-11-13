package com.canyoufix.ui.screens

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.canyoufix.domain.repository.ImportMode
import com.canyoufix.pass.R
import com.canyoufix.ui.components.*
import com.canyoufix.ui.viewmodel.*
import org.koin.compose.viewmodel.koinViewModel

@Composable
internal fun TransferRoute(onBack: () -> Unit, viewModel: TransferViewModel = koinViewModel()) {
    val form by viewModel.form.collectAsStateWithLifecycle()
    val status by viewModel.status.collectAsStateWithLifecycle()
    var confirmReplace by rememberSaveable { mutableStateOf(false) }
    val export =
        rememberLauncherForActivityResult(
            ActivityResultContracts.CreateDocument("application/json"),
        ) { uri ->
            uri?.let { viewModel.export(it.toString()) }
        }
    val importLauncher =
        rememberLauncherForActivityResult(ActivityResultContracts.OpenDocument()) { uri ->
            uri?.let { viewModel.importData(it.toString()) }
        }
    ScreenFrame(
        stringResource(R.string.transfer_title),
        status,
        viewModel::dismissMessage,
        onBack,
    ) { padding ->
        FormColumn(padding) {
            SecretField(
                form.masterPassword,
                { v -> viewModel.edit { it.copy(masterPassword = v) } },
                stringResource(R.string.field_current_master_password),
                !status.busy,
            )
            Text(
                stringResource(R.string.export_title),
                style = MaterialTheme.typography.titleMedium,
            )
            Row(verticalAlignment = androidx.compose.ui.Alignment.CenterVertically) {
                Checkbox(
                    form.encrypted,
                    { v -> viewModel.edit { it.copy(encrypted = v) } },
                    enabled = !status.busy,
                )
                Text(stringResource(R.string.export_encrypted))
            }
            if (!form.encrypted)
                Text(
                    stringResource(R.string.export_plaintext_notice),
                    color = MaterialTheme.colorScheme.error,
                )
            Button(
                { export.launch("TwicePass-export.json") },
                enabled = !status.busy && form.masterPassword.isNotEmpty(),
                modifier = Modifier.fillMaxWidth(),
            ) {
                Text(stringResource(R.string.export_action))
            }
            HorizontalDivider()
            Text(
                stringResource(R.string.import_title),
                style = MaterialTheme.typography.titleMedium,
            )
            SecretField(
                form.sourcePassword,
                { v -> viewModel.edit { it.copy(sourcePassword = v) } },
                stringResource(R.string.field_import_password),
                !status.busy,
            )
            ImportMode.entries.forEach { mode ->
                Row(verticalAlignment = androidx.compose.ui.Alignment.CenterVertically) {
                    RadioButton(
                        form.mode == mode,
                        { viewModel.edit { it.copy(mode = mode) } },
                        enabled = !status.busy,
                    )
                    Text(
                        if (mode == ImportMode.MERGE) stringResource(R.string.import_merge)
                        else stringResource(R.string.import_replace),
                    )
                }
            }
            Button(
                {
                    if (form.mode == ImportMode.REPLACE) confirmReplace = true
                    else importLauncher.launch(arrayOf("application/json", "text/plain"))
                },
                enabled = !status.busy && form.masterPassword.isNotEmpty(),
                modifier = Modifier.fillMaxWidth(),
            ) {
                Text(stringResource(R.string.import_choose_file))
            }
        }
    }
    if (confirmReplace)
        ConfirmDialog(
            stringResource(R.string.import_replace_title),
            stringResource(R.string.import_replace_confirmation),
            { confirmReplace = false },
            {
                confirmReplace = false
                importLauncher.launch(arrayOf("application/json", "text/plain"))
            },
        )
}
