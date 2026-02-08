package com.alius.gmrstockplus.ui.components

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.alius.gmrstockplus.domain.model.Ratio
import kotlinx.datetime.*

data class RatioData(
    val day: Int,                  // día del mes (1..31) o mes (1..12) si es anual
    var totalWeight: Int           // kilos acumulados ese día o mes
)

/** GENERACIÓN DE DATOS DIARIOS */
fun generateRatioDataFromCollection(ratios: List<Ratio>): List<RatioData> {
    if (ratios.isEmpty()) return emptyList()

    val now = Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault())
    val currentMonth = now.monthNumber
    val currentYear = now.year

    val dailyMap = mutableMapOf<Int, Int>()

    ratios.forEach { ratio ->
        // Validamos que la fecha sea positiva
        if (ratio.ratioDate <= 0L) return@forEach

        val date = try {
            Instant.fromEpochMilliseconds(ratio.ratioDate)
                .toLocalDateTime(TimeZone.currentSystemDefault())
        } catch (e: Exception) {
            println("⚠️ Fecha inválida en ratio: ${ratio.ratioDate}")
            return@forEach
        }

        if (date.year == currentYear && date.monthNumber == currentMonth) {
            val weight = ratio.ratioTotalWeight.toIntOrNull() ?: 0
            dailyMap[date.dayOfMonth] = (dailyMap[date.dayOfMonth] ?: 0) + weight
            println("📅 Día ${date.dayOfMonth}: agregando $weight kg, total acumulado: ${dailyMap[date.dayOfMonth]}")
        }
    }

    val result = dailyMap.entries
        .sortedBy { it.key }
        .map { RatioData(day = it.key, totalWeight = it.value) }

    println("📊 Lista final de RatioData diaria (solo días con datos):")
    result.forEach { println("Día ${it.day}: ${it.totalWeight} kg") }

    return result
}

/** GENERACIÓN DE DATOS MENSUALES (ANUAL) — Incluye meses sin datos */
fun generateRatioDataByMonth(ratios: List<Ratio>): List<RatioData> {
    if (ratios.isEmpty()) {
        val now = Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault())
        return (1..now.monthNumber).map { month ->
            RatioData(day = month, totalWeight = 0)
        }
    }

    val now = Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault())
    val currentYear = now.year
    val currentMonth = now.monthNumber
    val monthlyMap = mutableMapOf<Int, Int>()

    ratios.forEach { ratio ->
        if (ratio.ratioDate <= 0L) return@forEach

        val date = try {
            Instant.fromEpochMilliseconds(ratio.ratioDate)
                .toLocalDateTime(TimeZone.currentSystemDefault())
        } catch (e: Exception) {
            println("⚠️ Fecha inválida en ratio: ${ratio.ratioDate}")
            return@forEach
        }

        if (date.year == currentYear) {
            val weight = ratio.ratioTotalWeight.toIntOrNull() ?: 0
            monthlyMap[date.monthNumber] = (monthlyMap[date.monthNumber] ?: 0) + weight
        }
    }

    // 🔹 Incluimos todos los meses hasta el actual, con 0 si no tienen datos
    val result = (1..currentMonth).map { month ->
        RatioData(day = month, totalWeight = monthlyMap[month] ?: 0)
    }

    println("📊 Lista final de RatioData anual (meses hasta $currentMonth):")
    result.forEach { println("Mes ${it.day}: ${it.totalWeight} kg") }

    return result
}


// Declaración expect: se implementará en Android e iOS
@Composable
expect fun RatioProductionCard(
    modifier: Modifier = Modifier,
    ratioDataList: List<RatioData>,
    isAnnual: Boolean = false
)
