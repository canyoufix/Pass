package com.canyoufix.ui.screens.auth

import android.content.Context
import android.util.Log
import android.widget.Toast
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.canyoufix.crypto.KeyGenerator
import com.canyoufix.crypto.SecurePrefsManager
import com.canyoufix.crypto.SecurityConfig
import com.canyoufix.data.database.DatabaseManager
import kotlinx.coroutines.launch
import org.koin.androidx.compose.koinViewModel


@Composable
fun AuthScreen(
    onSuccess: () -> Unit,
    onFail: (String) -> Unit,
    onResetComplete: () -> Unit // Добавляем callback для сброса данных
) {
    val context = LocalContext.current
    var password by remember { mutableStateOf("") }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    val prefsManager = remember { SecurePrefsManager(context) }
    val databaseManager = remember { DatabaseManager(context) }

    // Получаем корутинный скоуп для работы с корутинами
    val coroutineScope = rememberCoroutineScope()

    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Column(
            modifier = Modifier
                .align(Alignment.Center)
                .padding(horizontal = 16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                imageVector = Icons.Default.Lock,
                contentDescription = "Lock Icon",
                modifier = Modifier.size(96.dp),
                tint = Color.Black
            )
            Spacer(modifier = Modifier.height(24.dp))

            Text("Введите мастер-пароль")
            OutlinedTextField(
                value = password,
                onValueChange = { password = it },
                label = { Text("Мастер-пароль") },
                visualTransformation = PasswordVisualTransformation()
            )
            Spacer(modifier = Modifier.height(16.dp))

            // Кнопка для входа
            Button(onClick = {
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
            }) {
                Text("Войти")
            }
            Spacer(modifier = Modifier.height(16.dp))

            // Кнопка для сброса данных
            Button(onClick = {
                coroutineScope.launch {
                    try {
                        // Сброс всех данных из базы данных
                        databaseManager.clearAllData()

                        // Сброс соли и тестовых данных из EncryptedSharedPreferences
                        prefsManager.clearAllData()

                        // После сброса данных, переходим на экран регистрации (setup)
                        onResetComplete()

                        errorMessage = "Все данные были сброшены!"
                        onFail(errorMessage!!) // Покажем сообщение
                    } catch (e: Exception) {
                        errorMessage = "Ошибка при сбросе данных: ${e.message}"
                        onFail(errorMessage!!) // Покажем ошибку
                    }
                }
            }) {
                Text("Сбросить все данные")
            }

            errorMessage?.let {
                Text(it, color = Color.Red)
            }
        }
    }
}





