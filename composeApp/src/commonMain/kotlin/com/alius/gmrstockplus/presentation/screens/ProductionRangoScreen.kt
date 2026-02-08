package com.alius.gmrstockplus.presentation.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Divider
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
import com.alius.gmrstockplus.data.getHistorialRepository
import com.alius.gmrstockplus.data.getLoteRepository
import com.alius.gmrstockplus.data.getRatioRepository
import com.alius.gmrstockplus.domain.model.Ratio
import com.alius.gmrstockplus.ui.components.DateRangeFilter
import com.alius.gmrstockplus.ui.components.ExpandableProduccionCard
import com.alius.gmrstockplus.ui.components.ProduccionTrendChart
import com.alius.gmrstockplus.ui.components.UniversalDatePickerDialog
import com.alius.gmrstockplus.ui.theme.BackgroundColor
import com.alius.gmrstockplus.ui.theme.PrimaryColor
import com.alius.gmrstockplus.ui.theme.WarningColor
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.datetime.*

// Modelos de datos fuera de la clase para que sean accesibles por otros componentes
data class ProduccionDiaria(
    val fecha: LocalDate,
    val totalKilos: Double,
    val cantidadRegistros: Int
)

data class ProduccionMensual(
    val mesLabel: String,
    val totalKilos: Double,
    val cantidadRegistros: Int,
    val fechaReferencia: LocalDate,
    val listaRatios: List<Ratio>
)

