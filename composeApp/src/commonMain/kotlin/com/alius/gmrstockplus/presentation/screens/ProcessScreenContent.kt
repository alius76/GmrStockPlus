package com.alius.gmrstockplus.presentation.screens

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.HourglassEmpty
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.alius.gmrstockplus.data.getProcessRepository
import com.alius.gmrstockplus.data.getRatioRepository
import com.alius.gmrstockplus.domain.model.Process
import com.alius.gmrstockplus.domain.model.Ratio
import com.alius.gmrstockplus.domain.model.User
import com.alius.gmrstockplus.ui.components.*
import com.alius.gmrstockplus.ui.theme.PrimaryColor
import com.alius.gmrstockplus.ui.theme.TextSecondary
import com.alius.gmrstockplus.core.utils.formatWeight
import kotlinx.coroutines.launch

// 🔑 Definimos la Screen para Voyager si es necesario, o mantenemos la función Composable
@OptIn(ExperimentalFoundationApi::class)
@Composable
fun ProcessScreenContent(user: User, plantId: String) {

    val processRepository = remember(plantId) { getProcessRepository(plantId) }
    val ratioRepository = remember(plantId) { getRatioRepository(plantId) }

    // Estados
    var procesos by remember { mutableStateOf<List<Process>>(emptyList()) }
    var ratiosDelMes by remember { mutableStateOf<List<Ratio>>(emptyList()) }
    var ratiosDelAno by remember { mutableStateOf<List<Ratio>>(emptyList()) }
    var ratioDataList by remember { mutableStateOf<List<RatioData>>(emptyList()) }
    var loading by remember { mutableStateOf(true) }
    var isAnnual by remember { mutableStateOf(false) }

    val scope = rememberCoroutineScope()

    // --- Lógica de actualización de datos del gráfico ---
    val updateRatioDataList = {
        ratioDataList = if (isAnnual) {
            generateRatioDataByMonth(ratiosDelAno)
        } else {
            generateRatioDataFromCollection(ratiosDelMes)
        }
    }

    val totalKilos by derivedStateOf { ratioDataList.sumOf { it.totalWeight } }

    // --- Carga de datos con el nuevo SDK ---
    LaunchedEffect(plantId) {
        loading = true
        try {
            // Lanzamos en paralelo para mayor velocidad
            val procesosJob = launch { procesos = processRepository.listarProcesos() }
            val mesJob = launch { ratiosDelMes = ratioRepository.listarRatiosDelMes() }
            val anoJob = launch { ratiosDelAno = ratioRepository.listarRatiosDelAno() }

            // Esperamos a que todos terminen
            procesosJob.join()
            mesJob.join()
            anoJob.join()

            updateRatioDataList()
        } catch (e: Exception) {

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
            // --- Título ---
            item {
                Column(modifier = Modifier.fillMaxWidth()) {
                    Spacer(modifier = Modifier.height(50.dp))
                    Text(
                        text = "Lotes en progreso",
                        style = MaterialTheme.typography.titleLarge.copy(
                            fontSize = 26.sp,
                            fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.secondary
                    )
                }
            }

            // --- Carrusel de procesos (WIP) ---
            item {
                if (procesos.isEmpty()) {
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
                                Icon(Icons.Filled.HourglassEmpty, null, tint = Color.White, modifier = Modifier.size(60.dp))
                                Text("No hay procesos activos", fontSize = 20.sp, color = Color.White)
                            }
                        }
                    }
                } else {
                    LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(16.dp),
                        modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp)
                    ) {
                        items(procesos) { proceso -> ProcessItem(proceso = proceso) }
                    }
                }
            }

            // --- Sección Producción ---
            item {
                Column(modifier = Modifier.fillMaxWidth()) {
                    Text(
                        text = "Gráfico de producción",
                        style = MaterialTheme.typography.titleLarge.copy(fontSize = 26.sp, fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.secondary
                    )
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Total kilos: ${formatWeight(totalKilos)} Kg",
                            style = MaterialTheme.typography.titleMedium,
                            color = TextSecondary
                        )
                        MySegmentedButton(
                            options = listOf("Mes", "Año"),
                            selectedIndex = if (isAnnual) 1 else 0,
                            onSelect = {
                                isAnnual = it == 1
                                updateRatioDataList()
                            }
                        )
                    }
                }
            }

            // --- Gráfico de Producción ---
            item {
                RatioProductionCard(
                    modifier = Modifier.fillMaxWidth().height(250.dp),
                    ratioDataList = ratioDataList,
                    isAnnual = isAnnual
                )
            }
        }
    }
}