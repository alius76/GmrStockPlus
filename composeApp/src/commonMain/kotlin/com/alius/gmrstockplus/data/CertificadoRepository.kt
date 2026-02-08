package com.alius.gmrstockplus.data

import com.alius.gmrstockplus.domain.model.Certificado

interface CertificadoRepository {
    suspend fun getCertificadoByLoteNumber(loteNumber: String): Certificado?
}

// ✅ expect unificado para GmrStockPlus usando el ID de la planta
expect fun getCertificadoRepository(plantId: String): CertificadoRepository
