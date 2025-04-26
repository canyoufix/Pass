package com.canyoufix.ui.screens.settings

import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Storage
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.TextButton
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.canyoufix.crypto.CryptoManager
import com.canyoufix.crypto.SecurePrefsManager
import com.canyoufix.crypto.SecurityConfig
import com.canyoufix.crypto.SessionKeyHolder
import com.canyoufix.data.database.AppDatabase
import com.canyoufix.data.json.ExportManager
import com.canyoufix.data.repository.CardRepository
import com.canyoufix.data.repository.NoteRepository
import com.canyoufix.data.repository.PasswordRepository
import com.canyoufix.ui.utils.rememberPasswordVisibilityState
import kotlinx.coroutines.launch
import org.koin.androidx.compose.koinViewModel
import org.koin.compose.koinInject

@Composable
fun StorageSettingsScreen(navController: NavController) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    val noteRepository: NoteRepository = koinInject()
    val cardRepository: CardRepository = koinInject()
    val passwordRepository: PasswordRepository = koinInject()

    val prefsManager = remember { SecurePrefsManager(context) }

    val exportManager = remember {
        ExportManager(
            noteRepository = noteRepository,
            cardRepository = cardRepository,
            passwordRepository = passwordRepository,
            securePrefsManager = prefsManager
        )
    }

    var showPasswordDialog by remember { mutableStateOf(false) }
    var passwordInput by remember { mutableStateOf("") }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var exportReady by remember { mutableStateOf(false) }

    // Для отображения/скрытия пароля
    val passwordVisibility = rememberPasswordVisibilityState()

    val exportLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.CreateDocument("application/json"),
        onResult = { uri ->
            if (uri != null) {
                scope.launch {
                    val result = exportManager.exportAll(context, uri)
                    result.onSuccess {
                        Toast.makeText(context, "Экспорт завершён!", Toast.LENGTH_LONG).show()
                    }.onFailure { error ->
                        Toast.makeText(context, "Ошибка экспорта: ${error.localizedMessage}", Toast.LENGTH_LONG).show()
                    }
                }
            }
        }
    )

    Column(
        modifier = Modifier
            .padding(16.dp)
            .fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Button(
            onClick = {
                showPasswordDialog = true
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Экспорт данных")
        }

        Button(
            onClick = {
                // Импорт позже
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Импорт данных")
        }
    }

    if (showPasswordDialog) {
        AlertDialog(
            onDismissRequest = {
                showPasswordDialog = false
                passwordInput = ""
                errorMessage = null
            },
            title = { Text("Подтверждение пароля") },
            text = {
                Column {
                    Text("Введите мастер-пароль для подтверждения экспорта")
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = passwordInput,
                        onValueChange = { passwordInput = it },
                        label = { Text("Мастер-пароль") },
                        visualTransformation = passwordVisibility.visualTransformation,
                        trailingIcon = {
                            IconButton(onClick = passwordVisibility.toggle) {
                                Icon(
                                    imageVector = passwordVisibility.icon,
                                    contentDescription = passwordVisibility.description
                                )
                            }
                        },
                        keyboardOptions = KeyboardOptions.Default.copy(
                            imeAction = ImeAction.Done
                        ),
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                    if (errorMessage != null) {
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = errorMessage!!,
                            color = MaterialTheme.colorScheme.error,
                            style = MaterialTheme.typography.bodySmall
                        )
                    }
                }
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        val salt = prefsManager.getSalt()
                        val encryptedTestBlock = prefsManager.getEncryptedTestBlock()

                        if (salt != null && encryptedTestBlock != null) {
                            val key = CryptoManager.deriveKeyFromPassword(passwordInput, salt)

                            val decryptedRealBlock = try {
                                CryptoManager.decrypt(encryptedTestBlock, key)
                            } catch (e: Exception) {
                                null
                            }

                            if (decryptedRealBlock == SecurityConfig.TEST_BLOCK) {
                                SessionKeyHolder.key = key
                                showPasswordDialog = false
                                passwordInput = ""
                                errorMessage = null
                                exportReady = true
                            } else {
                                errorMessage = "Неверный пароль!"
                            }
                        } else {
                            errorMessage = "Ошибка загрузки данных!"
                        }
                    }
                ) {
                    Text("Подтвердить")
                }
            },
            dismissButton = {
                TextButton(
                    onClick = {
                        showPasswordDialog = false
                        passwordInput = ""
                        errorMessage = null
                    }
                ) {
                    Text("Отмена")
                }
            }
        )
    }

    LaunchedEffect(exportReady) {
        if (exportReady) {
            exportReady = false
            exportLauncher.launch("secure_export_${System.currentTimeMillis()}.json")
        }
    }
}






