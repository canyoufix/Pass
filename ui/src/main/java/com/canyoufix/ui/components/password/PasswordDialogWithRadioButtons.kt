package com.canyoufix.ui.components.password

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun PasswordDialogWithRadioButtons(
    title: String,
    onConfirm: (String) -> Unit,
    onDismiss: () -> Unit,
    errorMessage: String?,
    passwordInput: String,
    onPasswordChange: (String) -> Unit,
    radioOptions: List<Pair<String, Boolean>>,
    onRadioOptionChange: (String) -> Unit,
    confirmText: String,
    dismissText: String
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(title) },
        text = {
            Column {
                Text("Введите мастер-пароль")
                Spacer(modifier = Modifier.height(8.dp))

                PasswordTextField(
                    password = passwordInput,
                    onPasswordChange = onPasswordChange,
                    label = "Мастер-пароль",
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(16.dp))

                radioOptions.forEach { (label, selected) ->
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        RadioButton(
                            selected = selected,
                            onClick = { onRadioOptionChange(label) }
                        )
                        Text(label, modifier = Modifier.padding(start = 8.dp))
                    }
                }

                errorMessage?.let {
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = it,
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodySmall
                    )
                }
            }
        },
        confirmButton = {
            TextButton(onClick = { onConfirm(passwordInput) }) {
                Text(confirmText)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(dismissText)
            }
        }
    )
}