@OptIn(ExperimentalMaterial3Api::class)
class ProduccionRangoScreen(
    private val plantId: String // 👈 Cambio: de databaseUrl a plantId
) : Screen {

    @Composable
    override fun Content() {
        val navigator = LocalNavigator.currentOrThrow

        // --- Repositorios actualizados para usar plantId ---
        val ratioRepository = remember(plantId) { getRatioRepository(plantId) }
        val loteRepository = remember(plantId) { getLoteRepository(plantId) }
        val historialRepository = remember(plantId) { getHistorialRepository(plantId) }

        // --- Estados de Fecha ---
        val today = remember { Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault()).date }
        var startDate by remember { mutableStateOf(today.minus(DatePeriod(days = 7))) }
        var endDate by remember { mutableStateOf(today) }

        // --- Estados de Datos ---
        var ratios by remember { mutableStateOf<List<Ratio>>(emptyList()) }
        var isLoading by remember { mutableStateOf(false) }

        // --- Mapa para traducir ID técnico -> Número de Lote (number) ---
        var loteNombresMap by remember { mutableStateOf<Map<String, String>>(emptyMap()) }

        // --- Diálogos ---
        var showStartPicker by remember { mutableStateOf(false) }
        var showEndPicker by remember { mutableStateOf(false) }

        // --- Lógica de Carga ---
        LaunchedEffect(startDate, endDate) {
            isLoading = true
            try {
                // 1. Obtener los ratios del rango seleccionado
                val fetchedRatios = ratioRepository.listarRatiosPorRango(startDate, endDate)
                ratios = fetchedRatios

                // 2. Extraer IDs únicos de lotes para buscar sus nombres
                val loteIdsNecesarios = fetchedRatios.map { it.ratioLoteId }
                    .filter { it.isNotBlank() }
                    .distinct()

                // 3. Búsqueda en Cascada (Lote -> Historial) en paralelo
                val deferredResultados = loteIdsNecesarios.map { id ->
                    async {
                        // Intento 1: Buscar en colección 'lote' (stock)
                        var loteEncontrado = loteRepository.getLoteById(id)

                        // Intento 2: Si no está en stock, buscar en 'historial' (vendidos)
                        if (loteEncontrado == null) {
                            loteEncontrado = historialRepository.getLoteHistorialById(id)
                        }

                        // Retornar el par ID to Number
                        id to loteEncontrado?.number
                    }
                }

                val resultados = deferredResultados.awaitAll()

                // 4. Crear el mapa de traducción ID -> Number
                loteNombresMap = resultados.mapNotNull { (id, number) ->
                    number?.let { id to it }
                }.toMap()

            } catch (e: Exception) {
                ratios = emptyList()
            } finally {
                isLoading = false
            }
        }

        // --- Procesamiento de Datos ---
        val datosAgrupados = remember(ratios) { agruparRatiosPorDia(ratios) }
        val datosMensuales = remember(ratios) { agruparRatiosPorMes(ratios) }

        val totalKilosGlobal by remember(ratios) {
            derivedStateOf { ratios.sumOf { it.ratioTotalWeight.toDoubleOrNull() ?: 0.0 } }
        }

        val diasActivos by remember(datosAgrupados) {
            derivedStateOf { datosAgrupados.size }
        }
        val promedioDiario = remember(totalKilosGlobal, diasActivos) {
            if (diasActivos > 0) totalKilosGlobal / diasActivos else 0.0
        }

        Scaffold(
            containerColor = BackgroundColor,
            topBar = {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(BackgroundColor)
                        .padding(horizontal = 16.dp, vertical = 12.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            IconButton(onClick = { navigator.pop() }) {
                                Icon(Icons.Default.ArrowBack, "Atrás", tint = PrimaryColor)
                            }
                            Column(modifier = Modifier.padding(start = 8.dp)) {
                                Text(
                                    "Producción",
                                    fontSize = 26.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.secondary
                                )
                                Text(
                                    "Seleccione rango de fechas",
                                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Medium), color = Color.Gray)

                            }
                        }
                        IconButton(onClick = {
                            if (ratios.isNotEmpty()) {
                                val startDay = startDate.dayOfMonth.toString().padStart(2, '0')
                                val startMonth = startDate.monthNumber.toString().padStart(2, '0')
                                val endDay = endDate.dayOfMonth.toString().padStart(2, '0')
                                val endMonth = endDate.monthNumber.toString().padStart(2, '0')

                                val rangeStr = "$startDay/$startMonth/${startDate.year} al $endDay/$endMonth/${endDate.year}"

                                PdfGenerator.generateProductionReportPdf(
                                    ratios = ratios,
                                    totalKilos = totalKilosGlobal,
                                    promedio = promedioDiario,
                                    dateRange = rangeStr,
                                    loteNombresMap = loteNombresMap
                                )
                            }
                        }) {
                            Icon(
                                imageVector = Icons.Default.Print,
                                contentDescription = "Imprimir",
                                tint = PrimaryColor,
                                modifier = Modifier.size(28.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    DateRangeFilter(
                        startDate = startDate,
                        endDate = endDate,
                        onSelectStartDate = { showStartPicker = true },
                        onSelectEndDate = { showEndPicker = true }
                    )

                    Spacer(modifier = Modifier.height(8.dp))
                    Divider(color = Color.LightGray.copy(alpha = 0.3f))
                }
            }
        ) { paddingValues ->
            Box(modifier = Modifier.padding(paddingValues).fillMaxSize()) {
                if (isLoading) {
                    CircularProgressIndicator(modifier = Modifier.align(Alignment.Center), color = PrimaryColor)
                } else if (ratios.isEmpty()) {
                    Text("No hay datos para este rango", modifier = Modifier.align(Alignment.Center), color = Color.Gray)
                } else {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(16.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        item {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                KPICard(Modifier.weight(1f), "TOTAL", "${formatWeight(totalKilosGlobal)} kg", PrimaryColor)
                                KPICard(Modifier.weight(1f), "DÍAS ACTIVOS", "$diasActivos", WarningColor)
                                KPICard(Modifier.weight(1f), "MEDIA", "${formatWeight(promedioDiario)} kg", Color(0xFF2196F3))
                            }
                        }

                        item {
                            Text(
                                "Tendencia de producción",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.secondary,
                                modifier = Modifier.padding(top = 8.dp, bottom = 8.dp)
                            )
                            ProduccionTrendChart(
                                datos = datosAgrupados,
                                modifier = Modifier.padding(top = 8.dp)
                            )
                        }

                        item {
                            Text(
                                "Desglose mensual",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.secondary,
                                modifier = Modifier.padding(top = 8.dp)
                            )
                        }

                        items(datosMensuales.reversed()) { mensual ->
                            val porcentaje = if (totalKilosGlobal > 0) {
                                (mensual.totalKilos / totalKilosGlobal).toFloat()
                            } else 0f

                            ExpandableProduccionCard(
                                mensual = mensual,
                                porcentaje = porcentaje,
                                loteNombresMap = loteNombresMap
                            )
                        }

                        item { Spacer(modifier = Modifier.height(50.dp)) }
                    }
                }
            }

            if (showStartPicker) {
                UniversalDatePickerDialog(startDate, { startDate = it }, { showStartPicker = false })
            }
            if (showEndPicker) {
                UniversalDatePickerDialog(endDate, { endDate = it }, { showEndPicker = false })
            }
        }
    }

    private fun agruparRatiosPorDia(ratios: List<Ratio>): List<ProduccionDiaria> {
        return ratios.groupBy {
            Instant.fromEpochMilliseconds(it.ratioDate).toLocalDateTime(TimeZone.currentSystemDefault()).date
        }.map { (fecha, lista) ->
            ProduccionDiaria(
                fecha = fecha,
                totalKilos = lista.sumOf { it.ratioTotalWeight.toDoubleOrNull() ?: 0.0 },
                cantidadRegistros = lista.size
            )
        }.sortedBy { it.fecha }
    }

    private fun agruparRatiosPorMes(ratios: List<Ratio>): List<ProduccionMensual> {
        val mesesEspanol = mapOf(
            Month.JANUARY to "Enero",
            Month.FEBRUARY to "Febrero",
            Month.MARCH to "Marzo",
            Month.APRIL to "Abril",
            Month.MAY to "Mayo",
            Month.JUNE to "Junio",
            Month.JULY to "Julio",
            Month.AUGUST to "Agosto",
            Month.SEPTEMBER to "Septiembre",
            Month.OCTOBER to "Octubre",
            Month.NOVEMBER to "Noviembre",
            Month.DECEMBER to "Diciembre"
        )

        return ratios.groupBy {
            val date = Instant.fromEpochMilliseconds(it.ratioDate).toLocalDateTime(TimeZone.currentSystemDefault()).date
            "${mesesEspanol[date.month]} ${date.year}"
        }.map { (mesAnio, lista) ->
            ProduccionMensual(
                mesLabel = mesAnio,
                totalKilos = lista.sumOf { it.ratioTotalWeight.toDoubleOrNull() ?: 0.0 },
                cantidadRegistros = lista.size,
                fechaReferencia = Instant.fromEpochMilliseconds(lista.first().ratioDate).toLocalDateTime(TimeZone.currentSystemDefault()).date,
                listaRatios = lista.sortedByDescending { it.ratioDate }
            )
        }.sortedBy { it.fechaReferencia }
    }

    @Composable
    private fun KPICard(modifier: Modifier, title: String, value: String, color: Color) {
        Card(
            modifier = modifier,
            colors = CardDefaults.cardColors(containerColor = Color.White),
            elevation = CardDefaults.cardElevation(2.dp),
            shape = RoundedCornerShape(12.dp)
        ) {
            Column(
                modifier = Modifier.padding(vertical = 16.dp).fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(title, fontSize = 11.sp, color = Color.Gray, fontWeight = FontWeight.Bold)
                Text(value, fontSize = 15.sp, fontWeight = FontWeight.ExtraBold, color = color)
            }
        }
    }
}