package com.alius.gmrstockplus.presentation.screens


import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.alius.gmrstockplus.data.LoteRepositoryImpl
import com.alius.gmrstockplus.domain.model.LoteModel
import kotlinx.coroutines.launch

@Composable
fun LoteValidationScreen() {
    var lotes by remember { mutableStateOf<List<LoteModel>>(emptyList()) }
    var selectedPlant by remember { mutableStateOf("Ninguna") }
    var isLoading by remember { mutableStateOf(false) }
    var queryText by remember { mutableStateOf("") } // Para buscar por descripción
    val scope = rememberCoroutineScope()

    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        Text("Validación de Lotes - GmrStockPlus", style = MaterialTheme.typography.h5)

        Spacer(modifier = Modifier.height(16.dp))

        // Selector de Planta
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            Button(
                onClick = {
                    selectedPlant = "P07"
                    scope.launch {
                        isLoading = true
                        lotes = LoteRepositoryImpl("P07").listarLotesPorDescripcion(queryText)
                        isLoading = false
                    }
                },
                modifier = Modifier.weight(1f)
            ) { Text("Cargar P07") }

            Button(
                onClick = {
                    selectedPlant = "P08"
                    scope.launch {
                        isLoading = true
                        lotes = LoteRepositoryImpl("P08").listarLotesPorDescripcion(queryText)
                        isLoading = false
                    }
                },
                modifier = Modifier.weight(1f)
            ) { Text("Cargar P08") }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Campo de búsqueda por descripción
        OutlinedTextField(
            value = queryText,
            onValueChange = { queryText = it },
            label = { Text("Buscar por descripción (ej: Algodón)") },
            modifier = Modifier.fillMaxWidth()
        )

        Divider(modifier = Modifier.padding(vertical = 16.dp))

        if (isLoading) {
            CircularProgressIndicator(modifier = Modifier.align(androidx.compose.ui.Alignment.CenterHorizontally))
        } else {
            Text("Lotes encontrados: ${lotes.size}", style = MaterialTheme.typography.caption)

            LazyColumn(modifier = Modifier.fillMaxSize()) {
                items(lotes) { lote ->
                    LoteItemCard(lote)
                }
            }
        }
    }
}

@Composable
fun LoteItemCard(lote: LoteModel) {
    Card(
        modifier = Modifier.fillMaxWidth().padding(vertical = 6.dp),
        elevation = 4.dp
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Row(horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth()) {
                Text("Lote: ${lote.number}", style = MaterialTheme.typography.h6, color = MaterialTheme.colors.primary)
                Text(lote.status, color = if(lote.status == "s") Color.Green else Color.Gray)
            }

            Text("Desc: ${lote.description}", style = MaterialTheme.typography.body1)
            Text("Ubicación: ${lote.location}", style = MaterialTheme.typography.body2)

            Spacer(modifier = Modifier.height(4.dp))

            // Verificamos si los BigBags se cargaron (Lista anidada)
            Text("Bolsas (BigBags): ${lote.bigBag.size}", fontWeight = androidx.compose.ui.text.font.FontWeight.Bold)

            // Verificamos si hay reserva (Objeto anidado)
            if (lote.booked != null) {
                Surface(color = Color.Yellow.copy(alpha = 0.2f), shape = MaterialTheme.shapes.small) {
                    Text(" Reservado por: ${lote.booked.cliNombre} ", style = MaterialTheme.typography.caption)
                }
            }
        }
    }
}