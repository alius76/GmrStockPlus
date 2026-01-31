package com.alius.gmrstockplus.domain.model

import kotlinx.serialization.Serializable

@Serializable
data class Cliente(
    val cliNombre : String ="",
    val cliObservaciones: String = ""
)
