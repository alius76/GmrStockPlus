package com.alius.gmrstockplus.domain.model

import com.alius.gmrstockplus.core.utils.FirebaseInstantSerializer
import kotlinx.serialization.Serializable
import kotlinx.datetime.Instant

enum class PlanningFilter {
    TODAS, SEMANA, MES
}

@Serializable
data class Comanda(
    val idComanda: String,
    val numeroDeComanda: Long = 0,
    val userEmailComanda: String = "",

    // Compatibilidad
    val numberLoteComanda: String = "",
    val descriptionLoteComanda: String = "",
    val totalWeightComanda: String = "",

    val bookedClientComanda: Cliente? = null,
    val remarkComanda: String = "",
    val fueVendidoComanda: Boolean = false, // Este es tu "flag" principal

    @Serializable(with = FirebaseInstantSerializer::class)
    val dateBookedComanda: Instant? = null,

    val listaAsignaciones: List<AsignacionLote> = emptyList()

)
