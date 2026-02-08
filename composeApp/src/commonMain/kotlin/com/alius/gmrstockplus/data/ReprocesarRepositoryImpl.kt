package com.alius.gmrstockplus.data

import com.alius.gmrstockplus.data.firestore.FirebaseClient
import com.alius.gmrstockplus.domain.model.Reprocesar
import dev.gitlive.firebase.firestore.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.withContext
import kotlinx.datetime.Instant

class ReprocesarRepositoryImpl(private val plantId: String) : ReprocesarRepository {

    private val firestore by lazy {
        if (plantId == "P08") FirebaseClient.db08 else FirebaseClient.db07
    }

    private val reprocesarCollection by lazy { firestore.collection("reprocesar") }

    /**
     * Mapea un DocumentSnapshot a Reprocesar inyectando el ID del documento
     * y reparando manualmente la fecha para evitar el error de 1970.
     */
    private fun DocumentSnapshot.toReprocesarSafe(): Reprocesar? {
        return try {
            // 1. Intentamos la conversión automática (aquí la fecha vendrá como 1970)
            val baseItem = this.data<Reprocesar>()

            // 2. 🛠️ RESCATE MANUAL DE LA FECHA (Timestamp a Instant)
            // Extraemos el valor directamente de Firebase saltándonos el Serializer de Kotlin
            val fechaCorregida = try {
                val ts = this.get<dev.gitlive.firebase.firestore.Timestamp>("reprocesarFechaReproceso")
                Instant.fromEpochMilliseconds(ts.seconds * 1000)
            } catch (e: Exception) {
                // Si no es un Timestamp nativo, intentamos leerlo como Long por si acaso
                try {
                    val ms = this.get<Long>("reprocesarFechaReproceso")
                    Instant.fromEpochMilliseconds(ms)
                } catch (e2: Exception) {
                    baseItem.reprocesarFechaReproceso // Si todo falla, dejamos el original
                }
            }

            // 3. Devolvemos el objeto con el ID de Firestore y la fecha reparada
            baseItem.copy(
                id = this.id,
                reprocesarFechaReproceso = fechaCorregida
            )
        } catch (e: Exception) {
            println("❌ [ReprocesarRepo] Error mapeando ${this.id}: ${e.message}")
            null
        }
    }

    override suspend fun listarReprocesos(): List<Reprocesar> = withContext(Dispatchers.IO) {
        try {
            val snapshot = reprocesarCollection.get()
            // Al usar mapNotNull, si un documento falla o está vacío, no rompe la lista
            val lista = snapshot.documents.mapNotNull { it.toReprocesarSafe() }

            println("📦 [Repo] Cargados ${lista.size} reprocesos de la planta $plantId")
            lista
        } catch (e: Exception) {
            println("❌ [Repo] Error en listarReprocesos: ${e.message}")
            emptyList()
        }
    }

    override suspend fun getReprocesoByNumber(reprocesoNumber: String): Reprocesar? = withContext(Dispatchers.IO) {
        try {
            // Usamos el nombre exacto de la variable en Firebase
            val snapshot = reprocesarCollection
                .where { "reprocesarLoteNumber" equalTo reprocesoNumber }
                .get()

            snapshot.documents.firstOrNull()?.toReprocesarSafe()
        } catch (e: Exception) {
            println("❌ [Repo] Error buscando lote $reprocesoNumber: ${e.message}")
            null
        }
    }
}