package com.alius.gmrstockplus.data

import com.alius.gmrstockplus.data.firestore.FirebaseClient
import com.alius.gmrstockplus.domain.model.*
import dev.gitlive.firebase.firestore.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.withContext
import kotlinx.datetime.*

class LoteRepositoryImpl(private val plantName: String) : LoteRepository {

    private val firestore by lazy {
        if (plantName == "P08") FirebaseClient.db08 else FirebaseClient.db07
    }

    private val loteCollection by lazy {
        firestore.collection("lote")
    }

    // --- FUNCIÓN DE MAPEO MANUAL ---

    private fun DocumentSnapshot.toLoteModelSafe(): LoteModel? {
        return try {
            val lote = this.data<LoteModel>()

            // Extracción manual de Timestamps para garantizar compatibilidad con Kotlin Instant
            val firebaseDate = try { this.get<Timestamp>("date") } catch (e: Exception) { null }
            val firebaseCreatedAt = try { this.get<Timestamp>("createdAt") } catch (e: Exception) { null }
            val firebaseDateBooked = try { this.get<Timestamp>("dateBooked") } catch (e: Exception) { null }

            lote.copy(
                id = this.id,
                date = firebaseDate?.let { Instant.fromEpochSeconds(it.seconds, it.nanoseconds) } ?: lote.date,
                createdAt = firebaseCreatedAt?.let { Instant.fromEpochSeconds(it.seconds, it.nanoseconds) } ?: lote.createdAt,
                dateBooked = firebaseDateBooked?.let { Instant.fromEpochSeconds(it.seconds, it.nanoseconds) } ?: lote.dateBooked
            )
        } catch (e: Exception) {
            println("❌ [GmrStockPlus] Error mapeando lote ${this.id}: ${e.message}")
            null
        }
    }

    // --- CONSULTAS ---

    override suspend fun listarLotes(data: String): List<LoteModel> = withContext(Dispatchers.IO) {
        try {
            // Seguridad: Si no hay texto, devolvemos los últimos 50 para que la pantalla no esté vacía
            if (data.isBlank()) return@withContext listarUltimosLotes(50)

            val snapshot = loteCollection
                .where { "number" greaterThanOrEqualTo data }
                .where { "number" lessThanOrEqualTo data + "\uf8ff" }
                .get()
            snapshot.documents.mapNotNull { it.toLoteModelSafe() }
        } catch (e: Exception) {
            println("❌ Error listarLotes: ${e.message}")
            emptyList()
        }
    }

    override suspend fun getLoteById(id: String): LoteModel? = withContext(Dispatchers.IO) {
        try {
            val doc = loteCollection.document(id).get()
            if (doc.exists) doc.toLoteModelSafe() else null
        } catch (e: Exception) { null }
    }

    override suspend fun getLoteByNumber(number: String): LoteModel? = withContext(Dispatchers.IO) {
        try {
            val snapshot = loteCollection.where { "number" equalTo number }.get()
            snapshot.documents.firstOrNull()?.toLoteModelSafe()
        } catch (e: Exception) { null }
    }

    override suspend fun listarLotesPorFecha(fecha: LocalDate): List<LoteModel> = withContext(Dispatchers.IO) {
        try {
            val inicio = fecha.atStartOfDayIn(TimeZone.currentSystemDefault())
            val fin = inicio.plus(DateTimeUnit.DAY, TimeZone.currentSystemDefault())

            // VACUNA: Convertir a Timestamp nativo para que Firestore encuentre los datos
            val startTs = Timestamp(inicio.epochSeconds, inicio.nanosecondsOfSecond)
            val endTs = Timestamp(fin.epochSeconds, fin.nanosecondsOfSecond)

            val snapshot = loteCollection
                .where { "createdAt" greaterThanOrEqualTo startTs }
                .where { "createdAt" lessThan endTs }
                .get()

            println("✅ [LoteRepo] Lotes hallados para $fecha: ${snapshot.documents.size}")
            snapshot.documents.mapNotNull { it.toLoteModelSafe() }
        } catch (e: Exception) {
            println("❌ Error listarLotesPorFecha: ${e.message}")
            emptyList()
        }
    }

