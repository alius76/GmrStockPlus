package com.alius.gmrstockplus.domain.model

import kotlinx.serialization.Serializable

@Serializable
data class VentaBigbag(
    val ventaBbNumber: String = "",
    var ventaBbWeight: String = ""

)
