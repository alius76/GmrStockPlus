package com.alius.gmrstockplus.data

import com.alius.gmrstockplus.domain.model.Process
import dev.gitlive.firebase.Firebase
import dev.gitlive.firebase.firestore.Direction
import dev.gitlive.firebase.firestore.firestore

class ProcessRepositoryImpl(
    private val plantId: String
) : ProcessRepository {

    private val db = Firebase.firestore

    private val collection = db.collection("wip")

    override suspend fun listarProcesos(): List<Process> {
        return try {
            println("🌐 [SDK] Consultando colección 'wip' para: $plantId")

            collection
                .orderBy("date", Direction.DESCENDING)
                .limit(10) // Mantenemos el límite de los últimos 10 procesos
                .get()
                .documents
                .map { document ->
                    // El SDK mapea automáticamente los campos del documento a tu clase Process
                    document.data<Process>()
                }
                .also {
                    println("✅ [SDK] Procesos WIP recuperados: ${it.size}")
                }
        } catch (e: Exception) {
            println("❌ [SDK] Error al listar procesos WIP: ${e.message}")
            emptyList()
        }
    }
}