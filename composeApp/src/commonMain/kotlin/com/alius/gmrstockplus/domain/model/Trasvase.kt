package com.alius.gmrstockplus.domain.model

import com.alius.gmrstockplus.core.utils.FirebaseInstantSerializer
import kotlinx.datetime.Instant
import kotlinx.serialization.Serializable

@Serializable
data class Trasvase(
    // El ID ya está en el cuerpo como trasvaseId, así que lo mapeamos directamente
    val trasvaseId: String = "",
    val trasvaseNumber: String = "",
    val trasvaseDescription: String = "",
    val trasvaseLocation: String = "",
    val trasvaseCount: String = "",
    val trasvaseTotalWeight: String = "",

    @Serializable(with = FirebaseInstantSerializer::class)
    val trasvaseDate: Instant? = null,

    val trasvaseBigBag: List<TrasvaseBigBag> = emptyList()
)

@Serializable
data class TrasvaseBigBag(
    val bbTrasNumber: String = "",
    val bbTrasWeight: String = ""
)