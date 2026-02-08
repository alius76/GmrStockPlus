package com.alius.gmrstockplus.data

actual fun getDevolucionRepository(plantId: String): DevolucionRepository {
    return DevolucionRepositoryImpl(plantId)
}

