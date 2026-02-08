package com.alius.gmrstockplus.ui.components

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.alius.gmrstockplus.domain.model.Venta
import kotlinx.datetime.*

data class VentaData(
    val day: Int,                  // día del mes (1..31) o mes (1..12) si es anual
    var totalWeight: Int           // kilos acumulados ese día o mes
)

/** GENERACIÓN DE DATOS DIARIOS */
fun generateVentaDataFromCollection(ventas: List<Venta>): List<VentaData> {
    if (ventas.isEmpty()) return emptyList()

    val now = Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault())
    val currentMonth = now.monthNumber
    val currentYear = now.year

    val dailyMap = mutableMapOf<Int, Int>()

    ventas.forEach { venta ->
        val date = venta.ventaFecha?.toLocalDateTime(TimeZone.currentSystemDefault()) ?: return@forEach
        if (date.year == currentYear && date.monthNumber == currentMonth) {
            val weight = venta.ventaPesoTotal?.toIntOrNull() ?: 0
            dailyMap[date.dayOfMonth] = (dailyMap[date.dayOfMonth] ?: 0) + weight
        }
    }

    return dailyMap.entries
        .sortedBy { it.key }
        .map { VentaData(day = it.key, totalWeight = it.value) }
}

/** GENERACIÓN DE DATOS MENSUALES (ANUAL) */
fun generateVentaDataByMonth(ventas: List<Venta>): List<VentaData> {
    if (ventas.isEmpty()) return emptyList()

    val currentYear = Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault()).year
    val currentMonth = Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault()).monthNumber
    val monthlyMap = mutableMapOf<Int, Int>() // key = mes con datos

    ventas.forEach { venta ->
        val date = venta.ventaFecha?.toLocalDateTime(TimeZone.currentSystemDefault()) ?: return@forEach
        if (date.year == currentYear) {
            val weight = venta.ventaPesoTotal?.toIntOrNull() ?: 0
            monthlyMap[date.monthNumber] = (monthlyMap[date.monthNumber] ?: 0) + weight
        }
    }

    // Llenar todos los meses hasta el actual, aunque no tengan datos
    val result = (1..currentMonth).map { month ->
        VentaData(day = month, totalWeight = monthlyMap[month] ?: 0)
    }

    return result
}

// Declaración expect
@Composable
expect fun VentaChartCard(
    modifier: Modifier = Modifier,
    ventaDataList: List<VentaData>,
    isAnnual: Boolean = false
)
