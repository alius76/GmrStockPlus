package com.alius.gmrstockplus.presentation.screens

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.MoneyOff
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.alius.gmrstockplus.core.utils.formatWeight
import com.alius.gmrstockplus.data.VentaRepositoryImpl
import com.alius.gmrstockplus.data.DevolucionRepositoryImpl
import com.alius.gmrstockplus.domain.model.User
import com.alius.gmrstockplus.domain.model.Venta
import com.alius.gmrstockplus.domain.model.Devolucion
import com.alius.gmrstockplus.ui.components.*
import com.alius.gmrstockplus.ui.theme.PrimaryColor
import com.alius.gmrstockplus.ui.theme.TextSecondary
import kotlinx.coroutines.launch

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun TransferScreenContent(user: User, plantId: String) {
    // Instanciamos los repositorios usando el SDK nativo pasando el plantId (P07/P08)
    val ventaRepository = remember(plantId) { VentaRepositoryImpl(plantId) }
    val devolucionRepository = remember(plantId) { DevolucionRepositoryImpl(plantId) }

    // Estados de Ventas (Originales)
    var ventasHoy by remember { mutableStateOf<List<Venta>>(emptyList()) }
    var ultimasVentas by remember { mutableStateOf<List<Venta>>(emptyList()) }
    var ventasDelMes by remember { mutableStateOf<List<Venta>>(emptyList()) }
    var ventasDelAnio by remember { mutableStateOf<List<Venta>>(emptyList()) }

    // Estados de Devoluciones (Originales)
    var devolucionesDelMes by remember { mutableStateOf<List<Devolucion>>(emptyList()) }
    var devolucionesDelAnio by remember { mutableStateOf<List<Devolucion>>(emptyList()) }

    // Estados de UI y control (Originales)
    var ventaDataList by remember { mutableStateOf<List<VentaData>>(emptyList()) }
    var loading by remember { mutableStateOf(true) }
    var isAnnual by remember { mutableStateOf(false) }

    val scope = rememberCoroutineScope()
    val hoyListState = rememberLazyListState()

    // Tu helper original de cálculo de devoluciones
    fun calculateTotalDevoluciones(devoluciones: List<Devolucion>): Double {
        var total = 0.0
        println("\n--- INICIO CÁLCULO DEVOLUCIONES ---")
        devoluciones.forEachIndexed { index, devolucion ->
            val pesoString = devolucion.devolucionPesoTotal
            val pesoDoble = pesoString?.replace(",", ".")?.toDoubleOrNull() ?: 0.0
            total += pesoDoble
            println("DEVOLUCIÓN #${index + 1}: Fecha=${devolucion.devolucionFecha}, Peso String='$pesoString', Peso Double=$pesoDoble")
        }
        println("TOTAL CALCULADO: $total")
        println("--- FIN CÁLCULO DEVOLUCIONES ---\n")
        return total
    }

    // Tu métrica derivada original
    val totalKilosDevueltosActual by derivedStateOf {
        val listaUsada = if (isAnnual) "DEVOLUCIONES DEL AÑO" else "DEVOLUCIONES DEL MES"
        val kilos = if (isAnnual) {
            calculateTotalDevoluciones(devolucionesDelAnio)
        } else {
            calculateTotalDevoluciones(devolucionesDelMes)
        }
        println("RENDERIZANDO: Selector en ${if (isAnnual) "AÑO" else "MES"}. Usando lista: $listaUsada. Kilos mostrados: $kilos Kg")
        kilos
    }

    // Tu actualización de gráfico original
    fun updateVentaDataList() {
        ventaDataList = if (isAnnual) {
            generateVentaDataByMonth(ventasDelAnio)
        } else {
            generateVentaDataFromCollection(ventasDelMes)
        }
    }

    val totalKilosVentas by derivedStateOf { ventaDataList.sumOf { it.totalWeight.toDouble() } }

    LaunchedEffect(plantId) {
        loading = true
        scope.launch {
            // Cargar datos usando el repositorio SDK (mismos métodos que la API)
            ventasHoy = ventaRepository.mostrarLasVentasDeHoy()
            ultimasVentas = ventaRepository.mostrarLasUltimasVentas()
            ventasDelMes = ventaRepository.mostrarVentasDelMes()
            ventasDelAnio = ventaRepository.mostrarVentasPorCliente("") // Trae todas las del año/históricas

            // Cargar datos de Devoluciones
            devolucionesDelMes = devolucionRepository.obtenerDevolucionesDelMes()
            println("DEBUG LOAD: devolucionesDelMes cargadas: ${devolucionesDelMes.size} elementos.")

            devolucionesDelAnio = devolucionRepository.obtenerDevolucionesDelAnioActual()
            println("DEBUG LOAD: devolucionesDelAnio (Todas las devoluciones) cargadas: ${devolucionesDelAnio.size} elementos.")

            devolucionesDelAnio.firstOrNull()?.let {
                println("DEBUG INSPECCIÓN: Primer Devolución del Año -> Fecha: ${it.devolucionFecha}, Peso: ${it.devolucionPesoTotal}")
            }

            updateVentaDataList()
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
            // --- Ventas de Hoy (Original) ---
            item {
                Column(modifier = Modifier.fillMaxWidth()) {
                    Spacer(modifier = Modifier.height(50.dp))
                    Text(
                        text = "Ventas de hoy",
                        style = MaterialTheme.typography.titleLarge.copy(fontSize = 26.sp, fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.secondary
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                }

                if (ventasHoy.isEmpty()) {
                    // Mantenemos tu Box/Card de "Sin ventas hoy" exacta
                    Box(modifier = Modifier.fillMaxWidth().height(180.dp), contentAlignment = Alignment.Center) {
                        Card(
                            modifier = Modifier.fillMaxWidth().height(160.dp),
                            elevation = CardDefaults.cardElevation(defaultElevation = 6.dp),
                            shape = RoundedCornerShape(16.dp),
                            colors = CardDefaults.cardColors(containerColor = Color.Transparent)
                        ) {
                            Box(
                                modifier = Modifier.fillMaxSize().background(
                                    brush = Brush.verticalGradient(colors = listOf(Color(0xFF029083), Color(0xFF00BFA5)))
                                ),
                                contentAlignment = Alignment.Center
                            ) {
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Icon(Icons.Outlined.MoneyOff, null, tint = Color.White, modifier = Modifier.size(60.dp))
                                    Spacer(modifier = Modifier.height(8.dp))
                                    Text("Sin ventas hoy", fontSize = 20.sp, fontWeight = FontWeight.Bold, color = Color.White)
                                }
                            }
                        }
                    }
                } else {
                    LazyRow(
                        state = hoyListState,
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        items(ventasHoy) { venta -> VentaItemSmall(venta) }
                    }
                }
            }

            // --- Gráfico de Ventas (Original) ---
            item {
                Column(modifier = Modifier.fillMaxWidth()) {
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = "Gráfico de ventas",
                        style = MaterialTheme.typography.titleLarge.copy(fontSize = 26.sp, fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.secondary
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text("Total kilos: ${formatWeight(totalKilosVentas)} Kg", style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Medium), color = TextSecondary)
                            Text("Devoluciones: ${formatWeight(totalKilosDevueltosActual)} Kg", style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Medium), color = Color.Gray)
                        }
                        MySegmentedButton(
                            options = listOf("Mes", "Año"),
                            selectedIndex = if (isAnnual) 1 else 0,
                            onSelect = {
                                isAnnual = it == 1
                                updateVentaDataList()
                            }
                        )
                    }
                    Spacer(modifier = Modifier.height(12.dp))
                }
                VentaChartCard(modifier = Modifier.fillMaxWidth().height(250.dp), ventaDataList = ventaDataList, isAnnual = isAnnual)
            }

            // --- Últimas Ventas (Original) ---
            item {
                Column(modifier = Modifier.fillMaxWidth()) {
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = "Últimas ventas",
                        style = MaterialTheme.typography.titleLarge.copy(fontSize = 26.sp, fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.secondary
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                }
            }
            items(ultimasVentas) { venta -> VentaItem(venta) }
        }
    }
}