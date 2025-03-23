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
import com.canyoufix.data.viewmodel.CardViewModel
import com.canyoufix.ui.components.DataItemCard
import org.koin.androidx.compose.koinViewModel

@Composable
fun CardScreen(navController: NavController, viewModel: CardViewModel = koinViewModel()) {
    val cards by viewModel.allCards.collectAsState(initial = emptyList())

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Text("Карты", style = MaterialTheme.typography.headlineLarge)
        Spacer(modifier = Modifier.height(20.dp))

        if (cards.isEmpty()) {
            // Если список карт пуст, выводим сообщение "Пусто"
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .weight(1f),
                contentAlignment = Alignment.Center
            ) {
                Text("Пусто", style = MaterialTheme.typography.bodyLarge)
            }
        } else {
            // Если список карт не пуст, отображаем список карт
            LazyColumn {
                items(cards) { card ->
                    DataItemCard(
                        title = card.title,
                        subtitle = card.cardNumber,
                        onClick = { navController.navigate("cardDetail/${card.id}") }
                    )
                }
            }
        }
    }
}
