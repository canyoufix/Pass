package com.canyoufix.ui.components

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Button
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.SheetState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.DpOffset
import androidx.compose.ui.unit.dp
import com.canyoufix.data.entity.CardEntity
import com.canyoufix.data.entity.NoteEntity
import com.canyoufix.data.entity.PasswordEntity
import com.canyoufix.data.viewmodel.CardViewModel
import com.canyoufix.data.viewmodel.NoteViewModel
import com.canyoufix.data.viewmodel.PasswordViewModel
import kotlinx.coroutines.launch


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddEntryBottomSheet(
    sheetState: SheetState,
    onDismiss: () -> Unit,
    passwordViewModel: PasswordViewModel, // Убрали параметр по умолчанию
    cardViewModel: CardViewModel, // Убрали параметр по умолчанию
    noteViewModel: NoteViewModel // Убрали параметр по умолчанию
) {
    val scope = rememberCoroutineScope()

    val categories = listOf("Пароль", "Карта", "Заметка")
    var selectedCategory by remember { mutableStateOf(categories[0]) }

    // Общие поля
    var title by remember { mutableStateOf("") }
    var site by remember { mutableStateOf("") }
    var username by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }

    var cardNumber by remember { mutableStateOf("") }
    var expiryDate by remember { mutableStateOf("") }
    var cvc by remember { mutableStateOf("") }
    var cardHolder by remember { mutableStateOf("") }

    var noteContent by remember { mutableStateOf("") }

    ModalBottomSheet(
        onDismissRequest = { onDismiss() },
        sheetState = sheetState
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(text = "Добавить новую запись", style = MaterialTheme.typography.titleLarge)

            Spacer(modifier = Modifier.height(16.dp))

            CategoryDropdown(
                categories = categories,
                selectedCategory = selectedCategory,
                onCategorySelected = { selectedCategory = it }
            )

            Spacer(modifier = Modifier.height(16.dp))

            when (selectedCategory) {
                "Пароль" -> PasswordFields(
                    title, { title = it }, site, { site = it },
                    username, { username = it }, password, { password = it }
                )
                "Карта" -> CardFields(
                    title, { title = it }, cardNumber, { cardNumber = it },
                    expiryDate, { expiryDate = it }, cvc, { cvc = it },
                    cardHolder, { cardHolder = it }
                )
                "Заметка" -> NoteFields(
                    title, { title = it }, noteContent, { noteContent = it }
                )
            }
            Spacer(modifier = Modifier.height(16.dp))

            Button(onClick = {
                scope.launch {
                    val entity = when (selectedCategory) {
                        "Пароль" -> PasswordEntity(title = title, site = site, username = username, password = password)
                        "Карта" -> CardEntity(title = title, cardNumber = cardNumber, expiryDate = expiryDate, cvc = cvc, cardHolder = cardHolder)
                        "Заметка" -> NoteEntity(title = title, content = noteContent)
                        else -> null
                    }

                    entity?.let {
                        when (it) {
                            is PasswordEntity -> passwordViewModel.insert(it)
                            is CardEntity -> cardViewModel.insert(it)
                            is NoteEntity -> noteViewModel.insert(it)
                            else -> Unit
                        }
                    }

                    sheetState.hide()
                }.invokeOnCompletion {
                    if (!sheetState.isVisible) {
                        onDismiss()
                    }
                }
            }) {
                Text("Сохранить")
            }
            Spacer(modifier = Modifier.height(16.dp))

            Button(onClick = {
                scope.launch { sheetState.hide() }.invokeOnCompletion {
                    if (!sheetState.isVisible) {
                        onDismiss()
                    }
                }
            }) {
                Text("Закрыть")
            }

        }
    }
}

@Composable
fun CategoryDropdown(categories: List<String>, selectedCategory: String, onCategorySelected: (String) -> Unit) {
    var expanded by remember { mutableStateOf(false) }

    Box {
        OutlinedButton(
            onClick = { expanded = true },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(text = selectedCategory)
        }

        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false },
            offset = DpOffset(0.dp, 4.dp),
            modifier = Modifier.width(200.dp)
        ) {
            categories.forEach { category ->
                DropdownMenuItem(
                    text = { Text(category) },
                    onClick = {
                        onCategorySelected(category)
                        expanded = false
                    }
                )
            }
        }
    }
}

@Composable
fun PasswordFields(
    title: String, onTitleChange: (String) -> Unit,
    site: String, onSiteChange: (String) -> Unit,
    username: String, onUsernameChange: (String) -> Unit,
    password: String, onPasswordChange: (String) -> Unit
) {
    OutlinedTextField(value = title, onValueChange = onTitleChange, label = { Text("Название") }, modifier = Modifier.fillMaxWidth())
    Spacer(modifier = Modifier.height(8.dp))
    OutlinedTextField(value = site, onValueChange = onSiteChange, label = { Text("Сайт") }, modifier = Modifier.fillMaxWidth())
    Spacer(modifier = Modifier.height(8.dp))
    OutlinedTextField(value = username, onValueChange = onUsernameChange, label = { Text("Логин") }, modifier = Modifier.fillMaxWidth())
    Spacer(modifier = Modifier.height(8.dp))
    OutlinedTextField(value = password, onValueChange = onPasswordChange, label = { Text("Пароль") }, modifier = Modifier.fillMaxWidth())
}

@Composable
fun CardFields(
    title: String, onTitleChange: (String) -> Unit,
    cardNumber: String, onCardNumberChange: (String) -> Unit,
    expiryDate: String, onExpiryDateChange: (String) -> Unit,
    cvc: String, onCvcChange: (String) -> Unit,
    cardHolder: String, onCardHolderChange: (String) -> Unit
) {
    OutlinedTextField(value = title, onValueChange = onTitleChange, label = { Text("Название") }, modifier = Modifier.fillMaxWidth())
    Spacer(modifier = Modifier.height(8.dp))
    OutlinedTextField(value = cardNumber, onValueChange = onCardNumberChange, label = { Text("Номер карты") }, modifier = Modifier.fillMaxWidth())
    Spacer(modifier = Modifier.height(8.dp))
    OutlinedTextField(value = expiryDate, onValueChange = onExpiryDateChange, label = { Text("Срок действия") }, modifier = Modifier.fillMaxWidth())
    Spacer(modifier = Modifier.height(8.dp))
    OutlinedTextField(value = cvc, onValueChange = onCvcChange, label = { Text("CVC") }, modifier = Modifier.fillMaxWidth())
    Spacer(modifier = Modifier.height(8.dp))
    OutlinedTextField(value = cardHolder, onValueChange = onCardHolderChange, label = { Text("Держатель карты") }, modifier = Modifier.fillMaxWidth())
}

@Composable
fun NoteFields(
    title: String, onTitleChange: (String) -> Unit,
    content: String, onContentChange: (String) -> Unit
) {
    OutlinedTextField(value = title, onValueChange = onTitleChange, label = { Text("Название") }, modifier = Modifier.fillMaxWidth())
    Spacer(modifier = Modifier.height(8.dp))
    OutlinedTextField(value = content, onValueChange = onContentChange, label = { Text("Содержание") }, modifier = Modifier.fillMaxWidth())
}
