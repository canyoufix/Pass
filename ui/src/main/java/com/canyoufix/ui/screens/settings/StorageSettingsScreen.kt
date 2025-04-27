package com.canyoufix.ui.screens.settings

import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.TextButton
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import androidx.navigation.NavController
import com.canyoufix.crypto.CryptoManager
import com.canyoufix.crypto.MasterPasswordManager
import com.canyoufix.crypto.SecurePrefsManager
import com.canyoufix.data.dto.ExportData
import com.canyoufix.data.json.DataExportImportManager
import com.canyoufix.data.json.ImportStatus
import com.canyoufix.data.repository.CardRepository
import com.canyoufix.data.repository.NoteRepository
import com.canyoufix.data.repository.PasswordRepository
import com.canyoufix.ui.components.password.PasswordDialog
import com.canyoufix.ui.components.password.PasswordDialogWithRadioButtons
import com.canyoufix.ui.components.password.PasswordTextField
import kotlinx.coroutines.launch
import org.koin.compose.koinInject

@Composable
fun StorageSettingsScreen(navController: NavController) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    val noteRepository: NoteRepository = koinInject()
    val cardRepository: CardRepository = koinInject()
    val passwordRepository: PasswordRepository = koinInject()

    val prefsManager = remember { SecurePrefsManager(context) }
    val cryptoManager = remember { CryptoManager }
    val masterPasswordManager = remember { MasterPasswordManager(prefsManager, cryptoManager) }

    val dataExportImportManager = remember {
        DataExportImportManager(
            noteRepository = noteRepository,
            cardRepository = cardRepository,
            passwordRepository = passwordRepository,
            securePrefsManager = prefsManager
        )
    }

    var showPasswordDialog by remember { mutableStateOf(false) }
    var showImportPasswordDialog by remember { mutableStateOf(false) }
    var passwordInputForExport by remember { mutableStateOf("") }  // Изменяем имя переменной
    var passwordInputForImport by remember { mutableStateOf("") }  // Новая переменная для пароля импорта
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var exportReady by remember { mutableStateOf(false) }
    var exportEncrypted by remember { mutableStateOf(true) }
    var importType by remember { mutableStateOf("clean") } // "clean" или "merge"

    val exportLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.CreateDocument("application/json"),
        onResult = { uri ->
            if (uri != null) {
                scope.launch {
                    val result = dataExportImportManager.exportAll(context, uri, exportEncrypted)
                    result.onSuccess {
                        Toast.makeText(context, "Экспорт завершён!", Toast.LENGTH_LONG).show()
                    }.onFailure { error ->
                        Toast.makeText(
                            context,
                            "Ошибка экспорта: ${error.localizedMessage}",
                            Toast.LENGTH_LONG
                        ).show()
                    }
                }
            }
        }
    )

    var currentExportData by remember { mutableStateOf<ExportData?>(null) }

    // Новое состояние для диалога импорта зашифрованных данных
    var showEncryptedImportDialog by remember { mutableStateOf(false) }

    // Новый диалог импорта
    if (showEncryptedImportDialog) {
        PasswordDialog(
            title = "Импорт зашифрованных данных",
            passwordInput = passwordInputForImport,  // Используем новую переменную
            onPasswordChange = { passwordInputForImport = it },  // Обновляем состояние для импорта
            errorMessage = errorMessage,
            onConfirm = { password ->
                if (password.isBlank()) {
                    errorMessage = "Введите пароль"
                    return@PasswordDialog
                }

                scope.launch {
                    try {
                        currentExportData?.let { data ->
                            if (importType == "clean") {
                                // dataExportImportManager.clearDatabase()
                            }
                            dataExportImportManager.importEncryptedData(data, password)
                            Toast.makeText(context, "Импорт завершён", Toast.LENGTH_SHORT).show()
                        }
                        showEncryptedImportDialog = false
                    } catch (e: Exception) {
                        errorMessage = "Ошибка: ${e.localizedMessage}"
                    }
                }
            },
            onDismiss = {
                showEncryptedImportDialog = false
                passwordInputForImport = ""  // Сбрасываем пароль при закрытии
                errorMessage = null
            }
        )
    }

    // Обновленный обработчик в importLauncher
    val importLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument(),
        onResult = { uri ->
            if (uri != null) {
                scope.launch {
                    when (val status = dataExportImportManager.checkImport(context, uri, importType)) {
                        is ImportStatus.NotEncryptedClean -> {
                            val data = dataExportImportManager.parseExportData(context, uri)
                            // dataExportImportManager.clearDatabase()
                            dataExportImportManager.importNotEncryptedData(data)
                            Toast.makeText(context, "Импорт (clean) завершён", Toast.LENGTH_SHORT).show()
                        }
                        is ImportStatus.NotEncryptedMerge -> {
                            val data = dataExportImportManager.parseExportData(context, uri)
                            dataExportImportManager.importNotEncryptedData(data)
                            Toast.makeText(context, "Импорт (merge) завершён", Toast.LENGTH_SHORT).show()
                        }
                        is ImportStatus.EncryptedCleanNeeded -> {
                            currentExportData = dataExportImportManager.parseExportData(context, uri)
                            showEncryptedImportDialog = true
                        }
                        is ImportStatus.EncryptedMergeNeeded -> {
                            currentExportData = dataExportImportManager.parseExportData(context, uri)
                            showEncryptedImportDialog = true
                        }
                        is ImportStatus.Error -> {
                            Toast.makeText(context, status.message, Toast.LENGTH_LONG).show()
                        }
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
            onClick = { showPasswordDialog = true },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Экспорт данных")
        }

        Button(
            onClick = {
                showImportPasswordDialog = true
                passwordInputForImport = ""  // Сбрасываем переменную для импорта
                errorMessage = null
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Импорт данных")
        }
    }

    if (showPasswordDialog) {
        PasswordDialogWithRadioButtons(
            title = "Подтверждение пароля",
            onConfirm = { password ->
                if (password.isBlank()) {
                    errorMessage = "Введите пароль"
                    return@PasswordDialogWithRadioButtons
                }

                if (masterPasswordManager.verifyMasterPassword(password)) {
                    showPasswordDialog = false
                    passwordInputForExport = ""  // Сбрасываем переменную для экспорта
                    errorMessage = null
                    exportReady = true
                } else {
                    errorMessage = "Неверный пароль!"
                }
            },
            onDismiss = {
                showPasswordDialog = false
                passwordInputForExport = ""  // Сбрасываем переменную для экспорта
                errorMessage = null
            },
            errorMessage = errorMessage,
            passwordInput = passwordInputForExport,  // Используем переменную для экспорта
            onPasswordChange = { passwordInputForExport = it },  // Обновляем состояние для экспорта
            radioOptions = listOf(
                "Зашифрованный" to exportEncrypted,
                "Незашифрованный" to !exportEncrypted
            ),
            onRadioOptionChange = { selected ->
                exportEncrypted = selected == "Зашифрованный"
            },
            confirmText = "Подтвердить",
            dismissText = "Отмена"
        )
    }

    if (showImportPasswordDialog) {
        PasswordDialogWithRadioButtons(
            title = "Импорт данных",
            onConfirm = { password ->
                if (password.isBlank()) {
                    errorMessage = "Введите пароль"
                    return@PasswordDialogWithRadioButtons
                }

                if (!masterPasswordManager.verifyMasterPassword(password)) {
                    errorMessage = "Неверный пароль!"
                    return@PasswordDialogWithRadioButtons
                }

                showImportPasswordDialog = false
                passwordInputForImport = ""  // Сбрасываем переменную для импорта
                errorMessage = null
                importLauncher.launch(arrayOf("application/json"))
            },
            onDismiss = {
                showImportPasswordDialog = false
                passwordInputForImport = ""  // Сбрасываем переменную для импорта
                errorMessage = null
            },
            errorMessage = errorMessage,
            passwordInput = passwordInputForImport,  // Используем переменную для импорта
            onPasswordChange = { passwordInputForImport = it },  // Обновляем состояние для импорта
            radioOptions = listOf(
                "Очистить и импортировать" to (importType == "clean"),
                "Добавить к существующим" to (importType == "merge")
            ),
            onRadioOptionChange = { selected ->
                importType = if (selected == "Очистить и импортировать") "clean" else "merge"
            },
            confirmText = "Продолжить",
            dismissText = "Отмена"
        )
    }


    LaunchedEffect(exportReady) {
        if (exportReady) {
            exportReady = false
            exportLauncher.launch("secure_export_${System.currentTimeMillis()}.json")
        }
    }
}
