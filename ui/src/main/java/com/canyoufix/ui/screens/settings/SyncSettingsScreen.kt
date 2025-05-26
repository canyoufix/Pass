package com.canyoufix.ui.screens.settings

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.canyoufix.ui.components.SwitchPref
import com.canyoufix.ui.datastore.SyncSettings
import com.canyoufix.ui.datastore.SyncSettingsStore
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@Composable
fun SyncSettingsScreen(navController: NavController) {
    val context = LocalContext.current
    val settingsStore = remember { SyncSettingsStore(context) }
    val scope = rememberCoroutineScope()

    var isSyncEnabled by rememberSaveable { mutableStateOf(false) }
    var ipAddress by rememberSaveable { mutableStateOf("") }
    var port by rememberSaveable { mutableStateOf("") }

    // Сохраняем IP и порт через debounce
    LaunchedEffect(ipAddress, port, isSyncEnabled) {
        delay(500) // 500 мс после последнего изменения
        settingsStore.saveSettings(
            SyncSettings(isSyncEnabled, ipAddress, port)
        )
    }

    // Загружаем при первом запуске
    LaunchedEffect(Unit) {
        settingsStore.syncSettingsFlow.collect { saved ->
            isSyncEnabled = saved.isEnabled
            ipAddress = saved.ip
            port = saved.port
        }
    }

    Column(
        modifier = Modifier
            .padding(16.dp)
            .fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        SwitchPref(
            title = "Включить синхронизацию",
            checked = isSyncEnabled,
            onCheckedChange = { isSyncEnabled = it }
        )

        if (isSyncEnabled) {
            OutlinedTextField(
                value = ipAddress,
                onValueChange = { ipAddress = it },
                label = { Text("IP-адрес сервера") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )

            OutlinedTextField(
                value = port,
                onValueChange = { port = it },
                label = { Text("Порт") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
            )
        }
    }
}

