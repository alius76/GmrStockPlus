package com.alius.gmrstockplus.data

import com.alius.gmrstockplus.data.firestore.FirebaseClient
import com.alius.gmrstockplus.domain.model.*
import dev.gitlive.firebase.firestore.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.withContext
import kotlinx.datetime.*

class HistorialRepositoryImpl(private val plantName: String) : HistorialRepository {

    private val firestore by lazy {
        if (plantName == "P08") FirebaseClient.db08 else FirebaseClient.db07
    }

    private val historialCollection by lazy {
        firestore.collection("historial")
    }

    private fun DocumentSnapshot.toHistorialModelSafe(): LoteModel? {
        return try {
            val lote = this.data<LoteModel>()
            val firebaseDate = try { this.get<Timestamp>("date") } catch (e: Exception) { null }
            val firebaseCreatedAt = try { this.get<Timestamp>("createdAt") } catch (e: Exception) { null }

            lote.copy(
                id = this.id,
                date = firebaseDate?.let { Instant.fromEpochSeconds(it.seconds, it.nanoseconds) } ?: lote.date,
                createdAt = firebaseCreatedAt?.let { Instant.fromEpochSeconds(it.seconds, it.nanoseconds) } ?: lote.createdAt
            )
        } catch (e: Exception) {
            println("❌ [Historial] Error mapeando: ${this.id} -> ${e.message}")
            null
        }
    }

    override suspend fun listarLotesHistorialDeHoy(): List<LoteModel> = withContext(Dispatchers.IO) {
        try {
            val hoy = Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault()).date
            val inicio = hoy.atStartOfDayIn(TimeZone.currentSystemDefault())
            val fin = inicio.plus(1, DateTimeUnit.DAY, TimeZone.currentSystemDefault())

            // CORRECCIÓN CRÍTICA PARA iOS: Convertir Instant a Timestamp nativo
            val startTs = Timestamp(inicio.epochSeconds, inicio.nanosecondsOfSecond)
            val endTs = Timestamp(fin.epochSeconds, fin.nanosecondsOfSecond)

            val snapshot = historialCollection
                .where { "createdAt" greaterThanOrEqualTo startTs }
                .where { "createdAt" lessThan endTs }
                .get()

            snapshot.documents.mapNotNull { it.toHistorialModelSafe() }
        } catch (e: Exception) {
            println("❌ Error listarLotesHistorialDeHoy: ${e.message}")
            emptyList()
        }
    }

    // --- CORRECCIÓN PARA CARGA DE DATOS (POST/PATCH) ---

    override suspend fun agregarLote(lote: LoteModel): Boolean = withContext(Dispatchers.IO) {
        try {
            // Convertimos el modelo a Map para manejar las fechas manualmente y evitar crash en iOS
            val loteData = lote.toFirestoreMap()
            historialCollection.add(loteData)
            true
        } catch (e: Exception) {
            println("❌ Error al agregar a historial: ${e.message}")
            false
        }
    }

    override suspend fun agregarYLigaroLote(lote: LoteModel): String? = withContext(Dispatchers.IO) {
        try {
            val loteData = lote.toFirestoreMap()
            val docRef = historialCollection.add(loteData)
            val newId = docRef.id
            docRef.update("id" to newId)
            newId
        } catch (e: Exception) {
            println("❌ Error en agregarYLigaroLote: ${e.message}")
            null
        }
    }

    // Helper para evitar el error de "Unsupported type: Instant" al SUBIR datos
    private fun LoteModel.toFirestoreMap(): Map<String, Any?> {
        return mapOf(
            "number" to number,
            "description" to description,
            "date" to date?.let { Timestamp(it.epochSeconds, it.nanosecondsOfSecond) },
            "location" to location,
            "count" to count,
            "weight" to weight,
            "status" to status,
            "totalWeight" to totalWeight,
            "qrCode" to qrCode,
            "bigBag" to bigBag,
            "booked" to booked,
            "dateBooked" to dateBooked?.let { Timestamp(it.epochSeconds, it.nanosecondsOfSecond) },
            "bookedByUser" to bookedByUser,
            "bookedRemark" to bookedRemark,
            "remark" to remark,
            "certificateOk" to certificateOk,
            "createdAt" to createdAt?.let { Timestamp(it.epochSeconds, it.nanosecondsOfSecond) }
        )
    }

    // El resto de métodos permanecen igual usando toHistorialModelSafe()...
    override suspend fun getLoteHistorialByNumber(number: String): LoteModel? = withContext(Dispatchers.IO) {
        try {
            val snapshot = historialCollection.where("number", equalTo = number).get()
            snapshot.documents.firstOrNull()?.toHistorialModelSafe()
        } catch (e: Exception) { null }
    }

    override suspend fun getLoteHistorialById(id: String): LoteModel? = withContext(Dispatchers.IO) {
        try {
            val doc = historialCollection.document(id).get()
            if (doc.exists) doc.toHistorialModelSafe() else null
        } catch (e: Exception) { null }
    }

    override suspend fun eliminarLoteHistorial(loteId: String): Boolean = withContext(Dispatchers.IO) {
        try {
            historialCollection.document(loteId).delete()
            true
        } catch (e: Exception) { false }
    }
}