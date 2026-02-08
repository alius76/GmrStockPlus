package com.alius.gmrstockplus.domain.model

import com.alius.gmrstockplus.core.utils.FirebaseInstantSerializer
import kotlinx.serialization.Serializable
import kotlinx.datetime.Instant

@Serializable
data class Venta(
    val ventaCliente: String = "",
    val ventaLote: String = "",
    val ventaMaterial: String = "",
    @Serializable(with = FirebaseInstantSerializer::class)
    val ventaFecha: Instant?,
    val ventaPesoTotal: String? = null,
    val ventaBigbags: List<VentaBigbag> = emptyList()
)
