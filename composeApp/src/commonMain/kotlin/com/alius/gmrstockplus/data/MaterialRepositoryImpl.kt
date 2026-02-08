package com.alius.gmrstockplus.data

import com.alius.gmrstockplus.data.firestore.FirebaseClient
import com.alius.gmrstockplus.domain.model.Material
import dev.gitlive.firebase.firestore.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.withContext
import io.github.aakira.napier.Napier

class MaterialRepositoryImpl(private val plantId: String) : MaterialRepository {

    private val firestore by lazy {
        if (plantId == "P08") FirebaseClient.db08 else FirebaseClient.db07
    }

    private val materialCollection by lazy { firestore.collection("material") }

    override suspend fun getAllMaterialsOrderedByName(): List<Material> = withContext(Dispatchers.IO) {
        try {
            val snapshot = materialCollection
                .orderBy("materialNombre", Direction.ASCENDING)
                .get()

            snapshot.documents.map { doc ->
                // El SDK mapea recursivamente: Material -> List<Parametro> -> Rango
                val material = doc.data<Material>()
                material.copy(id = doc.id)
            }
        } catch (e: Exception) {
            Napier.e("❌ Error en getAllMaterialsOrderedByName (${plantId}): ${e.message}", e)
            emptyList()
        }
    }
}