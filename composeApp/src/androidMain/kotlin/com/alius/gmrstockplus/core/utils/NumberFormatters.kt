package com.alius.gmrstockplus.core.utils

import java.text.NumberFormat
import java.util.Locale

// Implementación 'actual' para Android/JVM
actual fun formatWeight(weight: Number): String {
    // Usamos el Locale español (es, ES) para asegurar el punto (.) como separador de miles
    val format = NumberFormat.getNumberInstance(Locale("es", "ES"))

    // Configuramos para no tener decimales
    format.maximumFractionDigits = 0
    format.minimumFractionDigits = 0

    return format.format(weight)
}