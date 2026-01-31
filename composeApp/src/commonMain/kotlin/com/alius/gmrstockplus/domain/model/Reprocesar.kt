package com.alius.gmrstockplus.domain.model

import kotlinx.datetime.Instant
import kotlinx.serialization.Serializable

/**
 * Modelo de dominio para los registros de reproceso (colección "reprocesar").
 * Campos en español para mantener consistencia con el proyecto.
 */
@Serializable
data class Reprocesar(
    val reprocesoNumber: String = "",          // número del lote original que se reprocesa
    val reprocesoDescription: String = "",     // descripción / material
    val reprocesoCreatedAt: Instant? = null,   // fecha de creación del lote original (si procede)
    val reprocesoLoteWeight: String = "",      // peso del lote a reprocesar (string para mantener consistencia)
    val reprocesoTargetLoteNumber: String = "",// número de lote en el que se va a transformar / reutilizar
    val reprocesoDate: Instant? = null,        // fecha del reproceso (movimiento)
    val reprocesoBigBag: List<ReprocesarBigBag> = emptyList()
)

@Serializable
data class ReprocesarBigBag(
    val bbNumber: String = "",   // número del BigBag
    val bbWeight: String = ""    // peso del BigBag (string)
)
