package com.canyoufix.ui.screens.menu

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController

@Composable
fun StorageScreen(navController: NavController) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Карточки для каждой категории
        StorageCategoryCard(
            title = "Логины",
            onClick = { navController.navigate("login") } // Переход на экран с логинами
        )
        Spacer(modifier = Modifier.height(16.dp))

        StorageCategoryCard(
            title = "Карты",
            onClick = { navController.navigate("card") } // Переход на экран с картами
        )
        Spacer(modifier = Modifier.height(16.dp))

        StorageCategoryCard(
            title = "Защищенные заметки",
            onClick = { navController.navigate("note") } // Переход на экран с заметками
        )
    }
}

@Composable
fun StorageCategoryCard(title: String, onClick: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .padding(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp), // Corrected elevation usage
        shape = MaterialTheme.shapes.medium
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.bodyMedium,
                modifier = Modifier.weight(1f)
            )
            Icon(
                imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                contentDescription = "Перейти",
                modifier = Modifier.size(24.dp)
            )
        }
    }
}

@Preview
@Composable
fun PreviewStorageScreen() {
    // Заглушка для NavController
    val navController = rememberNavController()

    // Теперь передаем navController в StorageScreen
    StorageScreen(navController = navController)
}