package com.alius.gmrstockplus.data

import com.alius.gmrstockplus.domain.model.Devolucion

interface DevolucionRepository {
    suspend fun obtenerTodasLasDevoluciones(): List<Devolucion>
    suspend fun agregarDevolucion(devolucion: Devolucion): Boolean
    suspend fun obtenerDevolucionesPorLote(loteNumber: String): List<Devolucion>
    suspend fun obtenerDevolucionesDelMes(): List<Devolucion>
    suspend fun obtenerDevolucionesDelAnioActual(): List<Devolucion>
}

expect fun getDevolucionRepository(plantId: String): DevolucionRepository
