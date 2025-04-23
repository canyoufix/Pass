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
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
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
import com.canyoufix.ui.utils.rememberPasswordVisibilityState

@Composable
fun SetupScreen(onSetupComplete: () -> Unit) {
    val context = LocalContext.current
    var password by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }
    var fakePassword by remember { mutableStateOf("") }
    var confirmFakePassword by remember { mutableStateOf("") }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    val prefsManager = remember { SecurePrefsManager(context) }

    val passwordVisibility1 = rememberPasswordVisibilityState()
    val passwordVisibility2 = rememberPasswordVisibilityState()
    val passwordVisibility3 = rememberPasswordVisibilityState()
    val passwordVisibility4 = rememberPasswordVisibilityState()

    val focusRequester = remember { FocusRequester() }
    val keyboardController = LocalSoftwareKeyboardController.current

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
                    modifier = Modifier
                        .fillMaxWidth()
                        .focusRequester(focusRequester)
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
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next),
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(24.dp))

                OutlinedTextField(
                    value = fakePassword,
                    onValueChange = { fakePassword = it },
                    label = { Text("Фейковый мастер-пароль") },
                    visualTransformation = passwordVisibility3.visualTransformation,
                    trailingIcon = {
                        IconButton(onClick = passwordVisibility3.toggle) {
                            Icon(
                                imageVector = passwordVisibility3.icon,
                                contentDescription = passwordVisibility3.description
                            )
                        }
                    },
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next),
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(16.dp))

                OutlinedTextField(
                    value = confirmFakePassword,
                    onValueChange = { confirmFakePassword = it },
                    label = { Text("Подтверждение фейкового пароля") },
                    visualTransformation = passwordVisibility4.visualTransformation,
                    trailingIcon = {
                        IconButton(onClick = passwordVisibility4.toggle) {
                            Icon(
                                imageVector = passwordVisibility4.icon,
                                contentDescription = passwordVisibility4.description
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

                            fakePassword.isEmpty() || confirmFakePassword.isEmpty() -> {
                                errorMessage = "Фейковые пароли не могут быть пустыми!"
                            }

                            fakePassword != confirmFakePassword -> {
                                errorMessage = "Фейковые пароли не совпадают!"
                            }
                            else -> {
                                val salt = KeyGenerator.generateSalt()
                                val key = KeyGenerator.deriveKeyFromPassword(password, salt)
                                val testBlock = KeyGenerator.encrypt(SecurityConfig.TEST_BLOCK, key)

                                prefsManager.saveSalt(salt)
                                prefsManager.saveEncryptedTestBlock(testBlock)

                                if (fakePassword.isNotEmpty()) {
                                    val fakeKey = KeyGenerator.deriveKeyFromPassword(fakePassword, salt)
                                    val fakeTestBlock = KeyGenerator.encrypt(SecurityConfig.FAKE_TEST_BLOCK, fakeKey)
                                    prefsManager.saveFakeEncryptedTestBlock(fakeTestBlock)
                                }

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
