package com.canyoufix.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import com.canyoufix.pass.R
import com.canyoufix.ui.viewmodel.ActionStatus

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun ScreenFrame(
    title: String,
    status: ActionStatus = ActionStatus(),
    onDismissMessage: () -> Unit = {},
    onBack: (() -> Unit)? = null,
    actions: @Composable RowScope.() -> Unit = {},
    floatingActionButton: @Composable () -> Unit = {},
    titleIcon: Int? = null,
    content: @Composable (PaddingValues) -> Unit,
) {
    val snackbar = remember { SnackbarHostState() }
    LaunchedEffect(status.message) {
        status.message?.let {
            snackbar.showSnackbar(it)
            onDismissMessage()
        }
    }
    Scaffold(
        containerColor = Color.Transparent,
        topBar = {
            TopAppBar(
                title = {
                    val pageIcon =
                        titleIcon
                            ?: when (title) {
                                stringResource(R.string.nav_vault) -> R.drawable.ic_storage
                                stringResource(R.string.nav_settings) -> R.drawable.ic_settings
                                stringResource(R.string.generator_title) -> R.drawable.ic_generator
                                stringResource(R.string.category_passwords) ->
                                    R.drawable.ic_password
                                stringResource(R.string.category_cards) -> R.drawable.ic_card
                                stringResource(R.string.category_notes) -> R.drawable.ic_note
                                stringResource(R.string.files_title) -> R.drawable.ic_file
                                stringResource(R.string.sync_title) -> R.drawable.ic_sync
                                stringResource(R.string.appearance_title) -> R.drawable.ic_palette
                                stringResource(R.string.transfer_title) -> R.drawable.ic_storage
                                else -> R.drawable.ic_security
                            }
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                    ) {
                        Icon(painterResource(pageIcon), null, Modifier.size(22.dp))
                        Text(
                            title,
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.SemiBold,
                        )
                    }
                },
                colors =
                    TopAppBarDefaults.topAppBarColors(
                        containerColor = MaterialTheme.colorScheme.surfaceContainerLow,
                        scrolledContainerColor = MaterialTheme.colorScheme.surfaceContainerLow,
                    ),
                navigationIcon = {
                    if (onBack != null)
                        IconButton(onClick = onBack) {
                            Icon(
                                painterResource(R.drawable.ic_back),
                                stringResource(R.string.action_back),
                            )
                        }
                },
                actions = actions,
            )
        },
        snackbarHost = { SnackbarHost(snackbar) },
        floatingActionButton = floatingActionButton,
    ) { padding ->
        Box(Modifier.fillMaxSize()) {
            content(padding)
            if (status.busy)
                LinearProgressIndicator(
                    Modifier.fillMaxWidth().padding(top = padding.calculateTopPadding()),
                )
        }
    }
}

@Composable
internal fun FormColumn(padding: PaddingValues, content: @Composable ColumnScope.() -> Unit) {
    Box(
        Modifier.fillMaxSize().padding(padding).imePadding(),
        contentAlignment = Alignment.TopCenter,
    ) {
        Column(
            Modifier.widthIn(max = 720.dp)
                .fillMaxWidth()
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
            content = content,
        )
    }
}

@Composable
internal fun SecretField(
    value: String,
    onChange: (String) -> Unit,
    label: String,
    enabled: Boolean = true,
    readOnly: Boolean = false,
) {
    var visible by remember { mutableStateOf(false) }
    OutlinedTextField(
        value = value,
        onValueChange = onChange,
        label = { Text(label) },
        enabled = enabled,
        readOnly = readOnly,
        singleLine = true,
        shape = MaterialTheme.shapes.medium,
        colors =
            OutlinedTextFieldDefaults.colors(
                unfocusedContainerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.7f),
                focusedContainerColor = MaterialTheme.colorScheme.surface,
                unfocusedBorderColor = MaterialTheme.colorScheme.outlineVariant,
            ),
        modifier = Modifier.fillMaxWidth(),
        visualTransformation =
            if (visible) VisualTransformation.None else PasswordVisualTransformation(),
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
        trailingIcon = {
            IconButton(onClick = { visible = !visible }) {
                Icon(
                    painterResource(
                        if (visible) R.drawable.ic_visibility_off else R.drawable.ic_visibility,
                    ),
                    contentDescription =
                        stringResource(
                            if (visible) R.string.action_hide else R.string.action_reveal,
                        ),
                    modifier = Modifier.size(22.dp),
                )
            }
        },
    )
}

@Composable
internal fun ConfirmDialog(
    title: String,
    text: String,
    onDismiss: () -> Unit,
    onConfirm: () -> Unit,
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(title) },
        text = { Text(text) },
        confirmButton = {
            TextButton(onClick = onConfirm) { Text(stringResource(R.string.action_confirm)) }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text(stringResource(R.string.action_cancel)) }
        },
    )
}
