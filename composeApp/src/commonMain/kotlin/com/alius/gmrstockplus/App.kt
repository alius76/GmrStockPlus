package com.alius.gmrstockplus


import androidx.compose.material3.MaterialTheme

import androidx.compose.runtime.*
import com.alius.gmrstockplus.presentation.screens.ClientValidationScreen

import com.alius.gmrstockplus.presentation.screens.LoteValidationScreen
import org.jetbrains.compose.ui.tooling.preview.Preview

@Composable
@Preview
fun App() {
    // Estado para saber qué pantalla mostrar
    var showLotes by remember { mutableStateOf(false) }

    MaterialTheme {
        if (showLotes) {
            // Aquí llamarías a LoteValidationScreen()
            // Podrías pasarle un callback similar para volver
            LoteValidationScreen()
        } else {
            // Pasamos la lógica para cambiar el estado al botón
            ClientValidationScreen(onNavigateToLotes = { showLotes = true })
        }
    }
}