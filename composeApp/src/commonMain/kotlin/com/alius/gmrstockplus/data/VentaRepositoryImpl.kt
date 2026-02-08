package com.alius.gmrstockplus.data

import com.alius.gmrstockplus.data.firestore.FirebaseClient
import com.alius.gmrstockplus.domain.model.*
import dev.gitlive.firebase.firestore.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.withContext
import kotlinx.datetime.*
import kotlin.time.Duration.Companion.days

class VentaRepositoryImpl(private val plantName: String) : VentaRepository {

    private val firestore by lazy {
        if (plantName == "P08") FirebaseClient.db08 else FirebaseClient.db07
    }

    private val ventaCollection by lazy { firestore.collection("venta") }

    // --- Mapeo Seguro de Timestamps ---
    private fun DocumentSnapshot.toVentaSafe(): Venta? {
        return try {
            val venta = this.data<Venta>()
            // Intentamos extraer la fecha nativa de Firebase si existe
            val firebaseDate = try { this.get<Timestamp>("ventaFecha") } catch (e: Exception) { null }

            venta.copy(
                ventaFecha = firebaseDate?.let { Instant.fromEpochSeconds(it.seconds, it.nanoseconds) } ?: venta.ventaFecha
            )
        } catch (e: Exception) {
            println("❌ Error mapeando venta ${this.id}: ${e.message}")
            null
        }
    }

    // --- Consultas Implementadas ---

    override suspend fun mostrarTodasLasVentas(): List<Venta> = withContext(Dispatchers.IO) {
        try {
            val snapshot = ventaCollection.orderBy("ventaFecha", Direction.DESCENDING).get()
            snapshot.documents.mapNotNull { it.toVentaSafe() }
        } catch (e: Exception) { emptyList() }
    }

