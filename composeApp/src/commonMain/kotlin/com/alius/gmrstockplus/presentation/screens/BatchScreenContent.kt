package com.alius.gmrstockplus.presentation.screens

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.snapping.rememberSnapFlingBehavior
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.EventBusy
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.alius.gmrstockplus.data.getLoteRepository
import com.alius.gmrstockplus.data.getHistorialRepository
import com.alius.gmrstockplus.domain.model.LoteModel
import com.alius.gmrstockplus.domain.model.User
import com.alius.gmrstockplus.ui.components.*
import com.alius.gmrstockplus.ui.theme.PrimaryColor
import kotlinx.coroutines.launch

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun BatchScreenContent(user: User, plantId: String) {
    val loteRepository = remember(plantId) { getLoteRepository(plantId) }
    val historialRepository = remember(plantId) { getHistorialRepository(plantId) }

    var lotesHoy by remember { mutableStateOf<List<LoteModel>>(emptyList()) }
    var lotesHistorialHoy by remember { mutableStateOf<List<LoteModel>>(emptyList()) }
    var ultimosLotes by remember { mutableStateOf<List<LoteModel>>(emptyList()) }
    var loading by remember { mutableStateOf(true) }

    val combinedLotesHoy by remember(lotesHoy, lotesHistorialHoy) {
        derivedStateOf { lotesHoy + lotesHistorialHoy }
    }

    val hoyListState = rememberLazyListState()
    val scope = rememberCoroutineScope()

    LaunchedEffect(plantId) {
        loading = true
        try {
            val job1 = launch { lotesHoy = loteRepository.listarLotesCreadosHoy() }
            val job2 = launch { lotesHistorialHoy = historialRepository.listarLotesHistorialDeHoy() }
            val job3 = launch { ultimosLotes = loteRepository.listarUltimosLotes(5) }

            job1.join()
            job2.join()
            job3.join()
        } catch (e: Exception) {
            println("❌ Error: ${e.message}")
        } finally {
            loading = false
        }
    }

    if (loading) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator(color = PrimaryColor)
        }
    } else {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // --- Título Superior ---
            item {
                Column(modifier = Modifier.fillMaxWidth()) {
                    Spacer(modifier = Modifier.height(50.dp)) // Consistencia con ProcessScreen
                    Text(
                        text = "Actividad de hoy",
                        style = MaterialTheme.typography.titleLarge.copy(
                            fontSize = 26.sp,
                            fontWeight = FontWeight.Bold
                        ),
                        color = MaterialTheme.colorScheme.secondary
                    )
                }
            }

            // --- Carrusel de Actividad ---
            item {
                if (combinedLotesHoy.isEmpty()) {
                    Card(
                        modifier = Modifier.fillMaxWidth().height(160.dp),
                        elevation = CardDefaults.cardElevation(6.dp),
                        shape = RoundedCornerShape(16.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .background(Brush.verticalGradient(listOf(Color(0xFF029083), Color(0xFF00BFA5)))),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Icon(Icons.Default.EventBusy, null, tint = Color.White, modifier = Modifier.size(60.dp))
                                Text("Sin actividad hoy", fontSize = 20.sp, color = Color.White)
                            }
                        }
                    }
                } else {
                    LazyRow(
                        state = hoyListState,
                        horizontalArrangement = Arrangement.spacedBy(16.dp),
                        flingBehavior = rememberSnapFlingBehavior(lazyListState = hoyListState),
                        modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp)
                    ) {
                        items(combinedLotesHoy, key = { it.id }) { lote ->
                            val isHistorial = lotesHistorialHoy.any { it.id == lote.id }
                            if (isHistorial) {
                                LoteHistorialItemSmall(lote)
                            } else {
                                LoteItemSmall(lote)
                            }
                        }
                    }
                }
            }

            // --- Sección Stock Reciente ---
            item {
                Text(
                    text = "Últimos lotes en stock",
                    style = MaterialTheme.typography.titleLarge.copy(
                        fontSize = 26.sp,
                        fontWeight = FontWeight.Bold
                    ),
                    color = MaterialTheme.colorScheme.secondary
                )
            }

            items(ultimosLotes, key = { "recent_${it.id}" }) { lote ->
                LoteItem(lote)
            }
        }
    }
}