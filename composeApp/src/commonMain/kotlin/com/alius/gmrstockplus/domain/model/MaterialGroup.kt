package com.alius.gmrstockplus.domain.model

import kotlinx.serialization.Serializable

@Serializable
data class MaterialGroup(
    val description: String,       // Descripción del material
    val totalWeight: String,       // Peso total de los lotes con esa descripción
    val totalLotes: Int,           // Cantidad de lotes con esa descripción
    val totalBigBags: Int,         // Suma del campo count de todos los lotes (bigbags)
    val loteNumbers: List<String>  // Lista con los números de lote
)
