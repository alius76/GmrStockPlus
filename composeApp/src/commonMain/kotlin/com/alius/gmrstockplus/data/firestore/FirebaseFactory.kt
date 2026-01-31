package com.alius.gmrstockplus.data.firestore

import com.alius.gmrstockplus.getPlatformContext
import dev.gitlive.firebase.Firebase
import dev.gitlive.firebase.FirebaseOptions
import dev.gitlive.firebase.app
import dev.gitlive.firebase.initialize

object FirebaseFactory {
    private var initialized = false

    fun initializeApps() {
        if (initialized) return
        val context = getPlatformContext()

        // 1. COMPORTAMIENTO PARA iOS
        if (context == null) {
            // En iOS no hacemos nada manual.
            // El AppDelegate ya cargó P07 (default) y P08 (manual).
            initialized = true
            return
        }

        // 2. COMPORTAMIENTO PARA ANDROID
        try {
            // La P07 (Default) se inicializa sola por el plugin de Google.

            // Inicializamos P08 manualmente para Android
            val options08 = FirebaseOptions(
                applicationId = "1:729925233716:ios:8ec1d19983a585b1e2ee61", // Tu ID de Android para P08
                apiKey = "AIzaSyD2Wox_s_r59MaL3RD1iMTHFlDj0GlSDDI",
                projectId = "cepexproduccion-2d770"
            )

            // Verificamos si la app P08 ya existe para evitar errores de duplicidad
            try {
                Firebase.app("P08")
            } catch (e: Exception) {
                // Si no existe, la inicializamos
                Firebase.initialize(context, options08, name = "P08")
                println("✅ Android: P08 inicializada manualmente")
            }

            initialized = true
        } catch (e: Exception) {
            println("⚠️ Error en Factory de Android: ${e.message}")
        }
    }
}