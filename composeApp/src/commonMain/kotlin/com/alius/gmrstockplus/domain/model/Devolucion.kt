package com.alius.gmrstockplus.domain.model

import kotlinx.serialization.Serializable
import kotlinx.datetime.Instant

@Serializable
data class Devolucion(
    val devolucionCliente: String = "",
    val devolucionLote: String = "",
    val devolucionMaterial: String = "",
    val devolucionFecha: Instant? = null,
    val devolucionPesoTotal: String? = null,
    val devolucionBigbags: List<DevolucionBigbag> = emptyList()
)