    override suspend fun listarUltimosLotes(cantidad: Int): List<LoteModel> = withContext(Dispatchers.IO) {
        try {
            val snapshot = loteCollection
                .orderBy("createdAt", Direction.DESCENDING)
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
            // En GitLive, para filtrar por campos que no son null se usa una consulta de rango o existencia
            val snapshot = loteCollection
                .where { "booked" notEqualTo null }
                .orderBy(orderBy, dir)
                .get()
            snapshot.documents.mapNotNull { it.toLoteModelSafe() }
        } catch (e: Exception) {
            println("❌ Error listarLotesReservados: ${e.message}")
            emptyList()
        }
    }

    override suspend fun listarLotesPorDescripcion(descripcion: String): List<LoteModel> = withContext(Dispatchers.IO) {
        try {
            if (descripcion.isBlank()) return@withContext listarUltimosLotes(20)

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
            // Aseguramos que la fecha de reserva se guarde como Timestamp de Firebase
            val bookedTs = dateBooked?.let { Timestamp(it.epochSeconds, it.nanosecondsOfSecond) }

            loteCollection.document(loteId).update(
                "booked" to cliente,
                "dateBooked" to bookedTs,
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
            println("🚀 [DEBUG VERTISOL] --- INICIO DE ESCANEO TOTAL CON FECHAS (ASC) ---")

            // 1. Obtenemos TODOS los lotes de la colección (Sin límites de cantidad o fecha)
            val snapshot = loteCollection.get()
            val allLotes = snapshot.documents.mapNotNull { it.toLoteModelSafe() }

            println("📦 [DEBUG VERTISOL] Lotes analizados desde Firebase: ${allLotes.size}")

            // 2. Repositorio de trasvases para obtener el historial de fechas
            val trasvaseRepository = com.alius.gmrstockplus.data.getTrasvaseRepository(plantName)

            val resultado = allLotes.mapNotNull { lote ->
                // Filtramos bultos que están físicamente en Vertisol y activos ("s")
                val filteredBigBags = lote.bigBag.filter { bb ->
                    bb.bbLocation.equals("Vertisol", ignoreCase = true) && bb.bbStatus == "s"
                }

                // Si el lote no tiene nada en Vertisol, lo saltamos
                if (filteredBigBags.isEmpty()) return@mapNotNull null

                // 3. Obtenemos todos los eventos de trasvase registrados para este lote
                val trasvasesDelLote = trasvaseRepository.getTrasvasesByLote(lote.number)

                // 4. Mapeamos cada bulto buscando su fecha de trasvase específica
                val vertisolBbList = filteredBigBags.map { bb ->
                    var fechaBulto: kotlinx.datetime.Instant? = null

                    // Buscamos en el historial de trasvases el bulto exacto (bbNumber)
                    for (t in trasvasesDelLote) {
                        if (t.trasvaseBigBag.any { it.bbTrasNumber == bb.bbNumber }) {
                            fechaBulto = t.trasvaseDate
                            break // Encontrado, pasamos al siguiente bulto
                        }
                    }

                    VertisolBigBag(
                        bbNumber = bb.bbNumber,
                        bbWeight = bb.bbWeight,
                        bbTrasvaseDate = fechaBulto
                    )
                }

                // 5. Cálculo de peso total formateado
                val totalWeight = filteredBigBags.sumOf { it.bbWeight.toDoubleOrNull() ?: 0.0 }
                val totalWeightString = if (totalWeight % 1.0 == 0.0) {
                    totalWeight.toInt().toString()
                } else {
                    totalWeight.toString()
                }

                // 6. Construcción del modelo de vista para la pantalla
                Vertisol(
                    vertisolNumber = lote.number,
                    vertisolDescription = lote.description,
                    vertisolLocation = "Vertisol",
                    vertisolCount = filteredBigBags.size.toString(),
                    vertisolTotalWeight = totalWeightString,
                    vertisolCompletado = true,
                    vertisolBigBag = vertisolBbList
                )
            }

            println("🎯 [DEBUG VERTISOL] Escaneo finalizado: ${resultado.size} lotes en Vertisol.")

            // 7. Devolvemos la lista ordenada de forma ascendente por número de lote
            return@withContext resultado.sortedBy { it.vertisolNumber }

        } catch (e: Exception) {
            println("🔥 [DEBUG VERTISOL] ERROR CRÍTICO: ${e.message}")
            e.printStackTrace()
            emptyList()
        }
    }
}