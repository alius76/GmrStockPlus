package com.alius.gmrstockplus.data

import com.alius.gmrstockplus.domain.model.Reprocesar

interface ReprocesarRepository {
    suspend fun listarReprocesos(): List<Reprocesar>
    suspend fun getReprocesoByNumber(reprocesoNumber: String): Reprocesar?
}

/**
 * Función multiplataforma unificada con plantId.
 */
expect fun getReprocesarRepository(plantId: String): ReprocesarRepository
