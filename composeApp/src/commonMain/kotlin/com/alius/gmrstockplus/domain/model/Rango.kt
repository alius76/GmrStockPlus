package com.alius.gmrstockplus.domain.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class Rango(
    @SerialName("valorMin") val valorMin: Double? = null,
    @SerialName("valorMax") val valorMax: Double? = null
)
