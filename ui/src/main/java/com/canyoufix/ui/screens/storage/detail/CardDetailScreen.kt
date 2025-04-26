package com.canyoufix.ui.screens.storage.detail

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.OutlinedTextField
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.navigation.NavController
import com.canyoufix.data.viewmodel.PasswordViewModel
import com.canyoufix.ui.components.DataItemCard
import org.koin.androidx.compose.koinViewModel
import androidx.compose.material3.CircularProgressIndicator
import com.canyoufix.data.viewmodel.CardViewModel

@Composable
fun CardDetailScreen(
    navController: NavController,
    cardId: String,
    viewModel: CardViewModel // Принимаем ViewModel как параметр
) {
    val card by viewModel.getCardById(cardId).collectAsState(initial = null)

    var title by remember { mutableStateOf("") }
    var cardNumber by remember { mutableStateOf("") }
    var expiryDate by remember { mutableStateOf("") }
    var cvc by remember { mutableStateOf("") }
    var cardHolder by remember { mutableStateOf("") }

    // При загрузке данных заполняем поля
    LaunchedEffect(card) {
        card?.let {
            title = it.title
            cardNumber = it.cardNumber
            expiryDate = it.expiryDate
            cvc = it.cvc
            cardHolder = it.cardHolder
        }
    }

    if (card == null) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            CircularProgressIndicator()
        }
    } else {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
        ) {
            Spacer(modifier = Modifier.height(20.dp))

            OutlinedTextField(
                value = title,
                onValueChange = { title = it },
                label = { Text("Название") },
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(12.dp))

            OutlinedTextField(
                value = cardNumber,
                onValueChange = { cardNumber = it },
                label = { Text("Номер карты") },
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(12.dp))

            OutlinedTextField(
                value = expiryDate,
                onValueChange = { expiryDate = it },
                label = { Text("Срок действия (мм/гг)") },
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(12.dp))

            OutlinedTextField(
                value = cvc,
                onValueChange = { cvc = it },
                label = { Text("CVC") },
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(12.dp))

            OutlinedTextField(
                value = cardHolder,
                onValueChange = { cardHolder = it },
                label = { Text("Владелец карты") },
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(24.dp))

            Row(
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                Button(onClick = {
                    viewModel.delete(card!!)
                    navController.popBackStack()
                }) {
                    Text("Удалить")
                }

                Button(onClick = {
                    viewModel.update(
                        card!!.copy(
                            title = title,
                            cardNumber = cardNumber,
                            expiryDate = expiryDate,
                            cvc = cvc,
                            cardHolder = cardHolder
                        )
                    )
                    navController.popBackStack()
                }) {
                    Text("Сохранить")
                }
            }
        }
    }
}

