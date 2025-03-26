package com.canyoufix.ui.screens.auth

import android.content.Context
import android.util.Log
import android.widget.Toast
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.canyoufix.crypto.KeyGenerator
import com.canyoufix.crypto.SecurePrefsManager
import com.canyoufix.crypto.SecurityConfig
import com.canyoufix.ui.utils.rememberPasswordVisibilityState
import kotlinx.coroutines.launch
import org.koin.androidx.compose.koinViewModel

@Composable
fun SetupScreen(onSetupComplete: () -> Unit) {
    val context = LocalContext.current
    var password by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    val prefsManager = remember { SecurePrefsManager(context) }
    val passwordVisibility1 = rememberPasswordVisibilityState()
    val passwordVisibility2 = rememberPasswordVisibilityState()

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

                Spacer(modifier = Modifier.height(24.dp))

                Text(
                    text = "Установите мастер-пароль",
                    style = MaterialTheme.typography.titleLarge
                )

                Spacer(modifier = Modifier.height(24.dp))

                OutlinedTextField(
                    value = password,
                    onValueChange = { password = it },
                    label = { Text("Мастер-пароль") },
                    visualTransformation = passwordVisibility1.visualTransformation,
                    trailingIcon = {
                        IconButton(onClick = passwordVisibility1.toggle) {
                            Icon(
                                imageVector = passwordVisibility1.icon,
                                contentDescription = passwordVisibility1.description
                            )
                        }
                    },
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next),
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(16.dp))

                OutlinedTextField(
                    value = confirmPassword,
                    onValueChange = { confirmPassword = it },
                    label = { Text("Подтверждение пароля") },
                    visualTransformation = passwordVisibility2.visualTransformation,
                    trailingIcon = {
                        IconButton(onClick = passwordVisibility2.toggle) {
                            Icon(
                                imageVector = passwordVisibility2.icon,
                                contentDescription = passwordVisibility2.description
                            )
                        }
                    },
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                errorMessage?.let {
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = it,
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodyMedium
                    )
                }

                Spacer(modifier = Modifier.height(24.dp))

                Button(
                    onClick = {
                        when {
                            password.isEmpty() || confirmPassword.isEmpty() -> {
                                errorMessage = "Пароли не могут быть пустыми!"
                            }
                            password != confirmPassword -> {
                                errorMessage = "Пароли не совпадают!"
                            }
                            else -> {
                                val salt = KeyGenerator.generateSalt()
                                val key = KeyGenerator.deriveKeyFromPassword(password, salt)
                                val testBlock = KeyGenerator.encrypt(SecurityConfig.TEST_BLOCK, key)

                                prefsManager.saveSalt(salt)
                                prefsManager.saveEncryptedTestBlock(testBlock)

                                onSetupComplete()
                            }
                        }
                    },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Сохранить и продолжить")
                }
            }
        }
    )
}
