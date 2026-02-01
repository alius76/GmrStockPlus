package com.alius.gmrstockplus.data


actual fun getClientRepository(plantName: String): ClientRepository {
    return ClientRepositoryImpl(plantName)
}

