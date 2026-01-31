package com.alius.gmrstockplus.domain.model

import kotlinx.serialization.Serializable

@Serializable
data class ClientGroupSell(
    val cliente: Cliente,
    val totalVentasMes: Int,
    val totalKilosVendidos: Int,
    val totalBigBags: Int
)
