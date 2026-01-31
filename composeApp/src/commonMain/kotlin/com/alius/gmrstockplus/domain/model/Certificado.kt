package com.alius.gmrstockplus.domain.model

import kotlinx.serialization.Serializable
import kotlinx.datetime.Instant
import kotlinx.serialization.SerialName

@Serializable
enum class CertificadoStatus {
    @SerialName("CORRECTO") CORRECTO,
    @SerialName("ADVERTENCIA") ADVERTENCIA,
    @SerialName("SIN_DATOS") SIN_DATOS
}

@Serializable
data class Certificado(
    @SerialName("loteNumber") val loteNumber: String = "",
    @SerialName("fecha") val fecha: Instant? = null,
    @SerialName("status") val status: CertificadoStatus = CertificadoStatus.SIN_DATOS,
    @SerialName("parametros") val parametros: List<Parametro> = emptyList()
)
