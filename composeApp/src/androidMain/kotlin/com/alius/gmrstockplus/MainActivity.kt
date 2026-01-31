package com.alius.gmrstockplus

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.alius.gmrstockplus.core.AppContextProvider
import com.alius.gmrstockplus.data.firestore.FirebaseFactory

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)

        // 1. Inicializar el Provider de Contexto
        AppContextProvider.init(this.application)

        // 2. Inicializar Firebase (La Factory ahora obtiene el contexto sola)
        // Quitamos el parámetro que sobraba
        FirebaseFactory.initializeApps()

        setContent {
            App()
        }
    }
}