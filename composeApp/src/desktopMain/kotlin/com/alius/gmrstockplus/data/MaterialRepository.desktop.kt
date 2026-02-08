package com.alius.gmrstockplus.data

actual fun getMaterialRepository(plantId: String): MaterialRepository {
    return MaterialRepositoryImpl(plantId)
}