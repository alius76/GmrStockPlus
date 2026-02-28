package com.alius.gmrstockplus.domain.model

import com.alius.gmrstockplus.core.utils.FirebaseInstantSerializer
import kotlinx.serialization.Serializable
import kotlinx.datetime.Instant

@Serializable
data class Venta(
    val ventaCliente: String = "",
    val ventaIdComanda: String? = null, // ID técnico de Firestore
    val ventaNumeroComanda: Long = 0L,   // <--- AÑADIR ESTE CAMPO (Trazabilidad legible)
    val ventaLote: String = "",
    val ventaMaterial: String = "",

    @Serializable(with = FirebaseInstantSerializer::class)
    val ventaFecha: Instant? = null,

    val ventaPesoTotal: String? = null,
    val ventaBigbags: List<VentaBigbag> = emptyList(),

    // Campos de control y auditoría
    val ventaUser: String = "", // Aquí registrarás el usuario de la reserva/comando
    val ventaFinalizaComanda: Boolean = false
)