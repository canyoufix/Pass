package com.canyoufix.ui.screens.auth

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
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
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import com.canyoufix.crypto.KeyGenerator
import com.canyoufix.crypto.SecurePrefsManager
import com.canyoufix.crypto.SecurityConfig
import com.canyoufix.data.database.DatabaseManager
import com.canyoufix.ui.utils.rememberPasswordVisibilityState
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
    val passwordVisibility = rememberPasswordVisibilityState()

    // Управление фокусом
    val focusRequester = remember { FocusRequester() }
    val keyboardController = LocalSoftwareKeyboardController.current

    // Устанавливаем фокус при запуске экрана
    LaunchedEffect(Unit) {
        focusRequester.requestFocus()
        keyboardController?.show()
    }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
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
                    text = "Аутентификация",
                    style = MaterialTheme.typography.headlineSmall
                )

                Spacer(modifier = Modifier.height(16.dp))

                OutlinedTextField(
                    value = password,
                    onValueChange = { password = it },
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
                    modifier = Modifier
                        .fillMaxWidth()
                        .focusRequester(focusRequester)
                )

                Spacer(modifier = Modifier.height(24.dp))

                Button(
                    onClick = {
                        val salt = prefsManager.getSalt()
                        val encryptedTestBlock = prefsManager.getEncryptedTestBlock()
                        val fakeEncryptedTestBlock = prefsManager.getFakeEncryptedTestBlock()

                        if (salt != null && encryptedTestBlock != null) {
                            val key = KeyGenerator.deriveKeyFromPassword(password, salt)

                            val decryptedRealBlock = try {
                                KeyGenerator.decrypt(encryptedTestBlock, key)
                            } catch (e: Exception) {
                                null
                            }

                            val decryptedFakeBlock = try {
                                fakeEncryptedTestBlock?.let { KeyGenerator.decrypt(it, key) }
                            } catch (e: Exception) {
                                null
                            }

                            when {
                                decryptedRealBlock == SecurityConfig.TEST_BLOCK -> {
                                    onSuccess()
                                }
                                decryptedFakeBlock == SecurityConfig.FAKE_TEST_BLOCK -> {
                                    onSuccess()

                                }
                                else -> {
                                    errorMessage = "Неверный пароль!"
                                    onFail(errorMessage!!)
                                }
                            }
                        }
                    },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Войти")
                }

                Spacer(modifier = Modifier.height(16.dp))

                OutlinedButton(
                    onClick = {
                        coroutineScope.launch {
                            try {
                                databaseManager.clearAllData()
                                prefsManager.clearAllData()
                                onResetComplete()
                                errorMessage = "Все данные были сброшены!"
                            } catch (e: Exception) {
                                errorMessage = "Ошибка при сбросе данных: ${e.message}"
                            }
                            errorMessage?.let { onFail(it) }
                        }
                    },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Сбросить все данные")
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