package com.alius.gmrstockplus.data

import com.alius.gmrstockplus.domain.model.BigBags
import com.alius.gmrstockplus.domain.model.Venta
import kotlinx.datetime.Instant

interface VentaRepository {
    suspend fun mostrarTodasLasVentas(): List<Venta>
    suspend fun mostrarLasVentasDeHoy(): List<Venta>
    suspend fun mostrarLasUltimasVentas(): List<Venta>
    suspend fun mostrarVentasPorCliente(cliente: String): List<Venta>
    suspend fun mostrarVentasDelMes(): List<Venta>
    suspend fun mostrarVentasDelAno(): List<Venta>
    suspend fun obtenerVentasPorLote(loteNumber: String): List<Venta>
    suspend fun obtenerUltimosBigBagsDeCliente(loteNumber: String, cliente: String): List<BigBags>
    suspend fun obtenerUltimoClienteYFechaDeBigBag(loteNumber: String, bbNumber: String): Pair<String, Instant>?
    suspend fun obtenerVentasReporteGlobal(cliente: String?, inicio: Instant, fin: Instant): List<Venta>
}

expect fun getVentaRepository(plantId: String): VentaRepository