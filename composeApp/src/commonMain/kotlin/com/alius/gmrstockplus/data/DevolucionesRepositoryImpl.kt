package com.alius.gmrstockplus.data

import com.alius.gmrstockplus.data.firestore.FirebaseClient
import com.alius.gmrstockplus.domain.model.LoteModel
import dev.gitlive.firebase.firestore.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.withContext
import kotlinx.datetime.Instant

class DevolucionesRepositoryImpl(private val plantId: String) : DevolucionesRepository {

    private val firestore by lazy {
        if (plantId == "P08") FirebaseClient.db08 else FirebaseClient.db07
    }

    private val loteCollection by lazy { firestore.collection("lote") }

    // --- Mapeo Manual para persistencia de IDs y Fechas ---
    private fun DocumentSnapshot.toLoteModelSafe(): LoteModel? {
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
            println("❌ [DevolucionesRepo] Error mapeando lote ${this.id}")
            null
        }
    }

    override suspend fun getLoteByNumber(number: String): LoteModel? = withContext(Dispatchers.IO) {
        try {
            val snapshot = loteCollection.where { "number" equalTo number }.get()
            snapshot.documents.firstOrNull()?.toLoteModelSafe()
        } catch (e: Exception) {
            println("❌ Error en getLoteByNumber: ${e.message}")
            null
        }
    }

    override suspend fun devolverBigBag(loteNumber: String, bigBagNumber: String): Boolean {
        return devolverBigBags(loteNumber, listOf(bigBagNumber))
    }

    override suspend fun devolverBigBags(
        loteNumber: String,
        bigBagNumbers: List<String>
    ): Boolean = withContext(Dispatchers.IO) {
        if (bigBagNumbers.isEmpty()) return@withContext true

        try {
            // 1. Obtenemos el lote actual
            val snapshot = loteCollection.where { "number" equalTo loteNumber }.get()
            val doc = snapshot.documents.firstOrNull() ?: return@withContext false
            val loteActual = doc.toLoteModelSafe() ?: return@withContext false

            // 2. Calculamos los cambios (Stock y Peso)
            val returnedBigBags = loteActual.bigBag.filter { bigBagNumbers.contains(it.bbNumber) }
            val weightToReturn = returnedBigBags.sumOf { it.bbWeight.toDoubleOrNull() ?: 0.0 }

            // 3. Actualizamos la lista de BigBags localmente
            val updatedBigBags = loteActual.bigBag.map { bb ->
                if (bigBagNumbers.contains(bb.bbNumber)) {
                    // Cambiamos estado a 's' (stock) y añadimos la marca de devolución
                    bb.copy(bbStatus = "s", bbRemark = "DEVO")
                } else {
                    bb
                }
            }

            // 4. Calculamos nuevos totales
            val newCount = (loteActual.count.toIntOrNull() ?: 0) + bigBagNumbers.size
            val newTotalWeight = (loteActual.totalWeight.toDoubleOrNull() ?: 0.0) + weightToReturn

            // 5. Aplicamos el Update mediante el SDK (Equivalente al PATCH optimizado)
            loteCollection.document(doc.id).update(
                "bigBag" to updatedBigBags,
                "count" to newCount.toString(),
                "totalWeight" to newTotalWeight.toString()
            )

            println("✅ [Devoluciones] Lote ${loteActual.number} actualizado: $newCount BBs en stock.")
            true
        } catch (e: Exception) {
            println("❌ Error en devolverBigBags: ${e.message}")
            false
        }
    }
}