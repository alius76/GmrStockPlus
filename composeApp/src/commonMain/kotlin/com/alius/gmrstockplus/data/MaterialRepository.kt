package com.alius.gmrstockplus.data

import com.alius.gmrstockplus.domain.model.Material

interface MaterialRepository {
    suspend fun getAllMaterialsOrderedByName(): List<Material>
}

/**
 * ✅ expect unificado con plantId
 * Eliminamos databaseUrl para mantener la coherencia con el resto de la app.
 */
expect fun getMaterialRepository(plantId: String): MaterialRepository
