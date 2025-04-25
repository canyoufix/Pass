package com.canyoufix.ui.screens.menu

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Storage
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.canyoufix.data.viewmodel.CardViewModel
import com.canyoufix.data.viewmodel.NoteViewModel
import com.canyoufix.data.viewmodel.PasswordViewModel
import com.canyoufix.ui.R
import com.canyoufix.ui.components.AddEntryBottomSheet


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StorageScreen(
    navController: NavController,
    passwordViewModel: PasswordViewModel,
    cardViewModel: CardViewModel,
    noteViewModel: NoteViewModel
) {

    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    var showBottomSheet by remember { mutableStateOf(false) }


    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        StorageCategoryCard(
            title = "Логин",
            icon = painterResource(id = R.drawable.ic_password),
            onClick = { navController.navigate("password") }
        )
        Spacer(modifier = Modifier.height(16.dp))

        StorageCategoryCard(
            title = "Карта",
            icon = painterResource(id = R.drawable.ic_card),
            onClick = { navController.navigate("card") }
        )
        Spacer(modifier = Modifier.height(16.dp))

        StorageCategoryCard(
            title = "Защищенная заметка",
            icon = painterResource(id = R.drawable.ic_note),
            onClick = { navController.navigate("note") }
        )
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
    ) {
        FloatingActionButton(
            onClick = { showBottomSheet = true },
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(16.dp),
            shape = CircleShape,
            containerColor = MaterialTheme.colorScheme.primary, // цвет кнопки
            contentColor = MaterialTheme.colorScheme.onPrimary  // цвет иконки
        ) {
            Icon(Icons.Default.Add, contentDescription = "Добавить")
        }
    }


    // Окно добавления записи
    if (showBottomSheet) {
        AddEntryBottomSheet(
            sheetState = sheetState,
            onDismiss = { showBottomSheet = false },
            passwordViewModel = passwordViewModel,  // Передаем ViewModel
            cardViewModel = cardViewModel,          // Передаем ViewModel
            noteViewModel = noteViewModel           // Передаем ViewModel
        )
    }
}

@Composable
fun StorageCategoryCard(
    title: String,
    icon: Painter,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        onClick = onClick,
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface, // Основной цвет фона
            contentColor = MaterialTheme.colorScheme.onSurface // Цвет контента (текста и иконок)
        )
    ) {
        Row(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Icon(
                painter = icon,
                contentDescription = null,
                modifier = Modifier.size(24.dp)
            )
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier.weight(1f)
            )
            Icon(
                imageVector = Icons.Default.ChevronRight,
                contentDescription = "Перейти",
                modifier = Modifier.size(24.dp)
            )
        }
    }
}


