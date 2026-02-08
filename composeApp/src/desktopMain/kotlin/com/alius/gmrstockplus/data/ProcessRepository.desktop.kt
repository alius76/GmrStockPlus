package com.alius.gmrstockplus.data

actual fun getProcessRepository(plantId: String): ProcessRepository {
    // En Android, el SDK de Firebase se inicializa automáticamente vía Google Services
    return ProcessRepositoryImpl(plantId)
}

