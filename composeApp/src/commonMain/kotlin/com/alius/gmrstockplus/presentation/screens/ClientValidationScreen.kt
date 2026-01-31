package com.alius.gmrstockplus.presentation.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.alius.gmrstockplus.data.ClientRepositoryImpl
import com.alius.gmrstockplus.domain.model.Cliente
import kotlinx.coroutines.launch

@Composable
fun ClientValidationScreen(onNavigateToLotes: () -> Unit) { // 1. Añadimos el parámetro de navegación
    var clients by remember { mutableStateOf<List<Cliente>>(emptyList()) }
    var selectedPlant by remember { mutableStateOf("Ninguna") }
    var isLoading by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    val scope = rememberCoroutineScope()



    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        // 2. Colocamos el título y el botón de navegación en una fila

        Spacer(modifier = Modifier.height(48.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("Validación Multi-Planta", style = MaterialTheme.typography.h5)

            Button(
                onClick = onNavigateToLotes,
                colors = ButtonDefaults.buttonColors(backgroundColor = MaterialTheme.colors.secondary)
            ) {
                Text("Ver Lotes")
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = "Planta actual: $selectedPlant",
            color = if (selectedPlant == "Ninguna") Color.Gray else MaterialTheme.colors.primary,
            style = MaterialTheme.typography.subtitle1
        )

        Spacer(modifier = Modifier.height(16.dp))

        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            Button(
                modifier = Modifier.weight(1f),
                onClick = {
                    selectedPlant = "P07"
                    scope.launch {
                        isLoading = true
                        errorMessage = null
                        clients = ClientRepositoryImpl("P07").getAllClientsOrderedByName()
                        isLoading = false
                    }
                }
            ) {
                Text("Cargar P07")
            }

            Button(
                modifier = Modifier.weight(1f),
                onClick = {
                    selectedPlant = "P08"
                    scope.launch {
                        isLoading = true
                        errorMessage = null
                        clients = ClientRepositoryImpl("P08").getAllClientsOrderedByName()
                        isLoading = false
                    }
                }
            ) {
                Text("Cargar P08")
            }
        }

        Divider(modifier = Modifier.padding(vertical = 16.dp))

        Box(modifier = Modifier.fillMaxSize()) {
            when {
                isLoading -> {
                    CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
                }
                errorMessage != null -> {
                    Text(
                        text = errorMessage!!,
                        color = MaterialTheme.colors.error,
                        modifier = Modifier.align(Alignment.Center)
                    )
                }
                clients.isEmpty() && selectedPlant != "Ninguna" -> {
                    Text(
                        text = "No hay clientes en esta base de datos",
                        modifier = Modifier.align(Alignment.Center),
                        color = Color.Gray
                    )
                }
                else -> {
                    LazyColumn(modifier = Modifier.fillMaxSize()) {
                        items(clients) { cliente ->
                            ClientCard(cliente)
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun ClientCard(cliente: Cliente) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        elevation = 4.dp,
        shape = MaterialTheme.shapes.medium
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = cliente.cliNombre,
                style = MaterialTheme.typography.h6
            )
            if (cliente.cliObservaciones.isNotEmpty()) {
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = cliente.cliObservaciones,
                    style = MaterialTheme.typography.body2,
                    color = Color.DarkGray
                )
            }
        }
    }
}