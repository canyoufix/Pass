package com.canyoufix.ui.components.file

import android.content.Context
import android.widget.Toast
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.canyoufix.ui.components.card.FileListCard
import com.canyoufix.ui.utils.FileUtils
import java.io.File

@Composable
fun DeleteFileDialog(
    context: Context,
    onFileDeleted: (File) -> Unit,
    onDismiss: () -> Unit
) {
    val encryptedDir = File(context.filesDir, "EncryptedFiles")
    val files = encryptedDir.listFiles()?.filter { it.name.endsWith(".enc") } ?: emptyList()

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Выберите файл для удаления") },
        text = {
            if (files.isEmpty()) {
                Text("Нет зашифрованных файлов")
            } else {
                LazyColumn(
                    modifier = Modifier
                        .heightIn(max = 300.dp)
                ) {
                    items(files) { file ->
                        FileListCard(
                            file = file,
                            onClick = {
                                val deleted = FileUtils.deleteEncryptedFile(context, file.name)
                                if (deleted) {
                                    Toast.makeText(
                                        context,
                                        "Файл удалён: ${file.name}",
                                        Toast.LENGTH_SHORT
                                    ).show()
                                    onFileDeleted(file)
                                } else {
                                    Toast.makeText(
                                        context,
                                        "Ошибка при удалении: ${file.name}",
                                        Toast.LENGTH_SHORT
                                    ).show()
                                }
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

