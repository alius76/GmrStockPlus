package com.alius.gmrstockplus.data

import com.alius.gmrstockplus.domain.model.Ratio
import kotlinx.datetime.LocalDate

interface RatioRepository {
    suspend fun listarRatiosDelDia(): List<Ratio>
    suspend fun listarRatiosDelMes(): List<Ratio>
    suspend fun listarRatiosDelAno(): List<Ratio>
    suspend fun listarRatiosPorRango(inicio: LocalDate, fin: LocalDate): List<Ratio>
    suspend fun listarRatiosUltimos12Meses(): List<Ratio>
    suspend fun obtenerProgresoMensual(): Float
}

// ✅ Asegúrate de que el nombre sea 'plantId' para que coincida con la implementación
expect fun getRatioRepository(plantId: String): RatioRepository

