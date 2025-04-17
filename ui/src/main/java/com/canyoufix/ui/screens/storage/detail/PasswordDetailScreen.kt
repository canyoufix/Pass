package com.canyoufix.ui.screens.storage.detail

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.OutlinedTextField
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.navigation.NavController
import com.canyoufix.data.viewmodel.PasswordViewModel
import com.canyoufix.ui.components.DataItemCard
import org.koin.androidx.compose.koinViewModel
import androidx.compose.material3.CircularProgressIndicator


@Composable
fun PasswordDetailScreen(
    navController: NavController,
    passwordId: String,
    viewModel: PasswordViewModel // Принимаем ViewModel как параметр
) {
    val password by viewModel.getPasswordById(passwordId).collectAsState(initial = null)

    var title by remember { mutableStateOf("") }
    var site by remember { mutableStateOf("") }
    var username by remember { mutableStateOf("") }
    var userPassword by remember { mutableStateOf("") }

    // При загрузке данных заполняем поля
    LaunchedEffect(password) {
        password?.let {
            title = it.title
            site = it.site
            username = it.username
            userPassword = it.password
        }
    }

    if (password == null) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            CircularProgressIndicator()
        }
    } else {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
        ) {
            Text("Детали записи", style = MaterialTheme.typography.headlineLarge)
            Spacer(modifier = Modifier.height(20.dp))

            OutlinedTextField(
                value = title,
                onValueChange = { title = it },
                label = { Text("Название") },
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(12.dp))

            OutlinedTextField(
                value = site,
                onValueChange = { site = it },
                label = { Text("Сайт") },
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(12.dp))

            OutlinedTextField(
                value = username,
                onValueChange = { username = it },
                label = { Text("Логин") },
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(12.dp))

            OutlinedTextField(
                value = userPassword,
                onValueChange = { userPassword = it },
                label = { Text("Пароль") },
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(24.dp))

            Row(
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                Button(onClick = {
                    viewModel.delete(password!!)
                    navController.popBackStack()
                }) {
                    Text("Удалить")
                }

                Button(onClick = {
                    viewModel.update(
                        password!!.copy(
                            title = title,
                            site = site,
                            username = username,
                            password = userPassword
                        )
                    )
                    navController.popBackStack()
                }) {
                    Text("Сохранить")
                }
            }
        }
    }
}
