package com.alius.gmrstockplus.data

actual fun getLoteRepository(plantName: String): LoteRepository {
    return LoteRepositoryImpl(plantName)
}