package com.alius.gmrstockplus.domain.model

import kotlinx.serialization.Serializable


@Serializable
data class Ratio(
    val ratioId: String,
    val ratioDate: Long,          // timestamp en millis
    val ratioTotalWeight: String,  // peso en kilos como string
    val ratioLoteId: String
)