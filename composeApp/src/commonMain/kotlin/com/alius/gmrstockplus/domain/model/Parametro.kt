package com.alius.gmrstockplus.domain.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class Parametro(
    @SerialName("descripcion") val descripcion: String = "",
    @SerialName("rango") val rango: Rango? = null,
    @SerialName("unidad") val unidad: String = "",
    @SerialName("code") val code: String = "",
    @SerialName("valor") val valor: String = "",
    @SerialName("warning") val warning: Boolean = false
)
