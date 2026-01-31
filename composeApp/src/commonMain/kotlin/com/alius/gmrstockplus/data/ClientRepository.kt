package com.alius.gmrstockplus.data

import com.alius.gmrstockplus.domain.model.Cliente

interface ClientRepository {
    suspend fun getAllClientsOrderedByName(): List<Cliente>
    suspend fun getAllClientsWithIds(): List<Pair<String, Cliente>>
    suspend fun addClient(cliente: Cliente): String
    suspend fun updateClient(documentId: String, cliente: Cliente)
    suspend fun deleteClient(documentId: String)
}



