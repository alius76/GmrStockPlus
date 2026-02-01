package com.alius.gmrstockplus.data

actual fun getRatioRepository(plantId: String): RatioRepository {
    return RatioRepositoryImpl(plantId)
}