package com.canyoufix.ui.components.file

import android.content.Context
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.canyoufix.ui.components.card.FileListCard
import java.io.File

@Composable
fun EncryptFileDialog(
    context: Context,
    onFileSelected: (File) -> Unit,
    onDismiss: () -> Unit
) {
    val encryptedDir = File(context.filesDir, "EncryptedFiles")
    val files = encryptedDir.listFiles()?.filter { it.name.endsWith(".enc") } ?: emptyList()

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Выберите файл для расшифровки") },
        text = {
            Column(modifier = Modifier.heightIn(max = 300.dp)) {
                if (files.isEmpty()) {
                    Text("Нет зашифрованных файлов")
                } else {
                    files.forEach { file ->
                        FileListCard(
                            file = file,
                            onClick = {
                                onFileSelected(file)
                                onDismiss()
                            }
                        )
                    }
                }
            }
        },
        confirmButton = {},
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Отмена")
            }
        }
    )
}