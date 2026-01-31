package com.alius.gmrstockplus.data.firestore

import com.alius.gmrstockplus.getPlatformContext // 👈 Importamos nuestra función mágica
import dev.gitlive.firebase.Firebase
import dev.gitlive.firebase.FirebaseOptions
import dev.gitlive.firebase.initialize

object FirebaseFactory {

    fun initializeApps() {
        val context = getPlatformContext()

        val options07 = FirebaseOptions(
            applicationId = "1:983598044129:android:5bee193d989afcfdca38a0",
            apiKey = "AIzaSyAi8EU5z7TdQXczA_y5R5kH_1abxIoohLo",
            projectId = "firestoreavanzadocompose-62d26"
        )

        val options08 = FirebaseOptions(
            applicationId = "1:729925233716:web:4fe4b6484ac7c2bfe2ee61",
            apiKey = "AIzaSyDuwTitw5QIsWZWmRQSaSIhuAYMggjT8Gw",
            projectId = "cepexproduccion-2d770"
        )

        try {
            // Inicializamos ambas plantas con sus nombres clave
            Firebase.initialize(context, options07, name = "P07")
            Firebase.initialize(context, options08, name = "P08")

            println("🔥 [GmrStockPlus] Firebase inicializado correctamente en ${if(context != null) "Android" else "Desktop/iOS"}")
        } catch (e: Exception) {
            println("⚠️ Error inicializando Firebase: ${e.message}")
        }
    }
}