package com.alius.gmrstockplus.presentation.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.alius.gmrstockplus.data.ClientRepositoryImpl
import com.alius.gmrstockplus.domain.model.Cliente
import kotlinx.coroutines.launch

@Composable
fun ClientValidationScreen() {
    var clients by remember { mutableStateOf<List<Cliente>>(emptyList()) }
    var selectedPlant by remember { mutableStateOf("Ninguna") }
    var isLoading by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()

    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        Text("Validación Multi-Planta", style = MaterialTheme.typography.h5)
        Spacer(modifier = Modifier.height(8.dp))
        Text("Planta actual: $selectedPlant", color = MaterialTheme.colors.primary)

        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
            // BOTÓN PLANTA 07
            Button(onClick = {
                selectedPlant = "P07"
                scope.launch {
                    isLoading = true
                    clients = ClientRepositoryImpl("P07").getAllClientsOrderedByName()
                    isLoading = false
                }
            }) {
                Text("Cargar P07")
            }

            // BOTÓN PLANTA 08
            Button(onClick = {
                selectedPlant = "P08"
                scope.launch {
                    isLoading = true
                    clients = ClientRepositoryImpl("P08").getAllClientsOrderedByName()
                    isLoading = false
                }
            }) {
                Text("Cargar P08")
            }
        }

        Divider(modifier = Modifier.padding(vertical = 16.dp))

        if (isLoading) {
            CircularProgressIndicator(modifier = Modifier.align(androidx.compose.ui.Alignment.CenterHorizontally))
        } else {
            LazyColumn {
                items(clients) { cliente ->
                    Card(
                        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                        elevation = 2.dp
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text(cliente.cliNombre, style = MaterialTheme.typography.subtitle1)
                            if (cliente.cliObservaciones.isNotEmpty()) {
                                Text(cliente.cliObservaciones, style = MaterialTheme.typography.caption)
                            }
                        }
                    }
                }
            }
        }
    }
}