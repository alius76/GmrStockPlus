package com.alius.gmrstockplus.data

import com.alius.gmrstockplus.data.firestore.FirebaseClient
import com.alius.gmrstockplus.domain.model.Cliente
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.withContext

class ClientRepositoryImpl(private val plantName: String) : ClientRepository {

    // 1. Usamos 'by lazy' para que no intente buscar la App hasta que realmente
    // necesitemos los datos. Esto evita crashes al abrir la pantalla.
    private val firestore by lazy {
        if (plantName == "P08") FirebaseClient.db08 else FirebaseClient.db07
    }

    // 2. La colección también debe ser lazy
    private val clientCollection by lazy {
        firestore.collection("cliente")
    }

    override suspend fun getAllClientsOrderedByName(): List<Cliente> = withContext(Dispatchers.IO) {
        try {
            // Aquí es donde se activa el 'lazy' por primera vez
            val snapshot = clientCollection.orderBy("cliNombre").get()
            snapshot.documents.map { it.data<Cliente>() }
        } catch (e: Exception) {
            println("❌ [GmrStockPlus] Error Firestore (GetAll en $plantName): ${e.message}")
            emptyList()
        }
    }

    override suspend fun getAllClientsWithIds(): List<Pair<String, Cliente>> = withContext(Dispatchers.IO) {
        try {
            val snapshot = clientCollection.orderBy("cliNombre").get()
            snapshot.documents.map { it.id to it.data<Cliente>() }
        } catch (e: Exception) {
            println("❌ [GmrStockPlus] Error (WithIds en $plantName): ${e.message}")
            emptyList()
        }
    }

    override suspend fun addClient(cliente: Cliente): String = withContext(Dispatchers.IO) {
        try {
            val docRef = clientCollection.add(cliente)
            docRef.id
        } catch (e: Exception) {
            println("❌ [GmrStockPlus] Error (Add en $plantName): ${e.message}")
            throw e
        }
    }

    override suspend fun updateClient(documentId: String, cliente: Cliente): Unit = withContext(Dispatchers.IO) {
        try {
            clientCollection.document(documentId).set(cliente)
        } catch (e: Exception) {
            println("❌ [GmrStockPlus] Error (Update en $plantName): ${e.message}")
            throw e
        }
    }

    override suspend fun deleteClient(documentId: String): Unit = withContext(Dispatchers.IO) {
        try {
            clientCollection.document(documentId).delete()
        } catch (e: Exception) {
            println("❌ [GmrStockPlus] Error (Delete en $plantName): ${e.message}")
            throw e
        }
    }
}