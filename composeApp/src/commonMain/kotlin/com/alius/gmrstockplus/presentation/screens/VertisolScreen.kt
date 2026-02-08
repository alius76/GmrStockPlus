package com.alius.gmrstockplus.presentation.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Print
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.navigator.LocalNavigator
import cafe.adriel.voyager.navigator.currentOrThrow
import com.alius.gmrstockplus.core.utils.PdfGenerator
import com.alius.gmrstockplus.core.utils.formatWeight
import com.alius.gmrstockplus.data.getLoteRepository
import com.alius.gmrstockplus.domain.model.Vertisol
import com.alius.gmrstockplus.ui.components.VertisolCard
import com.alius.gmrstockplus.ui.theme.BackgroundColor
import com.alius.gmrstockplus.ui.theme.PrimaryColor
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
class VertisolScreen(private val plantId: String) : Screen { // 👈 Cambiado: databaseUrl -> plantId

    @Composable
    override fun Content() {
        val navigator = LocalNavigator.currentOrThrow
        val scope = rememberCoroutineScope()

        // Repositorio actualizado para usar plantId
        val loteRepository = remember(plantId) { getLoteRepository(plantId) }

        var vertisolList by remember { mutableStateOf<List<Vertisol>>(emptyList()) }
        var isLoading by remember { mutableStateOf(true) }
        var totalKilos by remember { mutableStateOf(0.0) }

        // 🔹 Cargar los lotes Vertisol
        LaunchedEffect(plantId) {
            scope.launch {
                try {
                    isLoading = true
                    val loadedVertisol = loteRepository.listarLotesVertisol()
                    vertisolList = loadedVertisol
                    totalKilos = loadedVertisol.sumOf { it.vertisolTotalWeight.toDoubleOrNull() ?: 0.0 }
                } catch (e: Exception) {
                    println("❌ Error cargando Vertisol: ${e.message}")
                } finally {
                    isLoading = false
                }
            }
        }

        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(BackgroundColor)
        ) {
            Column(modifier = Modifier.fillMaxSize()) {
                // 🔹 Header fijo
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(onClick = { navigator.pop() }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Volver", tint = PrimaryColor)
                    }

                    Column(modifier = Modifier.weight(1f).padding(start = 12.dp)) {
                        Text(
                            text = "Lotes en Vertisol",
                            fontSize = 26.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.secondary
                        )
                        Text(
                            text = "Total kilos: ${formatWeight(totalKilos)} Kg",
                            style = MaterialTheme.typography.titleMedium,
                            color = Color.Gray
                        )
                    }

                    // 🔹 BOTÓN DE IMPRIMIR
                    IconButton(
                        onClick = {
                            PdfGenerator.generateVertisolReportPdf(vertisolList, totalKilos)
                        },
                        enabled = !isLoading
                    ) {
                        Icon(
                            imageVector = Icons.Default.Print,
                            contentDescription = "Imprimir",
                            tint = if (!isLoading) PrimaryColor else Color.LightGray
                        )
                    }
                }

                // 🔹 Contenido scrollable
                when {
                    isLoading -> Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .weight(1f),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator(color = PrimaryColor)
                    }

                    vertisolList.isEmpty() -> Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .weight(1f),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "No hay lotes ubicados en Vertisol.",
                            color = Color.DarkGray,
                            fontWeight = FontWeight.Medium
                        )
                    }

                    else -> LazyColumn(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f)
                            .padding(horizontal = 16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        items(vertisolList) { vertisol ->
                            VertisolCard(vertisol = vertisol)
                        }

                        item { Spacer(modifier = Modifier.height(80.dp)) }
                    }
                }
            }
        }
    }
}