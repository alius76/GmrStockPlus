package com.alius.gmrstockplus.data

actual fun getReprocesarRepository(plantId: String): ReprocesarRepository {
    return ReprocesarRepositoryImpl(plantId = plantId)
}