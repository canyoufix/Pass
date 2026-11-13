package com.canyoufix.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.*
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.canyoufix.domain.model.*
import com.canyoufix.pass.R
import com.canyoufix.ui.components.*
import com.canyoufix.ui.viewmodel.*
import org.koin.compose.viewmodel.koinViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun StorageRoute(
    onCategory: (EntryKind) -> Unit,
    onAdd: (EntryKind) -> Unit,
    onFiles: () -> Unit,
) {
    var showAdd by remember { mutableStateOf(false) }
    ScreenFrame(stringResource(R.string.nav_vault)) { padding ->
        Box(Modifier.fillMaxSize().padding(padding)) {
            LazyColumn(
                Modifier.widthIn(max = 720.dp).fillMaxSize().align(Alignment.TopCenter),
                contentPadding =
                    PaddingValues(start = 20.dp, end = 20.dp, top = 20.dp, bottom = 112.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                item {
                    GlassPanel(Modifier.fillMaxWidth()) {
                        EntryKind.entries.forEachIndexed { index, kind ->
                            CategoryCard(kind.label(), kind.icon(), { onCategory(kind) })
                            if (index < EntryKind.entries.lastIndex)
                                HorizontalDivider(
                                    Modifier.padding(start = 56.dp, end = 16.dp),
                                    color =
                                        MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f),
                                )
                        }
                    }
                }
            }
            Row(
                Modifier.align(Alignment.BottomCenter)
                    .widthIn(max = 720.dp)
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp, vertical = 20.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                FloatingActionButton(
                    onClick = onFiles,
                    shape = androidx.compose.foundation.shape.CircleShape,
                    containerColor = MaterialTheme.colorScheme.surfaceContainerHigh,
                    contentColor = MaterialTheme.colorScheme.onSurface,
                ) {
                    Icon(
                        painterResource(R.drawable.ic_file),
                        stringResource(R.string.files_title),
                        Modifier.size(24.dp),
                    )
                }
                FloatingActionButton(
                    onClick = { showAdd = true },
                    shape = androidx.compose.foundation.shape.CircleShape,
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    contentColor = MaterialTheme.colorScheme.onPrimaryContainer,
                ) {
                    Icon(painterResource(R.drawable.ic_add), stringResource(R.string.action_add))
                }
            }
        }
    }
    if (showAdd)
        ModalBottomSheet(onDismissRequest = { showAdd = false }) {
            Column(
                Modifier.fillMaxWidth().padding(horizontal = 20.dp).padding(bottom = 28.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                Text(
                    stringResource(R.string.entry_new),
                    style = MaterialTheme.typography.titleLarge,
                    modifier = Modifier.padding(bottom = 8.dp),
                )
                EntryKind.entries.forEach { kind ->
                    CategoryCard(
                        kind.label(),
                        kind.icon(),
                        {
                            showAdd = false
                            onAdd(kind)
                        },
                    )
                }
            }
        }
}

@Composable
internal fun CategoryCard(title: String, icon: Int, onClick: () -> Unit) {
    GlassPanel(Modifier.fillMaxWidth(), onClick) {
        Row(
            Modifier.fillMaxWidth()
                .heightIn(min = 56.dp)
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Box(
                Modifier.size(30.dp)
                    .background(MaterialTheme.colorScheme.surfaceVariant, RoundedCornerShape(8.dp)),
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    painterResource(icon),
                    null,
                    Modifier.size(21.dp),
                    tint = MaterialTheme.colorScheme.onSurface,
                )
            }
            Text(
                title,
                Modifier.weight(1f),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Normal,
            )
            Icon(
                painterResource(R.drawable.ic_chevron),
                null,
                Modifier.size(20.dp),
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}

internal fun EntryKind.icon(): Int =
    when (this) {
        EntryKind.PASSWORD -> R.drawable.ic_password
        EntryKind.CARD -> R.drawable.ic_card
        EntryKind.NOTE -> R.drawable.ic_note
    }

@Composable
internal fun VaultRoute(
    onBack: () -> Unit,
    onOpen: (EntryKind, VaultEntry) -> Unit,
    onAdd: (EntryKind) -> Unit,
    viewModel: VaultViewModel = koinViewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val filter by viewModel.filter.collectAsStateWithLifecycle()
    val kind = viewModel.kind ?: EntryKind.PASSWORD
    ScreenFrame(
        kind.label(),
        onBack = onBack,
        floatingActionButton = {
            FloatingActionButton(
                onClick = { onAdd(kind) },
                shape = androidx.compose.foundation.shape.CircleShape,
                containerColor = MaterialTheme.colorScheme.primaryContainer,
            ) {
                Icon(painterResource(R.drawable.ic_add), stringResource(R.string.action_add))
            }
        },
    ) { padding ->
        LazyColumn(
            Modifier.fillMaxSize().padding(padding).imePadding(),
            contentPadding = PaddingValues(horizontal = 20.dp, vertical = 16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            item(key = "search") {
                OutlinedTextField(
                    filter.query,
                    viewModel::query,
                    Modifier.fillMaxWidth(),
                    placeholder = { Text(stringResource(R.string.vault_search)) },
                    leadingIcon = { Icon(painterResource(R.drawable.ic_search), null) },
                    shape = androidx.compose.foundation.shape.RoundedCornerShape(24.dp),
                    singleLine = true,
                    colors =
                        OutlinedTextFieldDefaults.colors(
                            unfocusedContainerColor =
                                MaterialTheme.colorScheme.surface.copy(alpha = 0.8f),
                            unfocusedBorderColor = MaterialTheme.colorScheme.outlineVariant,
                        ),
                )
            }
            when {
                state.loading ->
                    item {
                        Box(
                            Modifier.fillMaxWidth().padding(40.dp),
                            contentAlignment = Alignment.Center,
                        ) {
                            CircularProgressIndicator()
                        }
                    }
                state.error != null ->
                    item {
                        Text(state.error!!, color = MaterialTheme.colorScheme.error)
                        TextButton(viewModel::retry) { Text(stringResource(R.string.action_retry)) }
                    }
                state.entries.isEmpty() ->
                    item {
                        Text(
                            stringResource(
                                if (filter.query.isBlank()) R.string.vault_empty
                                else R.string.search_empty,
                            ),
                            Modifier.padding(vertical = 40.dp),
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                else ->
                    items(state.entries, key = { it.id }) { entry ->
                        GlassPanel(Modifier.fillMaxWidth(), { onOpen(kind, entry) }) {
                            Row(
                                Modifier.fillMaxWidth().padding(20.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(16.dp),
                            ) {
                                Icon(
                                    painterResource(kind.icon()),
                                    null,
                                    Modifier.size(24.dp),
                                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                )
                                Column(
                                    Modifier.weight(1f),
                                    verticalArrangement = Arrangement.spacedBy(5.dp),
                                ) {
                                    Text(
                                        entry.title,
                                        style = MaterialTheme.typography.titleMedium,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis,
                                    )
                                    Text(
                                        when (entry) {
                                            is PasswordEntry -> entry.username.ifBlank { entry.url }
                                            is CardEntry -> "•••• " + entry.number.takeLast(4)
                                            is NoteEntry -> entry.content.take(100)
                                        },
                                        style = MaterialTheme.typography.bodyMedium,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    )
                                }
                                Icon(
                                    painterResource(R.drawable.ic_chevron),
                                    null,
                                    Modifier.size(20.dp),
                                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                )
                            }
                        }
                    }
            }
            item { Spacer(Modifier.height(88.dp)) }
        }
    }
}

@Composable
internal fun EntryKind.label(): String =
    when (this) {
        EntryKind.PASSWORD -> stringResource(R.string.category_passwords)
        EntryKind.CARD -> stringResource(R.string.category_cards)
        EntryKind.NOTE -> stringResource(R.string.category_notes)
    }
