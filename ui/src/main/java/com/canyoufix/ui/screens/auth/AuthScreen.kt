package com.canyoufix.ui.screens.auth

import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.canyoufix.crypto.CryptoManager
import com.canyoufix.crypto.MasterPasswordManager
import com.canyoufix.crypto.SecurePrefsManager
import com.canyoufix.data.database.DatabaseManager
import com.canyoufix.ui.R
import com.canyoufix.ui.components.password.PasswordTextField
import kotlinx.coroutines.launch

@Composable
fun AuthScreen(
    onSuccess: () -> Unit,
    onFail: (String) -> Unit,
    onResetComplete: () -> Unit
) {
    val context = LocalContext.current
    var password by remember { mutableStateOf("") }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    val prefsManager = remember { SecurePrefsManager(context) }
    val databaseManager = remember { DatabaseManager(context) }
    val coroutineScope = rememberCoroutineScope()

    // Управление фокусом
    val focusRequester = remember { FocusRequester() }
    val focusManager = LocalFocusManager.current
    val keyboardController = LocalSoftwareKeyboardController.current

    val masterPasswordManager = MasterPasswordManager(prefsManager, CryptoManager)

    // Устанавливаем фокус при запуске экрана
    LaunchedEffect(Unit) {
        focusRequester.requestFocus()
        keyboardController?.show()
    }

    Scaffold(
        modifier = Modifier
            .fillMaxSize()
            .clickable(indication = null, interactionSource = remember { MutableInteractionSource() }) {
                // При нажатии на фон:
                keyboardController?.hide() // Скрываем клавиатуру
                focusManager.clearFocus() // Убираем фокус с текстового поля
            },
        content = { paddingValues ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .padding(horizontal = 32.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Top
            ) {
                Spacer(modifier = Modifier.height(80.dp))

                Icon(
                    imageVector = Icons.Default.Lock,
                    contentDescription = "Lock Icon",
                    modifier = Modifier.size(96.dp),
                    tint = MaterialTheme.colorScheme.primary
                )

                Spacer(modifier = Modifier.height(32.dp))

                Text(
                    text = stringResource(R.string.authorization),
                    style = MaterialTheme.typography.headlineSmall
                )

                Spacer(modifier = Modifier.height(16.dp))

                PasswordTextField(
                    password = password,
                    onPasswordChange = { password = it },
                    label = stringResource(R.string.master_password),
                    modifier = Modifier
                        .fillMaxWidth(),
                    focusRequester = focusRequester
                )

                Spacer(modifier = Modifier.height(24.dp))

                Button(
                    onClick = {
                        coroutineScope.launch {
                            when (val result = masterPasswordManager.authenticate(password)) {
                                MasterPasswordManager.AuthResult.Success -> onSuccess()
                                MasterPasswordManager.AuthResult.FakeSuccess -> onSuccess()
                                is MasterPasswordManager.AuthResult.Failure -> {
                                    errorMessage = result.error
                                    onFail(errorMessage!!)
                                }
                            }
                        }
                    },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(stringResource(R.string.login))
                }

                Spacer(modifier = Modifier.height(16.dp))

                val successMessage = stringResource(R.string.reset_all_complete)
                val errorTemplate = stringResource(R.string.reset_error)
                OutlinedButton(
                    onClick = {

                        coroutineScope.launch {
                            try {
                                databaseManager.clearAllData()
                                prefsManager.clearAllData()
                                onResetComplete()
                                errorMessage = successMessage
                            } catch (e: Exception) {
                                errorMessage = String.format(errorTemplate, e.message ?: "")
                            }
                            errorMessage?.let { onFail(it) }
                        }
                    },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(stringResource(R.string.reset_all))
                }

                errorMessage?.let {
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = it,
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            }
        }
    )
}