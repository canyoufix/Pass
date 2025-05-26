package com.canyoufix.ui.screens.menu

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.canyoufix.ui.R
import com.canyoufix.ui.components.card.MenuCard

@Composable
fun SettingsScreen(navController: NavController) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        MenuCard(
            title = "Безопасность",
            icon = painterResource(id = R.drawable.ic_security),
            onClick = { navController.navigate("security_settings") }
        )
        Spacer(modifier = Modifier.height(16.dp))

        MenuCard(
            title = "Синхронизация",
            icon = painterResource(id = R.drawable.ic_sync),
            onClick = { navController.navigate("sync_settings") }
        )
        Spacer(modifier = Modifier.height(16.dp))

        MenuCard(
            title = "Хранилище",
            icon = painterResource(id = R.drawable.ic_storage2),
            onClick = { navController.navigate("storage_settings") }
        )
        Spacer(modifier = Modifier.height(16.dp))

        MenuCard(
            title = "Внешний вид",
            icon = painterResource(id = R.drawable.ic_palette),
            onClick = { navController.navigate("appearance_settings") }
        )

    }
}