package com.alius.gmrstockplus.domain.model

import com.alius.gmrstockplus.core.utils.FirebaseInstantSerializer
import kotlinx.datetime.Instant
import kotlinx.serialization.Serializable
import kotlinx.serialization.Transient

@Serializable
data class Reprocesar(
    @Transient
    val id: String = "",

    // Nombres exactos de Firebase (sin SerialName)
    val reprocesarLoteNumber: String = "",
    val reprocesarDescription: String = "",

    @Serializable(with = FirebaseInstantSerializer::class)
    val reprocesarDate: Instant? = null,

    val reprocesarTotalWeight: String = "",
    val reprocesarLoteDestino: String = "",

    @Serializable(with = FirebaseInstantSerializer::class)
    val reprocesarFechaReproceso: Instant? = null,

    // Lista de BigBags
    val bigBagsReprocesados: List<ReprocesarBigBag> = emptyList()
)

@Serializable
data class ReprocesarBigBag(
    // Asegúrate que en Firebase dentro del array se llamen así
    val bbNumber: String = "",
    val bbWeight: String = ""
)