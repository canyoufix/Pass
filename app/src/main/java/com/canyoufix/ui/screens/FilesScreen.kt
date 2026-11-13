package com.canyoufix.ui.screens

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.canyoufix.pass.R
import com.canyoufix.ui.components.*
import com.canyoufix.ui.viewmodel.*
import org.koin.compose.viewmodel.koinViewModel

@Composable
internal fun FilesRoute(onBack: () -> Unit, viewModel: FilesViewModel = koinViewModel()) {
    val files by viewModel.files.collectAsStateWithLifecycle()
    val status by viewModel.status.collectAsStateWithLifecycle()
    var selectedName by rememberSaveable { mutableStateOf<String?>(null) }
    var deleteName by rememberSaveable { mutableStateOf<String?>(null) }
    val input =
        rememberLauncherForActivityResult(ActivityResultContracts.OpenDocument()) { uri ->
            uri?.let { viewModel.encrypt(it.toString()) }
        }
    val output =
        rememberLauncherForActivityResult(
            ActivityResultContracts.CreateDocument("application/octet-stream"),
        ) { uri ->
            val name = selectedName
            selectedName = null
            if (uri != null && name != null) viewModel.decrypt(name, uri.toString())
        }
    ScreenFrame(
        stringResource(R.string.files_title),
        status,
        viewModel::dismissMessage,
        onBack,
        actions = {
            TextButton(viewModel::refresh, enabled = !status.busy) {
                Text(stringResource(R.string.action_refresh))
            }
        },
    ) { padding ->
        LazyColumn(
            Modifier.fillMaxSize().padding(padding).padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
            contentPadding = PaddingValues(bottom = 24.dp),
        ) {
            item {
                Button(
                    { input.launch(arrayOf("*/*")) },
                    enabled = !status.busy,
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    Text(stringResource(R.string.files_encrypt))
                }
            }
            if (files.isEmpty() && !status.busy) item { Text(stringResource(R.string.files_empty)) }
            items(files, key = { it.name }) { file ->
                GlassPanel(Modifier.fillMaxWidth()) {
                    Column(
                        Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                    ) {
                        Text(file.name, style = MaterialTheme.typography.titleMedium)
                        Text(stringResource(R.string.files_size_kib, file.size / 1024))
                        OutlinedButton(
                            {
                                selectedName = file.name
                                output.launch(file.name.removeSuffix(".enc"))
                            },
                            enabled = !status.busy,
                        ) {
                            Text(stringResource(R.string.files_decrypt))
                        }
                        TextButton({ deleteName = file.name }, enabled = !status.busy) {
                            Text(stringResource(R.string.action_delete))
                        }
                    }
                }
            }
        }
    }
    deleteName?.let { name ->
        ConfirmDialog(
            stringResource(R.string.file_delete_title),
            name,
            { deleteName = null },
            {
                deleteName = null
                viewModel.delete(name)
            },
        )
    }
}
