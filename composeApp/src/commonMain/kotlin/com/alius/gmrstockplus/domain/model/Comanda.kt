package com.alius.gmrstockplus.domain.model

import kotlinx.serialization.Serializable
import kotlinx.datetime.Instant

enum class PlanningFilter {
    TODAS, SEMANA, MES
}

@Serializable
data class Comanda(
    val idComanda: String,
    val numeroDeComanda: Long = 0,
    val numberLoteComanda: String,
    val descriptionLoteComanda: String,
    val dateBookedComanda: Instant? = null,
    val totalWeightComanda: String,
    val bookedClientComanda: Cliente? = null,
    val remarkComanda: String = "",
    val fueVendidoComanda: Boolean = false
)
