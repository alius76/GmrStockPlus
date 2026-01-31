package com.alius.gmrstockplus.data.firestore

import dev.gitlive.firebase.Firebase
import dev.gitlive.firebase.firestore.firestore
import dev.gitlive.firebase.app

object FirebaseClient {

    /**
     * P07: Base de datos principal.
     * En Android e iOS, al no pasar parámetros, Firebase usa la configuración
     * por defecto (google-services.json / GoogleService-Info.plist).
     */
    val db07 by lazy {
        Firebase.firestore
    }

    /**
     * P08: Base de datos secundaria.
     * Intentamos buscar la app "P08" que inicializamos en el AppDelegate (iOS)
     * o en la FirebaseFactory (Android).
     */
    val db08 by lazy {
        try {
            // Buscamos la app por el nombre exacto que le dimos en la capa nativa
            Firebase.firestore(Firebase.app("P08"))
        } catch (e: Exception) {
            println("⚠️ Error accediendo a P08, usando db07 como fallback: ${e.message}")
            // Si falla (ej. si el .plist no se cargó), devolvemos la default para no crashear
            Firebase.firestore
        }
    }
}