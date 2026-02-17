package com.alius.gmrstockplus.data

import com.alius.gmrstockplus.data.firestore.FirebaseClient
import com.alius.gmrstockplus.domain.model.Process
import dev.gitlive.firebase.firestore.Direction
import dev.gitlive.firebase.firestore.DocumentSnapshot
import dev.gitlive.firebase.firestore.Timestamp
import io.github.aakira.napier.Napier
import kotlinx.datetime.Instant

class ProcessRepositoryImpl(
    private val plantId: String
) : ProcessRepository {

    // 1. REUTILIZAMOS LA INSTANCIA (Soluciona el FIRIllegalStateException)
    private val firestore by lazy {
        if (plantId == "P08") FirebaseClient.db08 else FirebaseClient.db07
    }

    private val collection by lazy { firestore.collection("wip") }

    override suspend fun listarProcesos(): List<Process> {
        return try {
            Napier.d("🌐 [ProcessRepo] Consultando colección 'wip' para: $plantId")

            collection
                .orderBy("date", Direction.DESCENDING)
                .limit(10)
                .get()
                .documents
                .mapNotNull { document ->
                    // 2. MAPEO SEGURO (Evita fallos de fecha en iOS)
                    document.toProcessSafe()
                }
                .also {
                    Napier.d("✅ [ProcessRepo] Procesos WIP recuperados: ${it.size}")
                }
        } catch (e: Exception) {
            Napier.e("❌ [ProcessRepo] Error al listar: ${e.message}")
            emptyList()
        }
    }

    /**
     * Función para mapear Process de forma segura en iOS.
     * Salta el serializador de Instant automático que causa problemas con Timestamps nativos.
     */
    private fun DocumentSnapshot.toProcessSafe(): Process? {
        return try {
            // Mapeamos los campos básicos (id, number, description)
            val process = this.data<Process>()

            // Extraemos la fecha manualmente
            val firebaseDate = try { this.get<Timestamp>("date") } catch (e: Exception) { null }

            process.copy(
                id = this.id,
                date = firebaseDate?.let { Instant.fromEpochSeconds(it.seconds, it.nanoseconds) } ?: process.date
            )
        } catch (e: Exception) {
            Napier.e("❌ Error mapeando proceso ${this.id}: ${e.message}")
            null
        }
    }
}