package com.alius.gmrstockplus.data.firestore

import dev.gitlive.firebase.Firebase
import dev.gitlive.firebase.firestore.FirebaseFirestore
import dev.gitlive.firebase.firestore.firestore
import dev.gitlive.firebase.app

object FirebaseClient {

    val db07 by lazy {
        Firebase.firestore
    }

    val db08 by lazy {
        try {
            Firebase.firestore(Firebase.app("P08"))
        } catch (e: Exception) {
            println("⚠️ Error accediendo a P08: ${e.message}")
            Firebase.firestore
        }
    }

    /**
     * 🔑 ESTA ES LA FUNCIÓN QUE TE FALTA
     * Mapea el ID de la planta con la instancia de base de datos correcta.
     */
    fun getDb(plantId: String): FirebaseFirestore {
        return when (plantId) {
            "P07" -> db07
            "P08" -> db08
            else -> db07 // Fallback seguro
        }
    }
}