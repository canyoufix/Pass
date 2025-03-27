package com.canyoufix.ui.screens.menu

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.canyoufix.ui.utils.ClipboardUtils
import com.canyoufix.ui.utils.PasswordUtils

@Composable
fun GeneratorScreen() {
    var password by remember { mutableStateOf("") }
    var length by remember { mutableIntStateOf(25) }
    var useLowerCase by remember { mutableStateOf(true) }
    var useUpperCase by remember { mutableStateOf(true) }
    var useDigits by remember { mutableStateOf(true) }
    var useSymbols by remember { mutableStateOf(true) }

    // Получаем Context для доступа к ClipboardManager
    val context = LocalContext.current

    // Логика для копирования пароля
    val copyToClipboard: () -> Unit = {
        ClipboardUtils.copyToClipboard(context, "password", password)  // Используем утилиту
    }

    // Проверяем, что хотя бы один свитч включен
    val isAnySwitchOn = useLowerCase || useUpperCase || useDigits || useSymbols

    // Scroll для прокрутки контента
    val scrollState = rememberScrollState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .verticalScroll(scrollState), // Сделаем Column прокручиваемым
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Итоговый сгенерированный пароль
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(16.dp))
                .background(Color.Gray.copy(alpha = 0.1f)) // Серый фон с прозрачностью
                .padding(16.dp)
        ) {
            Text(
                text = password,
                style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold), // Уменьшенный шрифт
                modifier = Modifier.align(Alignment.CenterStart) // Выравнивание текста по левому краю
            )
        }
        Spacer(modifier = Modifier.height(16.dp))

        // Row для размещения кнопок горизонтально
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(16.dp), // Отступ между кнопками
            verticalAlignment = Alignment.CenterVertically // Выравнивание по центру по вертикали
        ) {
            // Кнопка для генерации пароля, делаем неактивной, если все свитчи выключены
            Button(
                onClick = {
                    password = PasswordUtils.generatePassword(length, useLowerCase, useUpperCase, useDigits, useSymbols)
                },
                enabled = isAnySwitchOn, // Кнопка неактивна, если все свитчи выключены
                modifier = Modifier.weight(1f) // Делает кнопку растягиваемой
            ) {
                Text("Сгенерировать")
            }

            // Кнопка для копирования пароля
            Button(
                onClick = copyToClipboard,
                enabled = password.isNotEmpty(),
                modifier = Modifier.weight(1f) // Делает кнопку растягиваемой
            ) {
                Text("Скопировать")
            }
        }
        Spacer(modifier = Modifier.height(16.dp))

        // Подложка с ползунком для изменения длины пароля
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(16.dp))
                .background(Color.Gray.copy(alpha = 0.1f)) // Серый фон
                .padding(16.dp)
        ) {
            Column {
                Text("Длина пароля: $length")
                Slider(
                    value = length.toFloat(),
                    onValueChange = { length = it.toInt() }, // Блокировка ползунка при выключении всех свитчей
                    valueRange = 8f..128f, // Теперь от 8 до 128
                    modifier = Modifier.padding(bottom = 5.dp) // Отступ снизу
                )
            }
        }
        Spacer(modifier = Modifier.height(24.dp))

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(16.dp))
                .background(Color.Gray.copy(alpha = 0.1f)) // Серый фон
                .padding(16.dp)
        ) {
            Column(
                modifier = Modifier.fillMaxWidth() // Используем Column для вертикального расположения
            ) {
                SwitchWithLabel(
                    label = "a-z",
                    checked = useLowerCase,
                    onCheckedChange = { useLowerCase = it }
                )

                Spacer(modifier = Modifier.height(16.dp))

                SwitchWithLabel(
                    label = "A-Z",
                    checked = useUpperCase,
                    onCheckedChange = { useUpperCase = it }
                )

                Spacer(modifier = Modifier.height(16.dp))

                SwitchWithLabel(
                    label = "0-9",
                    checked = useDigits,
                    onCheckedChange = { useDigits = it }
                )

                Spacer(modifier = Modifier.height(16.dp))

                SwitchWithLabel(
                    label = "!@#\$%^&*",
                    checked = useSymbols,
                    onCheckedChange = { useSymbols = it }
                )
            }
        }
    }
}

@Composable
fun SwitchWithLabel(
    label: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(text = label, modifier = Modifier.align(Alignment.CenterVertically))
        Switch(checked = checked, onCheckedChange = onCheckedChange)
    }
}