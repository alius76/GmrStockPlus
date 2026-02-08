package com.alius.gmrstockplus.presentation.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
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
import com.alius.gmrstockplus.core.utils.formatWeight
import com.alius.gmrstockplus.data.getReprocesarRepository
import com.alius.gmrstockplus.domain.model.Reprocesar
import com.alius.gmrstockplus.ui.components.ReprocesarCard
import com.alius.gmrstockplus.ui.components.DateRangeFilter
import com.alius.gmrstockplus.ui.components.UniversalDatePickerDialog
import com.alius.gmrstockplus.ui.theme.BackgroundColor
import com.alius.gmrstockplus.ui.theme.PrimaryColor
import kotlinx.datetime.*

@OptIn(ExperimentalMaterial3Api::class)
class ReprocesarScreen(private val plantId: String) : Screen {

    @Composable
    override fun Content() {
        val navigator = LocalNavigator.currentOrThrow
        val repository = remember(plantId) { getReprocesarRepository(plantId) }

        // --- 1. Gestión de Fechas (Rango Inicial: Última semana) ---
        val now = remember { Clock.System.now() }
        val systemTZ = remember { TimeZone.currentSystemDefault() }

        val today = remember(now) { now.toLocalDateTime(systemTZ).date }
        val oneWeekAgo = remember(now) {
            now.minus(7, DateTimeUnit.DAY, systemTZ).toLocalDateTime(systemTZ).date
        }

        var startDate by remember { mutableStateOf(oneWeekAgo) }
        var endDate by remember { mutableStateOf(today) }

        var allReprocesos by remember { mutableStateOf<List<Reprocesar>>(emptyList()) }
        var isLoading by remember { mutableStateOf(true) }

        var showStartPicker by remember { mutableStateOf(false) }
        var showEndPicker by remember { mutableStateOf(false) }

        // --- 2. Carga de datos desde Firebase ---
        LaunchedEffect(plantId) {
            isLoading = true
            try {
                allReprocesos = repository.listarReprocesos()
            } catch (e: Exception) {
                allReprocesos = emptyList()
            } finally {
                isLoading = false
            }
        }

        // --- 3. Lógica de Filtrado Reactivo ---
        val filteredReprocesos = remember(allReprocesos, startDate, endDate) {
            allReprocesos.filter { reproceso ->
                val rDate = reproceso.reprocesarFechaReproceso?.toLocalDateTime(TimeZone.UTC)?.date
                if (rDate == null) false
                else rDate in startDate..endDate
            }.sortedByDescending { it.reprocesarFechaReproceso }
        }

        val totalKilos = filteredReprocesos.sumOf { it.reprocesarTotalWeight.toDoubleOrNull() ?: 0.0 }
        val totalBigBags = filteredReprocesos.sumOf { it.bigBagsReprocesados.size }

        // --- 4. Diálogos de Fecha Personalizados ---
        if (showStartPicker) {
            UniversalDatePickerDialog(
                initialDate = startDate,
                onDateSelected = {
                    startDate = it
                    showStartPicker = false
                },
                onDismiss = { showStartPicker = false },
                primaryColor = PrimaryColor
            )
        }

        if (showEndPicker) {
            UniversalDatePickerDialog(
                initialDate = endDate,
                onDateSelected = {
                    endDate = it
                    showEndPicker = false
                },
                onDismiss = { showEndPicker = false },
                primaryColor = PrimaryColor
            )
        }

        Box(modifier = Modifier.fillMaxSize().background(BackgroundColor)) {
            Column {
                // Header Fijo
                HeaderSection(
                    navigator = navigator,
                    totalKilos = totalKilos,
                    totalBigBags = totalBigBags
                )

                // Selector de Rango (Filtro)
                DateRangeFilter(
                    startDate = startDate,
                    endDate = endDate,
                    onSelectStartDate = { showStartPicker = true },
                    onSelectEndDate = { showEndPicker = true },
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                )

                // Contenido de la Lista
                if (isLoading) {
                    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator(color = PrimaryColor)
                    }
                } else if (filteredReprocesos.isEmpty()) {
                    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Text(
                            text = "No hay registros entre $startDate y $endDate",
                            color = Color.Gray,
                            fontSize = 14.sp
                        )
                    }
                } else {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize().padding(horizontal = 16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp),
                        contentPadding = PaddingValues(bottom = 32.dp)
                    ) {
                        items(filteredReprocesos, key = { it.id }) { reproceso ->
                            ReprocesarCard(reproceso)
                        }
                    }
                }
            }
        }
    }

    @Composable
    private fun HeaderSection(
        navigator: cafe.adriel.voyager.navigator.Navigator,
        totalKilos: Double,
        totalBigBags: Int
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = { navigator.pop() }) {
                Icon(Icons.Default.ArrowBack, contentDescription = "Atrás", tint = PrimaryColor)
            }
            Column(modifier = Modifier.weight(1f).padding(start = 8.dp)) {
                Text(
                    text = "Reprocesos",
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.secondary
                )
                Text(
                    text = "${formatWeight(totalKilos)} Kg • $totalBigBags Big Bags",
                    color = Color.Gray,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Medium
                )
            }
        }
    }
}