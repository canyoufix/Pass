package com.canyoufix.ui.screens.settings

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.foundation.layout.size
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.KeyboardType
import androidx.navigation.NavController
import com.canyoufix.settings.datastore.SyncSettings
import com.canyoufix.settings.datastore.SyncSettingsStore
import com.canyoufix.sync.retrofit.RetrofitClientProvider
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.IOException

@Composable
fun SyncSettingsScreen(
    navController: NavController,
    retrofitClientProvider: RetrofitClientProvider
) {
    val context = LocalContext.current
    val settingsStore = remember { SyncSettingsStore(context) }

    var ipAddress by rememberSaveable { mutableStateOf("") }
    var port by rememberSaveable { mutableStateOf("") }
    var isSyncEnabled by rememberSaveable { mutableStateOf(false) }

    var isConnecting by remember { mutableStateOf(false) }

    val snackbarHostState = remember { SnackbarHostState() }

    // Загрузка настроек при старте
    LaunchedEffect(Unit) {
        settingsStore.syncSettingsFlow.collect { saved ->
            ipAddress = saved.ip
            port = saved.port
            isSyncEnabled = saved.isEnabled
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .padding(horizontal = 16.dp)
                .fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
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

            if (!isSyncEnabled) {
                Button(
                    onClick = {
                        isConnecting = true
                        // Запуск корутины подключения
                        CoroutineScope(Dispatchers.IO).launch {
                            try {
                                // Обновим настройки, чтобы retrofitClientProvider получил актуальные данные
                                settingsStore.saveSettings(
                                    SyncSettings(isEnabled = false, ip = ipAddress, port = port)
                                )

                                val client = retrofitClientProvider.getClient()
                                val response = client.pingApi.ping()

                                if (response.isSuccessful) {
                                    settingsStore.saveSettings(
                                        SyncSettings(isEnabled = true, ip = ipAddress, port = port)
                                    )
                                    withContext(Dispatchers.Main) {
                                        isSyncEnabled = true
                                        snackbarHostState.showSnackbar("Синхронизация включена")
                                    }
                                } else {
                                    throw IOException("Ошибка сервера: ${response.code()}")
                                }
                            } catch (e: Exception) {
                                withContext(Dispatchers.Main) {
                                    snackbarHostState.showSnackbar("Ошибка подключения: ${e.message}")
                                }
                            } finally {
                                withContext(Dispatchers.Main) {
                                    isConnecting = false
                                }
                            }
                        }
                    },
                    enabled = !isConnecting,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    if (isConnecting) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(20.dp),
                            strokeWidth = 2.dp
                        )
                    } else {
                        Text("Подключиться")
                    }
                }
            } else {
                Button(
                    onClick = {
                        CoroutineScope(Dispatchers.IO).launch {
                            settingsStore.saveSettings(
                                SyncSettings(isEnabled = false, ip = ipAddress, port = port)
                            )
                            withContext(Dispatchers.Main) {
                                isSyncEnabled = false
                                snackbarHostState.showSnackbar("Синхронизация отключена")
                            }
                        }
                    },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Отключить синхронизацию")
                }

                Text("Синхронизация включена", color = Color.Green)
            }
        }
    }
}