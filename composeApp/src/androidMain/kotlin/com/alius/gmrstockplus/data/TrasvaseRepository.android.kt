package com.alius.gmrstockplus.data

actual fun getTrasvaseRepository(plantId: String): TrasvaseRepository {
    return TrasvaseRepositoryImpl(plantId = plantId)
}