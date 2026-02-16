package com.alius.gmrstockplus.domain.model

data class OccupancyInfo(
    val cliente: String,
    val cantidad: Int,
    val numeroComanda: String,
    val fecha: String,
    val usuario: String
)
