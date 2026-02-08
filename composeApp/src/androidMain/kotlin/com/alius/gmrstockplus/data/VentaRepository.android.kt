package com.alius.gmrstockplus.data

actual fun getVentaRepository(plantId: String): VentaRepository {
    return VentaRepositoryImpl(plantId)
}

