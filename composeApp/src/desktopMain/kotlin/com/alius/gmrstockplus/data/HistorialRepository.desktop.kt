package com.alius.gmrstockplus.data

actual fun getHistorialRepository(plantId: String): HistorialRepository {
    return HistorialRepositoryImpl(plantId)
}

