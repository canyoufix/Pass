package com.canyoufix.ui.screens.auth

import android.content.Context
import android.util.Log
import android.widget.Toast
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.canyoufix.crypto.KeyGenerator
import com.canyoufix.crypto.SecurePrefsManager
import com.canyoufix.crypto.SecurityConfig
import com.canyoufix.data.database.DatabaseManager
import com.canyoufix.ui.utils.rememberPasswordVisibilityState
import kotlinx.coroutines.launch
import org.koin.androidx.compose.koinViewModel

@OptIn(ExperimentalMaterial3Api::class)
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

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        content = { paddingValues ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .padding(horizontal = 32.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Top // Изменено на Top
            ) {
                Spacer(modifier = Modifier.height(80.dp)) // Добавлен верхний отступ

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
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(24.dp))

                Button(
                    onClick = {
                        val salt = prefsManager.getSalt()
                        val encryptedTestBlock = prefsManager.getEncryptedTestBlock()

                        if (salt != null && encryptedTestBlock != null) {
                            val key = KeyGenerator.deriveKeyFromPassword(password, salt)
                            val decryptedBlock = KeyGenerator.decrypt(encryptedTestBlock, key)

                            if (decryptedBlock == SecurityConfig.TEST_BLOCK) {
                                onSuccess()
                            } else {
                                errorMessage = "Неверный пароль!"
                                onFail(errorMessage!!)
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
