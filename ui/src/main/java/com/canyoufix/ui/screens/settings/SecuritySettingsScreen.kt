package com.canyoufix.ui.screens.settings

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.material3.Button
import androidx.compose.material3.Switch
import androidx.navigation.NavController

@Composable
fun SecuritySettingsScreen(navController: NavController) {
    Column(
        modifier = Modifier
            .padding(16.dp)
            .fillMaxSize()
    ) {
        SwitchPref(
            title = "Биометрическая аутентификация",
            checked = true,
            onCheckedChange = {}
        )

        Button(
            onClick = { /* Смена пароля */ },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Изменить мастер-пароль")
        }
    }
}

@Composable
private fun SwitchPref(
    title: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(text = title)
        Switch(checked = checked, onCheckedChange = onCheckedChange)
    }
}