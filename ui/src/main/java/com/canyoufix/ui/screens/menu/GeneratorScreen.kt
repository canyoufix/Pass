package com.canyoufix.ui.screens.menu

import android.content.ClipboardManager
import android.content.Context
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.canyoufix.ui.R
import com.canyoufix.ui.utils.generatePassword

@Composable
fun GeneratorScreen() {
    var password by remember { mutableStateOf("") }
    var length by remember { mutableStateOf(12) }
    var useLowerCase by remember { mutableStateOf(true) }
    var useUpperCase by remember { mutableStateOf(true) }
    var useDigits by remember { mutableStateOf(true) }
    var useSymbols by remember { mutableStateOf(true) }

    // Получаем Context для доступа к ClipboardManager
    val context = LocalContext.current
    val clipboardManager = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager

    // Логика для копирования пароля
    val copyToClipboard: () -> Unit = {
        val clip = android.content.ClipData.newPlainText("password", password)
        clipboardManager.setPrimaryClip(clip)
    }

    // Проверяем, что хотя бы один свитч включен
    val isAnySwitchOn = useLowerCase || useUpperCase || useDigits || useSymbols

    // Логика принудительного включения ползунка с маленькими буквами
    val handleLowerCaseSwitch: (Boolean) -> Unit = { newValue ->
        if (!useUpperCase && !useDigits && !useSymbols) {
            useLowerCase = true // Принудительно включаем, если все выключены
        } else {
            useLowerCase = newValue
        }
    }

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
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .border(1.dp, Color.Gray)
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = password,
                style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold), // Уменьшенный шрифт
                modifier = Modifier.weight(1f)
            )
        }

        // Кнопка копирования вне рамки с паролем
        IconButton(
            onClick = copyToClipboard,
            modifier = Modifier
                .size(40.dp) // Размер кнопки
                .padding(8.dp)
                .background(MaterialTheme.colorScheme.primary, shape = CircleShape) // Кнопка с фоном
                .align(Alignment.End) // Выравнивание кнопки справа
        ) {
            val icon: Painter = painterResource(id = R.drawable.ic_copy) // Замените на свою иконку
            Image(painter = icon, contentDescription = "Copy")
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Кнопка для генерации пароля
        Button(onClick = {
            password = generatePassword(length, useLowerCase, useUpperCase, useDigits, useSymbols)
        }) {
            Text("Сгенерировать пароль")
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Ползунок для изменения длины пароля
        Text("Длина пароля: $length")
        Slider(
            value = length.toFloat(),
            onValueChange = { if (isAnySwitchOn) length = it.toInt() }, // Блокировка ползунка при выключении всех свитчей
            valueRange = 8f..128f, // Теперь от 8 до 128
            modifier = Modifier.padding(bottom = 16.dp) // Отступ снизу
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Свитчи для выбора типа символов
        SwitchWithLabel(
            label = "Строчные буквы",
            checked = useLowerCase,
            onCheckedChange = { handleLowerCaseSwitch(it) }
        )

        Spacer(modifier = Modifier.height(8.dp))

        SwitchWithLabel(
            label = "Заглавные буквы",
            checked = useUpperCase,
            onCheckedChange = { if (isAnySwitchOn) useUpperCase = it }
        )

        Spacer(modifier = Modifier.height(8.dp))

        SwitchWithLabel(
            label = "Цифры",
            checked = useDigits,
            onCheckedChange = { if (isAnySwitchOn) useDigits = it }
        )

        Spacer(modifier = Modifier.height(8.dp))

        SwitchWithLabel(
            label = "Спецсимволы",
            checked = useSymbols,
            onCheckedChange = { if (isAnySwitchOn) useSymbols = it }
        )
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


@Preview
@Composable
fun PreviewGeneratorScreen() {
    GeneratorScreen()
}