    override suspend fun mostrarLasVentasDeHoy(): List<Venta> = withContext(Dispatchers.IO) {
        val hoy = Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault()).date
        val inicio = hoy.atStartOfDayIn(TimeZone.currentSystemDefault())
        val fin = inicio.plus(1.days)
        obtenerVentasPorRango(inicio, fin)
    }

    override suspend fun mostrarLasUltimasVentas(): List<Venta> = withContext(Dispatchers.IO) {
        try {
            val snapshot = ventaCollection
                .orderBy("ventaFecha", Direction.DESCENDING)
                .limit(5)
                .get()
            snapshot.documents.mapNotNull { it.toVentaSafe() }
        } catch (e: Exception) { emptyList() }
    }

    override suspend fun mostrarVentasDelMes(): List<Venta> = withContext(Dispatchers.IO) {
        val ahora = Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault())
        val inicio = LocalDateTime(ahora.year, ahora.monthNumber, 1, 0, 0).toInstant(TimeZone.currentSystemDefault())
        obtenerVentasPorRango(inicio, Clock.System.now())
    }

    override suspend fun mostrarVentasDelAno(): List<Venta> = withContext(Dispatchers.IO) {
        val ahora = Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault())
        val inicio = LocalDateTime(ahora.year, 1, 1, 0, 0).toInstant(TimeZone.currentSystemDefault())
        obtenerVentasPorRango(inicio, Clock.System.now())
    }

    override suspend fun mostrarVentasPorCliente(cliente: String): List<Venta> = withContext(Dispatchers.IO) {
        try {
            // Si el cliente viene vacío (como en tu Screen para 'ventasDelAnio'),
            // no filtramos por cliente, solo ordenamos para traer todo el histórico.
            val query = if (cliente.isNotBlank()) {
                ventaCollection.where { "ventaCliente" equalTo cliente }
            } else {
                ventaCollection
            }

            val snapshot = query.orderBy("ventaFecha", Direction.DESCENDING).get()
            snapshot.documents.mapNotNull { it.toVentaSafe() }
        } catch (e: Exception) {
            println("❌ Error en mostrarVentasPorCliente: ${e.message}")
            emptyList()
        }
    }

    override suspend fun obtenerVentasPorLote(loteNumber: String): List<Venta> = withContext(Dispatchers.IO) {
        try {
            val snapshot = ventaCollection.where { "ventaLote" equalTo loteNumber }.get()
            snapshot.documents.mapNotNull { it.toVentaSafe() }
        } catch (e: Exception) { emptyList() }
    }

    // --- Lógica de Negocio ---

    override suspend fun obtenerUltimosBigBagsDeCliente(loteNumber: String, cliente: String): List<BigBags> {
        val ventas = obtenerVentasPorLote(loteNumber).filter { it.ventaCliente == cliente }
        return ventas
            .flatMap { v -> v.ventaBigbags.map { bb -> Triple(bb.ventaBbNumber, bb.ventaBbWeight, v.ventaFecha) } }
            .groupBy { it.first }
            .mapValues { it.value.maxByOrNull { reg -> reg.third ?: Instant.DISTANT_PAST } }
            .values.filterNotNull()
            .map { BigBags(bbNumber = it.first, bbWeight = it.second, bbStatus = "o", bbLocation = "") }
    }

    override suspend fun obtenerUltimoClienteYFechaDeBigBag(loteNumber: String, bbNumber: String): Pair<String, Instant>? {
        val ventas = obtenerVentasPorLote(loteNumber)
        return ventas.filter { v -> v.ventaBigbags.any { it.ventaBbNumber == bbNumber } }
            .maxByOrNull { it.ventaFecha ?: Instant.DISTANT_PAST }
            ?.let { v -> v.ventaCliente to v.ventaFecha!! }
    }

    override suspend fun obtenerVentasReporteGlobal(
        cliente: String?,
        inicio: Instant,
        fin: Instant
    ): List<Venta> = withContext(Dispatchers.IO) {
        try {
            val startTs = Timestamp(inicio.epochSeconds, inicio.nanosecondsOfSecond)
            val endTs = Timestamp(fin.epochSeconds, fin.nanosecondsOfSecond)

            // 1. Solo filtramos por FECHA en Firestore
            val snapshot = ventaCollection
                .where { "ventaFecha" greaterThanOrEqualTo startTs }
                .where { "ventaFecha" lessThanOrEqualTo endTs }
                .get()

            val todasLasVentas = snapshot.documents.mapNotNull { it.toVentaSafe() }

            // 2. Filtramos el CLIENTE manualmente en Kotlin
            return@withContext if (cliente.isNullOrBlank() || cliente == "TODOS LOS CLIENTES") {
                todasLasVentas
            } else {
                todasLasVentas.filter { it.ventaCliente.trim().equals(cliente.trim(), ignoreCase = true) }
            }
        } catch (e: Exception) {
            println("❌ Error en reporte global: ${e.message}")
            emptyList()
        }
    }

    // --- Helper Interno de Rango ---
    private suspend fun obtenerVentasPorRango(inicio: Instant, fin: Instant): List<Venta> {
        return withContext(Dispatchers.IO) {
            try {
                // Conversión obligatoria a Timestamp nativo para que el 'where' no devuelva 0
                val startTs = Timestamp(inicio.epochSeconds, inicio.nanosecondsOfSecond)
                val endTs = Timestamp(fin.epochSeconds, fin.nanosecondsOfSecond)

                val snapshot = ventaCollection
                    .where { "ventaFecha" greaterThanOrEqualTo startTs }
                    .where { "ventaFecha" lessThan endTs }
                    .orderBy("ventaFecha", Direction.DESCENDING)
                    .get()

                println("✅ [VentaRepo] Docs hallados en rango: ${snapshot.documents.size}")
                snapshot.documents.mapNotNull { it.toVentaSafe() }
            } catch (e: Exception) {
                println("❌ Error en obtenerVentasPorRango: ${e.message}")
                emptyList()
            }
        }
    }
}