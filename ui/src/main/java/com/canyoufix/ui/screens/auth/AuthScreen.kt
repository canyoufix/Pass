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
fun AuthScreen(
    onSuccess: () -> Unit,
    onFail: (String) -> Unit
) {
    val context = LocalContext.current
    var password by remember { mutableStateOf("") }
    var errorMessage by remember { mutableStateOf<String?>(null) }
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

            Text("Введите мастер-пароль")
            OutlinedTextField(
                value = password,
                onValueChange = { password = it },
                label = { Text("Мастер-пароль") },
                visualTransformation = PasswordVisualTransformation()
            )
            Spacer(modifier = Modifier.height(16.dp))

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
            errorMessage?.let {
                Text(it, color = Color.Red)
            }
        }
    }
}

