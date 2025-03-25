package com.canyoufix.ui.screens.storage

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.navigation.NavController
import androidx.navigation.NavHostController
import com.canyoufix.data.viewmodel.NoteViewModel
import com.canyoufix.ui.components.DataItemCard
import org.koin.androidx.compose.koinViewModel

@Composable
fun NoteScreen(navController: NavController, viewModel: NoteViewModel) {
    val notes by viewModel.allNotes.collectAsState(initial = emptyList())

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Text("Заметки", style = MaterialTheme.typography.headlineLarge)
        Spacer(modifier = Modifier.height(20.dp))

        if (notes.isEmpty()) {
            // Если список заметок пуст, выводим сообщение "Пусто"
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .weight(1f),
                contentAlignment = Alignment.Center
            ) {
                Text("Пусто", style = MaterialTheme.typography.bodyLarge)
            }
        } else {
            // Если список заметок не пуст, отображаем список заметок
            LazyColumn {
                items(notes) { note ->
                    DataItemCard(
                        title = note.title,
                        subtitle = note.content,
                        onClick = { navController.navigate("noteDetail/${note.id}") }
                    )
                }
            }
        }
    }
}
