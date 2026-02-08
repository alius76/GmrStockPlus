package com.alius.gmrstockplus.data

import com.alius.gmrstockplus.domain.model.LoteModel

interface HistorialRepository {
    suspend fun listarLotesHistorialDeHoy(): List<LoteModel>
    suspend fun getLoteHistorialByNumber(number: String): LoteModel?
    suspend fun getLoteHistorialById(id: String): LoteModel?
    suspend fun agregarLote(lote: LoteModel): Boolean
    suspend fun agregarYLigaroLote(lote: LoteModel): String?
    suspend fun eliminarLoteHistorial(loteId: String): Boolean
}

// 🔑 Cambiado databaseUrl por plantId para ser consistente con el SDK nativo
expect fun getHistorialRepository(plantId: String): HistorialRepository