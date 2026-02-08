package com.alius.gmrstockplus.data

import com.alius.gmrstockplus.data.firestore.FirebaseClient
import com.alius.gmrstockplus.domain.model.*
import dev.gitlive.firebase.Firebase
import dev.gitlive.firebase.firestore.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.withContext
import kotlinx.datetime.*

class HistorialRepositoryImpl(private val plantName: String) : HistorialRepository {

    // 1. Selección de base de datos según la planta
    private val firestore by lazy {
        if (plantName == "P08") FirebaseClient.db08 else FirebaseClient.db07
    }

    private val historialCollection by lazy {
        firestore.collection("historial")
    }

    // --- FUNCIÓN DE MAPEO SEGURO (Igual que en Lotes) ---
    private fun DocumentSnapshot.toHistorialModelSafe(): LoteModel? {
        return try {
            val lote = this.data<LoteModel>()

            // Extraemos los timestamps de Firestore (que el SDK no mapea directo a Instant)
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

    // --- CONSULTAS ---

    override suspend fun listarLotesHistorialDeHoy(): List<LoteModel> = withContext(Dispatchers.IO) {
        try {
            // Calculamos el inicio y fin del día actual
            val hoy = Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault()).date
            val inicio = hoy.atStartOfDayIn(TimeZone.currentSystemDefault())
            val fin = inicio.plus(1, DateTimeUnit.DAY, TimeZone.currentSystemDefault())

            // Consulta nativa: Lotes movidos al historial hoy
            val snapshot = historialCollection
                .where { "createdAt" greaterThanOrEqualTo inicio }
                .where { "createdAt" lessThan fin }
                .get()

            snapshot.documents.mapNotNull { it.toHistorialModelSafe() }
        } catch (e: Exception) {
            println("❌ Error listarLotesHistorialDeHoy: ${e.message}")
            emptyList()
        }
    }

    override suspend fun getLoteHistorialByNumber(number: String): LoteModel? = withContext(Dispatchers.IO) {
        try {
            val snapshot = historialCollection.where("number", equalTo = number).get()
            snapshot.documents.firstOrNull()?.toHistorialModelSafe()
        } catch (e: Exception) {
            null
        }
    }

    override suspend fun getLoteHistorialById(id: String): LoteModel? = withContext(Dispatchers.IO) {
        try {
            val doc = historialCollection.document(id).get()
            if (doc.exists) doc.toHistorialModelSafe() else null
        } catch (e: Exception) {
            null
        }
    }

    // --- OPERACIONES (REEMPLAZO DE POST/PATCH) ---

    override suspend fun agregarLote(lote: LoteModel): Boolean = withContext(Dispatchers.IO) {
        try {
            // En el SDK, 'add' hace el POST automático y genera el ID
            historialCollection.add(lote)
            true
        } catch (e: Exception) {
            println("❌ Error al agregar a historial: ${e.message}")
            false
        }
    }

    override suspend fun agregarYLigaroLote(lote: LoteModel): String? = withContext(Dispatchers.IO) {
        try {
            // El SDK nos devuelve la referencia del documento creado
            val docRef = historialCollection.add(lote)
            val newId = docRef.id

            // Actualizamos el campo interno 'id' para que coincida con el de Firebase (el antiguo PATCH)
            docRef.update("id" to newId)

            newId
        } catch (e: Exception) {
            println("❌ Error en agregarYLigaroLote: ${e.message}")
            null
        }
    }

    override suspend fun eliminarLoteHistorial(loteId: String): Boolean = withContext(Dispatchers.IO) {
        try {
            historialCollection.document(loteId).delete()
            true
        } catch (e: Exception) {
            false
        }
    }
}