package com.alius.gmrstockplus.data

import com.alius.gmrstockplus.domain.model.Cliente
import dev.gitlive.firebase.Firebase
import dev.gitlive.firebase.firestore.firestore
import dev.gitlive.firebase.app
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.withContext

class ClientRepositoryImpl(plantName: String) : ClientRepository {

    // Accedemos a la instancia de la planta seleccionada (P07 o P08)
    // inicializada previamente en FirebaseFactory
    private val firestore = Firebase.firestore(Firebase.app(plantName))
    private val clientCollection = firestore.collection("cliente")

    override suspend fun getAllClientsOrderedByName(): List<Cliente> = withContext(Dispatchers.IO) {
        try {
            // orderBy y get() reemplazan el structuredQuery de la API REST
            val snapshot = clientCollection.orderBy("cliNombre").get()
            // ✅ Especificamos <Cliente> para que el serializador sepa qué clase construir
            snapshot.documents.map { it.data<Cliente>() }
        } catch (e: Exception) {
            println("❌ [GmrStockPlus] Error Firestore (GetAll): ${e.message}")
            emptyList()
        }
    }

    override suspend fun getAllClientsWithIds(): List<Pair<String, Cliente>> = withContext(Dispatchers.IO) {
        try {
            val snapshot = clientCollection.orderBy("cliNombre").get()
            // ✅ it.id extrae el nombre del documento e it.data<Cliente>() el contenido
            snapshot.documents.map { it.id to it.data<Cliente>() }
        } catch (e: Exception) {
            println("❌ [GmrStockPlus] Error Firestore (WithIds): ${e.message}")
            emptyList()
        }
    }

    override suspend fun addClient(cliente: Cliente): String = withContext(Dispatchers.IO) {
        try {
            // .add() sube el objeto y genera el Document ID de Firebase automáticamente
            val docRef = clientCollection.add(cliente)
            docRef.id
        } catch (e: Exception) {
            println("❌ [GmrStockPlus] Error Firestore (Add): ${e.message}")
            throw e
        }
    }

    override suspend fun updateClient(documentId: String, cliente: Cliente): Unit = withContext(Dispatchers.IO) {
        try {
            // .set() sobrescribe el documento con el nuevo objeto Cliente
            clientCollection.document(documentId).set(cliente)
        } catch (e: Exception) {
            println("❌ [GmrStockPlus] Error Firestore (Update): ${e.message}")
            throw e
        }
    }

    override suspend fun deleteClient(documentId: String): Unit = withContext(Dispatchers.IO) {
        try {
            // Elimina el documento físico de la colección
            clientCollection.document(documentId).delete()
        } catch (e: Exception) {
            println("❌ [GmrStockPlus] Error Firestore (Delete): ${e.message}")
            throw e
        }
    }
}