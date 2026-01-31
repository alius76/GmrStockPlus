package com.alius.gmrstockplus.data

import com.alius.gmrstockplus.domain.model.*
import dev.gitlive.firebase.Firebase
import dev.gitlive.firebase.firestore.*
import dev.gitlive.firebase.app
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.withContext
import kotlinx.datetime.Instant
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.datetime.atStartOfDayIn
import kotlinx.datetime.DateTimeUnit
import kotlinx.datetime.plus
import kotlinx.datetime.Clock
import kotlinx.datetime.toLocalDateTime

class LoteRepositoryImpl(plantName: String) : LoteRepository {

    private val firestore = Firebase.firestore(Firebase.app(plantName))
    private val loteCollection = firestore.collection("lote")

    // --- FUNCIÓN DE MAPEO MANUAL (Idéntica a la solución de Filtros) ---

    private fun DocumentSnapshot.toLoteModelSafe(): LoteModel? {
        return try {
            // 1. Mapeo básico de los datos
            val lote = this.data<LoteModel>()

            // 2. Extracción manual de los Timestamps de Firebase
            val firebaseDate = try { this.get<Timestamp>("date") } catch (e: Exception) { null }
            val firebaseCreatedAt = try { this.get<Timestamp>("createdAt") } catch (e: Exception) { null }
            val firebaseDateBooked = try { this.get<Timestamp>("dateBooked") } catch (e: Exception) { null }

            // 3. Inyectamos los Instants corregidos en el modelo mediante copy()
            lote.copy(
                id = this.id,
                date = firebaseDate?.let { Instant.fromEpochSeconds(it.seconds, it.nanoseconds) } ?: lote.date,
                createdAt = firebaseCreatedAt?.let { Instant.fromEpochSeconds(it.seconds, it.nanoseconds) } ?: lote.createdAt,
                dateBooked = firebaseDateBooked?.let { Instant.fromEpochSeconds(it.seconds, it.nanoseconds) } ?: lote.dateBooked
            )
        } catch (e: Exception) {
            println("❌ [GmrStockPlus] Error crítico mapeando lote ${this.id}: ${e.message}")
            null
        }
    }

    // --- CONSULTAS ---

    override suspend fun listarLotes(data: String): List<LoteModel> = withContext(Dispatchers.IO) {
        try {
            val snapshot = loteCollection
                .where { "number" greaterThanOrEqualTo data }
                .where { "number" lessThanOrEqualTo data + "\uf8ff" }
                .get()
            snapshot.documents.mapNotNull { it.toLoteModelSafe() }
        } catch (e: Exception) {
            emptyList()
        }
    }

    override suspend fun getLoteById(id: String): LoteModel? = withContext(Dispatchers.IO) {
        try {
            val doc = loteCollection.document(id).get()
            if (doc.exists) doc.toLoteModelSafe() else null
        } catch (e: Exception) {
            null
        }
    }

    override suspend fun getLoteByNumber(number: String): LoteModel? = withContext(Dispatchers.IO) {
        try {
            val snapshot = loteCollection.where("number", equalTo = number).get()
            snapshot.documents.firstOrNull()?.toLoteModelSafe()
        } catch (e: Exception) {
            null
        }
    }

    override suspend fun listarLotesPorFecha(fecha: LocalDate): List<LoteModel> = withContext(Dispatchers.IO) {
        try {
            val inicio = fecha.atStartOfDayIn(TimeZone.currentSystemDefault())
            val fin = inicio.plus(1, DateTimeUnit.DAY, TimeZone.currentSystemDefault())

            val snapshot = loteCollection
                .where { "createdAt" greaterThanOrEqualTo inicio }
                .where { "createdAt" lessThan fin }
                .get()
            snapshot.documents.mapNotNull { it.toLoteModelSafe() }
        } catch (e: Exception) {
            emptyList()
        }
    }

    override suspend fun listarUltimosLotes(cantidad: Int): List<LoteModel> = withContext(Dispatchers.IO) {
        try {
            val snapshot = loteCollection
                .orderBy("createdAt", direction = Direction.DESCENDING)
                .limit(cantidad.toLong())
                .get()
            snapshot.documents.mapNotNull { it.toLoteModelSafe() }
        } catch (e: Exception) {
            emptyList()
        }
    }

    override suspend fun listarLotesReservados(orderBy: String, direction: String): List<LoteModel> = withContext(Dispatchers.IO) {
        try {
            val dir = if (direction == "ASCENDING") Direction.ASCENDING else Direction.DESCENDING
            val snapshot = loteCollection
                .where { "booked" notEqualTo null }
                .orderBy(orderBy, dir)
                .get()
            snapshot.documents.mapNotNull { it.toLoteModelSafe() }
        } catch (e: Exception) {
            emptyList()
        }
    }

    override suspend fun listarLotesPorDescripcion(descripcion: String): List<LoteModel> = withContext(Dispatchers.IO) {
        try {
            val snapshot = loteCollection
                .where { "description" greaterThanOrEqualTo descripcion }
                .where { "description" lessThanOrEqualTo descripcion + "\uf8ff" }
                .get()
            snapshot.documents.mapNotNull { it.toLoteModelSafe() }
        } catch (e: Exception) {
            emptyList()
        }
    }

    // --- OPERACIONES DE ACTUALIZACIÓN ---

    override suspend fun listarLotesCreadosHoy(): List<LoteModel> {
        val hoy = Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault()).date
        return listarLotesPorFecha(hoy)
    }

    override suspend fun updateLoteRemark(loteId: String, newRemark: String): Boolean = withContext(Dispatchers.IO) {
        try {
            loteCollection.document(loteId).update("remark" to newRemark)
            true
        } catch (e: Exception) { false }
    }

    override suspend fun updateLoteBooked(
        loteId: String,
        cliente: Cliente?,
        dateBooked: Instant?,
        bookedByUser: String?,
        bookedRemark: String?
    ): Boolean = withContext(Dispatchers.IO) {
        try {
            loteCollection.document(loteId).update(
                "booked" to cliente,
                "dateBooked" to dateBooked,
                "bookedByUser" to bookedByUser,
                "bookedRemark" to bookedRemark
            )
            true
        } catch (e: Exception) { false }
    }

    // --- LÓGICA VERTISOL ---

    override suspend fun listarGruposPorDescripcion(filter: String): List<MaterialGroup> {
        return emptyList()
    }

    override suspend fun listarLotesVertisol(): List<Vertisol> = withContext(Dispatchers.IO) {
        try {
            val allLotes = listarLotes("")
            allLotes.mapNotNull { lote ->
                val filteredBigBags = lote.bigBag.filter {
                    it.bbLocation.equals("Vertisol", ignoreCase = true) && it.bbStatus == "s"
                }
                if (filteredBigBags.isEmpty()) return@mapNotNull null

                val vertisolBbList = filteredBigBags.map { bb ->
                    VertisolBigBag(bbNumber = bb.bbNumber, bbWeight = bb.bbWeight, bbTrasvaseDate = null)
                }

                Vertisol(
                    vertisolNumber = lote.number,
                    vertisolDescription = lote.description,
                    vertisolLocation = "Vertisol",
                    vertisolCount = lote.count,
                    vertisolTotalWeight = filteredBigBags.sumOf { it.bbWeight.toDoubleOrNull() ?: 0.0 }.toString(),
                    vertisolCompletado = true,
                    vertisolBigBag = vertisolBbList
                )
            }
        } catch (e: Exception) { emptyList() }
    }
}