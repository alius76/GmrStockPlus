package com.alius.gmrstockplus.domain.model

import kotlinx.datetime.Instant
import kotlinx.serialization.Serializable

@Serializable
data class Trasvase(
    val trasvaseId: String = "",
    val trasvaseNumber: String = "",          // Número de lote
    val trasvaseDescription: String = "",     // Descripción o material
    val trasvaseLocation: String = "",        // Ejemplo: "Vertisol"
    val trasvaseCount: String = "",           // Cantidad de BigBags
    val trasvaseTotalWeight: String = "",     // Peso total
    val trasvaseDate: Instant? = null,        // 🔹 Fecha del trasvase
    val trasvaseBigBag: List<TrasvaseBigBag> = emptyList()
)

@Serializable
data class TrasvaseBigBag(
    val bbTrasNumber: String = "",  // Número individual del BigBag
    val bbTrasWeight: String = ""   // Peso individual
)
