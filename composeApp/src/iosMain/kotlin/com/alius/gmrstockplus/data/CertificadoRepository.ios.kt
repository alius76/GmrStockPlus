package com.alius.gmrstockplus.data

actual fun getCertificadoRepository(plantId: String): CertificadoRepository {
    return CertificadoRepositoryImpl(plantId)
}

