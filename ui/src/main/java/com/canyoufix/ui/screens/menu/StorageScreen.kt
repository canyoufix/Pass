package com.canyoufix.ui.screens.menu

import android.app.Activity
import android.content.Intent
import android.util.Log
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.FileCopy
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.canyoufix.crypto.CryptoManager
import com.canyoufix.crypto.SessionAESKeyHolder
import com.canyoufix.data.viewmodel.CardViewModel
import com.canyoufix.data.viewmodel.NoteViewModel
import com.canyoufix.data.viewmodel.PasswordViewModel
import com.canyoufix.ui.R
import com.canyoufix.ui.components.AddEntryBottomSheet
import com.canyoufix.ui.components.CustomFAB
import com.canyoufix.ui.components.card.MenuCard
import com.canyoufix.ui.components.file.DeleteFileDialog
import com.canyoufix.ui.components.file.EncryptFileDialog
import com.canyoufix.ui.components.file.FileActionsDialog
import com.canyoufix.ui.utils.FileUtils.addTimestampToFileName
import com.canyoufix.ui.utils.FileUtils.getFileNameFromUri
import java.io.File

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StorageScreen(
    navController: NavController,
    passwordViewModel: PasswordViewModel,
    cardViewModel: CardViewModel,
    noteViewModel: NoteViewModel
) {
    val context = LocalContext.current
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    var showBottomSheet by remember { mutableStateOf(false) }
    var showFileActionsDialog by remember { mutableStateOf(false) }

    var showDecryptFileDialog by remember { mutableStateOf(false) }
    var showDeleteFileDialog by remember { mutableStateOf(false) }
    var fileToDecrypt by remember { mutableStateOf<File?>(null) }

    // Launcher для выбора файла и шифрования (как у тебя)
    val encryptLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            val uri = result.data?.data ?: return@rememberLauncherForActivityResult
            val inputStream = context.contentResolver.openInputStream(uri) ?: return@rememberLauncherForActivityResult
            val fileName = getFileNameFromUri(context, uri) ?: "unknown"

            val encryptedDir = File(context.filesDir, "EncryptedFiles")
            if (!encryptedDir.exists()) encryptedDir.mkdirs()

            val outputFile = File(encryptedDir, "$fileName.enc")

            val key = SessionAESKeyHolder.key
            val success = CryptoManager.encryptFile(inputStream, outputFile, key)

            if (success) {

                Toast.makeText(context, "Файл зашифрован: ${outputFile.name}", Toast.LENGTH_LONG).show()
            } else {
                Toast.makeText(context, "Ошибка при шифровании файла", Toast.LENGTH_LONG).show()
            }
        }
    }

    // Launcher для сохранения расшифрованного файла (CreateDocument)
    val decryptLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.CreateDocument("application/octet-stream")
    ) { uri ->
        val file = fileToDecrypt ?: run {
            Log.d("Crypto", "3a - No file selected")
            return@rememberLauncherForActivityResult
        }

        if (uri == null) {
            return@rememberLauncherForActivityResult
        }

        try {
            context.contentResolver.openOutputStream(uri)?.use { outputStream ->
                val key = SessionAESKeyHolder.key
                val success = CryptoManager.decryptFile(file, outputStream, key)

                Toast.makeText(
                    context,
                    if (success) "Файл расшифрован: ${file.name.removeSuffix(".enc")}" else "Ошибка при расшифровке файла",
                    Toast.LENGTH_LONG
                ).show()
            } ?: run {
                Toast.makeText(context, "Не удалось открыть поток для записи", Toast.LENGTH_LONG).show()
            }
        } catch (e: Exception) {
            Toast.makeText(context, "Ошибка: ${e.message}", Toast.LENGTH_LONG).show()
        } finally {
            fileToDecrypt = null
        }
    }

    // Запуск диалога выбора файла для расшифровки — при выборе запускаем сохранение
    LaunchedEffect(fileToDecrypt) {
        fileToDecrypt?.let {
            val baseName = it.name.removeSuffix(".enc")
            val suggestedName = addTimestampToFileName(baseName)
            decryptLauncher.launch(suggestedName)
            showDecryptFileDialog = false
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        MenuCard(
            title = "Пароли",
            icon = painterResource(id = R.drawable.ic_password),
            onClick = { navController.navigate("password") }
        )
        Spacer(modifier = Modifier.height(16.dp))

        MenuCard(
            title = "Карты",
            icon = painterResource(id = R.drawable.ic_card),
            onClick = { navController.navigate("card") }
        )
        Spacer(modifier = Modifier.height(16.dp))

        MenuCard(
            title = "Защищенные заметки",
            icon = painterResource(id = R.drawable.ic_note),
            onClick = { navController.navigate("note") }
        )
    }

    Box(modifier = Modifier.fillMaxSize()) {
        CustomFAB(
            icon = Icons.Default.FileCopy,
            contentDescription = "Файлы",
            alignment = Alignment.BottomStart,
            containerColor = MaterialTheme.colorScheme.primary,
            contentColor = MaterialTheme.colorScheme.onPrimary,
            onClick = { showFileActionsDialog = true }
        )

        CustomFAB(
            icon = Icons.Default.Add,
            contentDescription = "Добавить",
            alignment = Alignment.BottomEnd,
            containerColor = MaterialTheme.colorScheme.primary,
            contentColor = MaterialTheme.colorScheme.onPrimary,
            onClick = { showBottomSheet = true }
        )
    }

    if (showBottomSheet) {
        AddEntryBottomSheet(
            sheetState = sheetState,
            onDismiss = { showBottomSheet = false },
            passwordViewModel = passwordViewModel,
            cardViewModel = cardViewModel,
            noteViewModel = noteViewModel
        )
    }

    if (showFileActionsDialog) {
        FileActionsDialog(
            onDismiss = { showFileActionsDialog = false },
            onEncryptFile = {
                val intent = Intent(Intent.ACTION_OPEN_DOCUMENT).apply {
                    addCategory(Intent.CATEGORY_OPENABLE)
                    type = "*/*"
                }
                encryptLauncher.launch(intent)
                showFileActionsDialog = false
            },
            onDecryptFile = {
                val encryptedDir = File(context.filesDir, "EncryptedFiles")
                val files = encryptedDir.listFiles()?.filter { it.name.endsWith(".enc") }

                if (files.isNullOrEmpty()) {
                    Toast.makeText(context, "Нет зашифрованных файлов", Toast.LENGTH_LONG).show()
                } else {
                    showDecryptFileDialog = true
                }
                showFileActionsDialog = false
            },
            onDeleteFile = {
                val encryptedDir = File(context.filesDir, "EncryptedFiles")
                val files = encryptedDir.listFiles()?.filter { it.name.endsWith(".enc") }

                if (files.isNullOrEmpty()) {
                    Toast.makeText(context, "Нет зашифрованных файлов", Toast.LENGTH_LONG).show()
                } else {
                    showDeleteFileDialog = true
                }
                showFileActionsDialog = false
            }
        )
    }

    if (showDecryptFileDialog) {
        EncryptFileDialog(
            context = context,
            onFileSelected = { file ->
                fileToDecrypt = file
            },
            onDismiss = {
                showDecryptFileDialog = false
            }
        )
    }

    if (showDeleteFileDialog) {
        DeleteFileDialog(
            context = context,
            onFileDeleted = { file ->
                showDeleteFileDialog = false
            },
            onDismiss = {
                showDeleteFileDialog = false
            }
        )
    }
}