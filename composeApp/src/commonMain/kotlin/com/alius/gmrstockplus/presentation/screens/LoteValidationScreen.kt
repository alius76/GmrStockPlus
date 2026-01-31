package com.alius.gmrstockplus.presentation.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.alius.gmrstockplus.data.LoteRepositoryImpl
import com.alius.gmrstockplus.domain.model.LoteModel
import kotlinx.coroutines.launch

@Composable
fun LoteValidationScreen() {
    var lotes by remember { mutableStateOf<List<LoteModel>>(emptyList()) }
    var selectedPlant by remember { mutableStateOf("Ninguna") }
    var isLoading by remember { mutableStateOf(false) }
    var queryText by remember { mutableStateOf("") }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    val scope = rememberCoroutineScope()

    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        Text("Validación de Lotes", style = MaterialTheme.typography.h5, fontWeight = FontWeight.Bold)

        Text(
            text = "Planta seleccionada: $selectedPlant",
            color = if (selectedPlant == "Ninguna") Color.Gray else MaterialTheme.colors.primary,
            style = MaterialTheme.typography.subtitle2
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Buscador
        OutlinedTextField(
            value = queryText,
            onValueChange = { queryText = it },
            label = { Text("Buscar por descripción o número") },
            modifier = Modifier.fillMaxWidth(),
            leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
            singleLine = true
        )

        Spacer(modifier = Modifier.height(12.dp))

        // Selector de Planta con botones de acción
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            Button(
                onClick = {
                    selectedPlant = "P07"
                    scope.launch {
                        isLoading = true
                        errorMessage = null
                        lotes = LoteRepositoryImpl("P07").listarLotesPorDescripcion(queryText)
                        isLoading = false
                    }
                },
                modifier = Modifier.weight(1f),
                colors = ButtonDefaults.buttonColors(
                    backgroundColor = if(selectedPlant == "P07") MaterialTheme.colors.primary else MaterialTheme.colors.surface
                )
            ) { Text("Cargar P07") }

            Button(
                onClick = {
                    selectedPlant = "P08"
                    scope.launch {
                        isLoading = true
                        errorMessage = null
                        lotes = LoteRepositoryImpl("P08").listarLotesPorDescripcion(queryText)
                        isLoading = false
                    }
                },
                modifier = Modifier.weight(1f),
                colors = ButtonDefaults.buttonColors(
                    backgroundColor = if(selectedPlant == "P08") MaterialTheme.colors.primary else MaterialTheme.colors.surface
                )
            ) { Text("Cargar P08") }
        }

        Divider(modifier = Modifier.padding(vertical = 16.dp))

        // Estado de carga y listado
        Box(modifier = Modifier.fillMaxSize()) {
            if (isLoading) {
                CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
            } else if (lotes.isEmpty() && selectedPlant != "Ninguna") {
                Text(
                    "No se encontraron lotes para '$queryText'",
                    modifier = Modifier.align(Alignment.Center),
                    color = Color.Gray
                )
            } else {
                Column {
                    if (lotes.isNotEmpty()) {
                        Text(
                            "Resultados: ${lotes.size}",
                            style = MaterialTheme.typography.caption,
                            modifier = Modifier.padding(bottom = 8.dp)
                        )
                    }
                    LazyColumn(modifier = Modifier.fillMaxSize()) {
                        items(lotes) { lote ->
                            LoteItemCard(lote)
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun LoteItemCard(lote: LoteModel) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp),
        elevation = 4.dp,
        shape = MaterialTheme.shapes.medium
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Lote: ${lote.number}",
                    style = MaterialTheme.typography.h6,
                    color = MaterialTheme.colors.primary,
                    fontWeight = FontWeight.Bold
                )

                // Badge de estado
                Surface(
                    color = if (lote.status == "s") Color(0xFFE8F5E9) else Color(0xFFF5F5F5),
                    shape = MaterialTheme.shapes.small
                ) {
                    Text(
                        text = if (lote.status == "s") " DISPONIBLE " else " OCUPADO ",
                        color = if (lote.status == "s") Color(0xFF2E7D32) else Color.Gray,
                        style = MaterialTheme.typography.overline,
                        modifier = Modifier.padding(4.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))
            Text("Descripción: ${lote.description}", style = MaterialTheme.typography.body1)
            Text("Ubicación: ${lote.location}", style = MaterialTheme.typography.body2, color = Color.Gray)

            if (lote.bigBag.isNotEmpty()) {
                Divider(modifier = Modifier.padding(vertical = 8.dp), thickness = 0.5.dp)
                Text(
                    "BigBags: ${lote.bigBag.size} unidades",
                    style = MaterialTheme.typography.body2,
                    fontWeight = FontWeight.Bold
                )
                // Mostrar peso total sumando los BigBags
                val totalWeight = lote.bigBag.sumOf { it.bbWeight.toDoubleOrNull() ?: 0.0 }
                Text("Peso Total: $totalWeight kg", style = MaterialTheme.typography.caption)
            }

            if (lote.booked != null) {
                Spacer(modifier = Modifier.height(8.dp))
                Surface(
                    color = Color(0xFFFFF9C4),
                    shape = MaterialTheme.shapes.small,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = "🔒 Reservado para: ${lote.booked.cliNombre}",
                        style = MaterialTheme.typography.caption,
                        modifier = Modifier.padding(8.dp),
                        fontWeight = FontWeight.Medium
                    )
                }
            }
        }
    }
}