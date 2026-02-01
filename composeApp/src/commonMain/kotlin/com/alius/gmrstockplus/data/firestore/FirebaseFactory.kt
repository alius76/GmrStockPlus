package com.alius.gmrstockplus.data.firestore

import com.alius.gmrstockplus.getPlatform
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
        val platform = getPlatform()

        // 1. COMPORTAMIENTO PARA iOS
        // Solo entramos aquí si es iOS Y el context es null
        if (platform.name.contains("iOS", ignoreCase = true)) {
            initialized = true
            return
        }

        // 2. COMPORTAMIENTO PARA DESKTOP
        // Si no es móvil, el main.kt de Desktop ya se encargó de todo.
        if (!platform.isMobile) {
            initialized = true
            return
        }

        // 3. COMPORTAMIENTO PARA ANDROID (Tu código original intacto)
        try {
            val options08 = FirebaseOptions(
                applicationId = "1:729925233716:ios:8ec1d19983a585b1e2ee61",
                apiKey = "AIzaSyD2Wox_s_r59MaL3RD1iMTHFlDj0GlSDDI",
                projectId = "cepexproduccion-2d770"
            )

            try {
                Firebase.app("P08")
            } catch (e: Exception) {
                // Si context es null aquí (en Android), esto lanzaría el error de cast.
                // Pero como ya filtramos iOS y Desktop arriba, aquí context será seguro.
                Firebase.initialize(context!!, options08, name = "P08")
                println("✅ Android: P08 inicializada manualmente")
            }

            initialized = true
        } catch (e: Exception) {
            println("⚠️ Error en Factory de Android: ${e.message}")
        }
    }
}