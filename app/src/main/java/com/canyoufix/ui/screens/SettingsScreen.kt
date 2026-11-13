package com.canyoufix.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.canyoufix.domain.repository.*
import com.canyoufix.pass.R
import com.canyoufix.ui.components.*
import com.canyoufix.ui.viewmodel.*
import org.koin.compose.viewmodel.koinViewModel

@Composable
internal fun SettingsRoute(
    onSecurity: () -> Unit,
    onSync: () -> Unit,
    onTransfer: () -> Unit,
    onAppearance: () -> Unit,
) {
    ScreenFrame(stringResource(R.string.nav_settings)) { padding ->
        FormColumn(padding) {
            GlassPanel(Modifier.fillMaxWidth()) {
                CategoryCard(
                    stringResource(R.string.security_title),
                    R.drawable.ic_security,
                    onSecurity,
                )
                SettingsDivider()
                CategoryCard(stringResource(R.string.sync_title), R.drawable.ic_sync, onSync)
                SettingsDivider()
                CategoryCard(stringResource(R.string.nav_vault), R.drawable.ic_storage, onTransfer)
                SettingsDivider()
                CategoryCard(
                    stringResource(R.string.appearance_title),
                    R.drawable.ic_palette,
                    onAppearance,
                )
            }
        }
    }
}

@Composable
internal fun AppearanceRoute(onBack: () -> Unit, viewModel: SettingsViewModel = koinViewModel()) {
    val theme by viewModel.theme.collectAsStateWithLifecycle()
    val status by viewModel.status.collectAsStateWithLifecycle()
    ScreenFrame(
        stringResource(R.string.appearance_title),
        status,
        viewModel::dismissMessage,
        onBack,
    ) { padding ->
        FormColumn(padding) {
            Text(stringResource(R.string.theme_title), style = MaterialTheme.typography.titleMedium)
            GlassPanel(Modifier.fillMaxWidth()) {
                AppTheme.entries.forEach { option ->
                    Row(
                        Modifier.fillMaxWidth().padding(horizontal = 12.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        RadioButton(
                            theme == option,
                            { viewModel.theme(option) },
                            enabled = !status.busy,
                        )
                        Text(
                            when (option) {
                                AppTheme.SYSTEM -> stringResource(R.string.theme_system)
                                AppTheme.LIGHT -> stringResource(R.string.theme_light)
                                AppTheme.DARK -> stringResource(R.string.theme_dark)
                            },
                        )
                    }
                }
            }
        }
    }
}

@Composable
internal fun SecurityRoute(onBack: () -> Unit, onLock: () -> Unit) {
    val viewModel: SettingsViewModel = koinViewModel()
    val encryptionMode by viewModel.encryptionMode.collectAsStateWithLifecycle()
    val status by viewModel.status.collectAsStateWithLifecycle()
    ScreenFrame(
        stringResource(R.string.security_title),
        status,
        viewModel::dismissMessage,
        onBack,
    ) { padding ->
        FormColumn(padding) {
            CategoryCard(stringResource(R.string.action_lock), R.drawable.ic_security, onLock)
            Text(
                stringResource(R.string.encryption_mode_title),
                style = MaterialTheme.typography.titleMedium,
            )
            Text(stringResource(R.string.encryption_mode_description))
            GlassPanel(Modifier.fillMaxWidth()) {
                EncryptionMode.entries.forEach { option ->
                    Row(
                        Modifier.fillMaxWidth().padding(horizontal = 12.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        RadioButton(
                            selected = encryptionMode == option,
                            onClick = { viewModel.encryptionMode(option) },
                            enabled = !status.busy,
                        )
                        Text(
                            when (option) {
                                EncryptionMode.AES -> stringResource(R.string.encryption_mode_aes)
                                EncryptionMode.RSA_AES -> stringResource(R.string.encryption_mode_rsa)
                                EncryptionMode.X25519_AES -> stringResource(R.string.encryption_mode_x25519)
                                EncryptionMode.ML_KEM_AES -> stringResource(R.string.encryption_mode_pq)
                            },
                        )
                    }
                }
            }
        }
    }
}

@Composable
internal fun SyncSettingsRoute(onBack: () -> Unit, viewModel: SettingsViewModel = koinViewModel()) {
    val draft by viewModel.draft.collectAsStateWithLifecycle()
    val saved by viewModel.saved.collectAsStateWithLifecycle()
    val status by viewModel.status.collectAsStateWithLifecycle()
    ScreenFrame(stringResource(R.string.sync_title), status, viewModel::dismissMessage, onBack) {
        padding ->
        FormColumn(padding) {
            Text(
                if (saved.isEnabled) stringResource(R.string.sync_enabled)
                else stringResource(R.string.sync_disabled),
            )
            OutlinedTextField(
                draft.ip,
                { v -> viewModel.edit { it.copy(ip = v) } },
                label = { Text(stringResource(R.string.sync_server)) },
                singleLine = true,
                enabled = !status.busy && !saved.isEnabled,
                modifier = Modifier.fillMaxWidth(),
            )
            OutlinedTextField(
                draft.port,
                { v -> viewModel.edit { it.copy(port = v) } },
                label = { Text(stringResource(R.string.sync_port)) },
                singleLine = true,
                enabled = !status.busy && !saved.isEnabled,
                modifier = Modifier.fillMaxWidth(),
                keyboardOptions =
                    androidx.compose.foundation.text.KeyboardOptions(
                        keyboardType = androidx.compose.ui.text.input.KeyboardType.Number,
                    ),
            )
            if (saved.isEnabled) {
                Button(
                    viewModel::syncNow,
                    enabled = !status.busy,
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    Text(stringResource(R.string.sync_now))
                }
                OutlinedButton(
                    viewModel::disconnect,
                    enabled = !status.busy,
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    Text(stringResource(R.string.sync_disable))
                }
            } else
                Button(
                    viewModel::connect,
                    enabled = !status.busy,
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    Text(stringResource(R.string.sync_connect))
                }
        }
    }
}

@Composable
private fun SettingsDivider() {
    HorizontalDivider(
        Modifier.padding(start = 58.dp, end = 16.dp),
        color = MaterialTheme.colorScheme.outlineVariant,
    )
}
