package com.alius.gmrstockplus.data

import com.alius.gmrstockplus.data.firestore.FirebaseClient
import com.alius.gmrstockplus.domain.model.Trasvase
import com.alius.gmrstockplus.domain.model.TrasvaseBigBag
import dev.gitlive.firebase.firestore.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.withContext
import kotlinx.datetime.Instant

class TrasvaseRepositoryImpl(private val plantId: String) : TrasvaseRepository {

    // Usamos el cliente nativo que ya tienes configurado
    private val firestore by lazy {
        if (plantId == "P08") FirebaseClient.db08 else FirebaseClient.db07
    }

    private val trasvaseCollection by lazy { firestore.collection("trasvase") }

    /**
     * Mapeo seguro para convertir DocumentSnapshot a Trasvase usando el SDK nativo.
     * Repara la fecha manualmente para evitar el error de 1970.
     */
    private fun DocumentSnapshot.toTrasvaseSafe(): Trasvase? {
        return try {
            val item = this.data<Trasvase>()

            // 🛡️ REPARACIÓN DE FECHA: Rescatamos el Timestamp nativo
            val fechaManual = try {
                val ts = this.get<dev.gitlive.firebase.firestore.Timestamp>("trasvaseDate")
                Instant.fromEpochMilliseconds(ts.seconds * 1000)
            } catch (e: Exception) {
                item.trasvaseDate
            }

            // Devolvemos el objeto con la fecha corregida e inyectamos el ID de Firestore
            item.copy(trasvaseDate = fechaManual)
        } catch (e: Exception) {
            println("❌ [TrasvaseRepo] Error mapeando documento ${this.id}: ${e.message}")
            null
        }
    }

    override suspend fun getTrasvaseByLote(trasvaseNumber: String): Trasvase? = withContext(Dispatchers.IO) {
        try {
            val snapshot = trasvaseCollection
                .where { "trasvaseNumber" equalTo trasvaseNumber }
                .get()

            snapshot.documents.firstOrNull()?.toTrasvaseSafe()
        } catch (e: Exception) {
            println("❌ [TrasvaseRepo] Error buscando lote $trasvaseNumber: ${e.message}")
            null
        }
    }

    override suspend fun getTrasvasesByLote(trasvaseNumber: String): List<Trasvase> = withContext(Dispatchers.IO) {
        try {
            val snapshot = trasvaseCollection
                .where { "trasvaseNumber" equalTo trasvaseNumber }
                .get()

            val resultados = snapshot.documents.mapNotNull { it.toTrasvaseSafe() }
            println("📅 getTrasvasesByLote($trasvaseNumber) -> ${resultados.size} trasvases encontrados")
            resultados
        } catch (e: Exception) {
            emptyList()
        }
    }

    override suspend fun getTrasvaseBigBagsByLote(trasvaseNumber: String): List<TrasvaseBigBag> {
        return getTrasvaseByLote(trasvaseNumber)?.trasvaseBigBag ?: emptyList()
    }
}