package com.alius.gmrstockplus.data

actual fun getDevolucionesRepository(plantId: String): DevolucionesRepository {
    return DevolucionesRepositoryImpl(plantId = plantId)
}