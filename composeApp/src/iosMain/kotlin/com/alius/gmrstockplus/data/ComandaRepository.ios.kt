package com.alius.gmrstockplus.data


actual fun getComandaRepository(plantName: String): ComandaRepository {
    return ComandaRepositoryImpl(plantName)
}


