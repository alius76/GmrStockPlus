package com.alius.gmrstockplus.data

import com.alius.gmrstockplus.data.firestore.FirebaseClient
import com.alius.gmrstockplus.domain.model.Devolucion
import dev.gitlive.firebase.firestore.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.withContext
import kotlinx.datetime.*

class DevolucionRepositoryImpl(private val plantName: String) : DevolucionRepository {

    private val firestore by lazy {
        if (plantName == "P08") FirebaseClient.db08 else FirebaseClient.db07
    }

    private val devolucionCollection by lazy { firestore.collection("devolucion") }

    // --- Mapeo Seguro de Timestamps ---
    private fun DocumentSnapshot.toDevolucionSafe(): Devolucion? {
        return try {
            val dev = this.data<Devolucion>()
            // Intentamos extraer la fecha nativa de Firebase para evitar errores de serialización
            val firebaseDate = try { this.get<Timestamp>("devolucionFecha") } catch (e: Exception) { null }

            dev.copy(
                devolucionFecha = firebaseDate?.let { Instant.fromEpochSeconds(it.seconds, it.nanoseconds) } ?: dev.devolucionFecha
            )
        } catch (e: Exception) {
            println("❌ Error mapeando devolución ${this.id}: ${e.message}")
            null
        }
    }

    override suspend fun obtenerTodasLasDevoluciones(): List<Devolucion> = withContext(Dispatchers.IO) {
        try {
            val snapshot = devolucionCollection.orderBy("devolucionFecha", Direction.DESCENDING).get()
            snapshot.documents.mapNotNull { it.toDevolucionSafe() }
        } catch (e: Exception) {
            println("❌ Error obtenerTodasLasDevoluciones: ${e.message}")
            emptyList()
        }
    }

    override suspend fun agregarDevolucion(devolucion: Devolucion): Boolean = withContext(Dispatchers.IO) {
        try {
            devolucionCollection.add(devolucion)
            true
        } catch (e: Exception) {
            println("❌ Error agregarDevolucion: ${e.message}")
            false
        }
    }

    override suspend fun obtenerDevolucionesPorLote(loteNumber: String): List<Devolucion> = withContext(Dispatchers.IO) {
        try {
            // Unificamos sintaxis de bloque para consistencia
            val snapshot = devolucionCollection.where { "devolucionLote" equalTo loteNumber }.get()
            snapshot.documents.mapNotNull { it.toDevolucionSafe() }
        } catch (e: Exception) { emptyList() }
    }

    override suspend fun obtenerDevolucionesDelMes(): List<Devolucion> = withContext(Dispatchers.IO) {
        try {
            val ahora = Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault())
            val inicioMes = LocalDateTime(ahora.year, ahora.monthNumber, 1, 0, 0).toInstant(TimeZone.currentSystemDefault())

            // VACUNA: Conversión a Timestamp nativo
            val startTs = Timestamp(inicioMes.epochSeconds, inicioMes.nanosecondsOfSecond)

            val snapshot = devolucionCollection
                .where { "devolucionFecha" greaterThanOrEqualTo startTs }
                .orderBy("devolucionFecha", Direction.DESCENDING)
                .get()

            println("✅ [DevolucionRepo] Mes: ${snapshot.documents.size} halladas")
            snapshot.documents.mapNotNull { it.toDevolucionSafe() }
        } catch (e: Exception) {
            println("❌ Error obtenerDevolucionesDelMes: ${e.message}")
            emptyList()
        }
    }

    override suspend fun obtenerDevolucionesDelAnioActual(): List<Devolucion> = withContext(Dispatchers.IO) {
        try {
            val ahora = Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault())
            val inicioAnio = LocalDateTime(ahora.year, 1, 1, 0, 0).toInstant(TimeZone.currentSystemDefault())

            // VACUNA: Conversión a Timestamp nativo
            val startTs = Timestamp(inicioAnio.epochSeconds, inicioAnio.nanosecondsOfSecond)

            val snapshot = devolucionCollection
                .where { "devolucionFecha" greaterThanOrEqualTo startTs }
                .orderBy("devolucionFecha", Direction.DESCENDING)
                .get()

            println("✅ [DevolucionRepo] Año: ${snapshot.documents.size} halladas")
            snapshot.documents.mapNotNull { it.toDevolucionSafe() }
        } catch (e: Exception) {
            println("❌ Error obtenerDevolucionesDelAnioActual: ${e.message}")
            emptyList()
        }
    }
}