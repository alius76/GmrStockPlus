package com.alius.gmrstockplus.core.utils

import platform.Foundation.NSNumberFormatter
import platform.Foundation.NSNumberFormatterDecimalStyle
import platform.Foundation.NSNumber
import platform.Foundation.numberWithDouble
import platform.Foundation.NSLocale
import platform.Foundation.localeWithLocaleIdentifier
import platform.Foundation.numberWithInt
import platform.Foundation.numberWithLong

// Implementación 'actual' para iOS
actual fun formatWeight(weight: Number): String {
    // 1. Crear el formateador
    val formatter = NSNumberFormatter().apply {
        // Establecer el estilo de número decimal
        numberStyle = NSNumberFormatterDecimalStyle

        // 2. Forzar el Locale español (es_ES) para obtener el punto como separador de miles.
        locale = NSLocale.localeWithLocaleIdentifier("es_ES")

        // 3. Asegurar que no haya decimales
        maximumFractionDigits = 0u
        minimumFractionDigits = 0u

        // Asegurar que se use el separador de agrupación (el punto)
        usesGroupingSeparator = true
    }

    // Convertir el número Kotlin al tipo NSNumber que Foundation espera
    val nsNumber = when (weight) {
        is Int -> NSNumber.numberWithInt(weight)
        is Long -> NSNumber.numberWithLong(weight)
        else -> NSNumber.numberWithDouble(weight.toDouble())
    }

    // Formatear y devolver. Si falla, devolvemos el String simple.
    return formatter.stringFromNumber(nsNumber) ?: weight.toString()
}