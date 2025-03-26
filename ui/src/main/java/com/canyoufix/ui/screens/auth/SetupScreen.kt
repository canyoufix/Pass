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
import kotlinx.coroutines.launch
import org.koin.androidx.compose.koinViewModel

@Composable
fun SetupScreen(onSetupComplete: () -> Unit) {
    val context = LocalContext.current
    var password by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") } // Для второго поля
    var errorMessage by remember { mutableStateOf<String?>(null) } // Для отображения ошибки
    val prefsManager = remember { SecurePrefsManager(context) }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Column(
            modifier = Modifier
                .align(Alignment.Center) // Центрируем все элементы внутри Column
                .padding(horizontal = 16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Иконка замка
            Icon(
                imageVector = Icons.Default.Lock, // Используем встроенную иконку замка
                contentDescription = "Lock Icon", // Описание иконки для доступности
                modifier = Modifier.size(96.dp), // Размер иконки
                tint = Color.Black // Цвет иконки (можно настроить)
            )
            Spacer(modifier = Modifier.height(24.dp)) // Отступ между иконкой и полем ввода

            // Первое поле ввода пароля
            Text("Придумайте мастер-пароль")
            OutlinedTextField(
                value = password,
                onValueChange = { password = it },
                label = { Text("Мастер-пароль") },
                visualTransformation = PasswordVisualTransformation()
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Второе поле для подтверждения пароля
            Text("Подтвердите мастер-пароль")
            OutlinedTextField(
                value = confirmPassword,
                onValueChange = { confirmPassword = it },
                label = { Text("Подтверждение пароля") },
                visualTransformation = PasswordVisualTransformation()
            )

            // Показываем ошибку, если пароли не совпадают
            errorMessage?.let {
                Text(it, color = Color.Red)
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Кнопка сохранения пароля
            Button(onClick = {
                if (password.isEmpty() || confirmPassword.isEmpty()) {
                    errorMessage = "Пароли не могут быть пустыми!"
                } else if (password != confirmPassword) {
                    errorMessage = "Пароли не совпадают!"
                } else {
                    val salt = KeyGenerator.generateSalt()
                    val key = KeyGenerator.deriveKeyFromPassword(password, salt)
                    val testBlock = KeyGenerator.encrypt(SecurityConfig.TEST_BLOCK, key)

                    prefsManager.saveSalt(salt)
                    prefsManager.saveEncryptedTestBlock(testBlock)

                    onSetupComplete() // Переход на следующий экран
                }
            }) {
                Text("Сохранить и продолжить")
            }
        }
    }
}
