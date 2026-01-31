package com.alius.gmrstockplus


import androidx.compose.material3.MaterialTheme

import androidx.compose.runtime.*

import com.alius.gmrstockplus.presentation.screens.LoteValidationScreen
import org.jetbrains.compose.ui.tooling.preview.Preview

@Composable
@Preview
fun App() {
    MaterialTheme {
        // Llamamos a la pantalla de validación que creamos
        //ClientValidationScreen()
        LoteValidationScreen()
    }
}