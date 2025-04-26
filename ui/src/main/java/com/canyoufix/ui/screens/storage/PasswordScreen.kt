package com.canyoufix.ui.screens.storage

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.navigation.NavController
import com.canyoufix.data.viewmodel.PasswordViewModel
import com.canyoufix.ui.components.card.DataItemCard
import org.koin.androidx.compose.koinViewModel

@Composable
fun PasswordScreen(navController: NavController, viewModel: PasswordViewModel = koinViewModel()) {
    val passwords by viewModel.allPasswords.collectAsState(initial = emptyList())

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        if (passwords.isEmpty()) {
            // Если список паролей пуст, выводим сообщение "Пусто"
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .weight(1f),
                contentAlignment = Alignment.Center
            ) {
                Text("Пусто", style = MaterialTheme.typography.bodyLarge)
            }
        } else {
            // Если список не пуст, отображаем список паролей
            LazyColumn {
                items(passwords) { password ->
                    DataItemCard(
                        title = password.title,
                        subtitle = password.username,
                        onClick = { navController.navigate("passwordDetail/${password.id}")
                        }
                    )
                }
            }
        }
    }
}