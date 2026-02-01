package com.alius.gmrstockplus.data

import com.alius.gmrstockplus.domain.model.Ratio
import kotlinx.datetime.LocalDate

interface RatioRepository {
    suspend fun listarRatiosDelMes(): List<Ratio>
    suspend fun listarRatiosDelDia(): List<Ratio>
    suspend fun listarRatiosDelAno(): List<Ratio>
    suspend fun listarRatiosPorRango(inicio: LocalDate, fin: LocalDate): List<Ratio>
    suspend fun listarRatiosUltimos12Meses(): List<Ratio>
}

// Función para obtener la implementación inyectando el ID de la planta
expect fun getRatioRepository(plantId: String): RatioRepository


