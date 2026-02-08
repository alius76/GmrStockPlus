package com.alius.gmrstockplus

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
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
    // 🛡️ SOLUCIÓN PARA EL ERROR DE LA TERCERA VEZ
    // Sobrescribimos el guardado de estado para que sea nulo.
    // Esto evita que Voyager intente serializar lambdas u objetos complejos
    // en el Bundle de Android, que es lo que causa el BadParcelableException.
    override fun onSaveInstanceState(outState: Bundle) {
        // No llamamos a super.onSaveInstanceState(outState)
        // O simplemente pasamos un bundle vacío para "limpiar" el rastro.
        val emptyBundle = Bundle()
        super.onSaveInstanceState(emptyBundle)
    }
}

@Preview
@Composable
fun AppAndroidPreview() {
    App()
}