package com.alius.gmrstockplus.domain.model

import com.alius.gmrstockplus.core.utils.FirebaseInstantSerializer
import kotlinx.serialization.Serializable
import kotlinx.datetime.Instant

@Serializable
data class Devolucion(
    val devolucionCliente: String = "",
    val devolucionLote: String = "",
    val devolucionMaterial: String = "",
    @Serializable(with = FirebaseInstantSerializer::class)
    val devolucionFecha: Instant? = null,
    val devolucionPesoTotal: String? = null,
    val devolucionBigbags: List<DevolucionBigbag> = emptyList()
)

