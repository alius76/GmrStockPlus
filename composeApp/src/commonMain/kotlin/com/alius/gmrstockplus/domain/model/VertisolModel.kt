package com.alius.gmrstockplus.domain.model

import kotlinx.datetime.Instant
import kotlinx.serialization.Serializable

@Serializable
data class Vertisol(
    val vertisolNumber: String,
    val vertisolDescription: String,
    val vertisolLocation: String,
    val vertisolCount: String,
    val vertisolTotalWeight: String,
    val vertisolCompletado: Boolean = true,
    val vertisolBigBag: List<VertisolBigBag>
)

@Serializable
data class VertisolBigBag(
    val bbNumber: String = "",
    val bbWeight: String = "",
    val bbTrasvaseDate: Instant? = null
)
