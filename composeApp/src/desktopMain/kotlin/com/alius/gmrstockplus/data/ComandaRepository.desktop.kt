package com.alius.gmrstockplus.data

actual fun getComandaRepository(databaseUrl: String): ComandaRepository {
    return ComandaRepositoryImpl(databaseUrl)
}