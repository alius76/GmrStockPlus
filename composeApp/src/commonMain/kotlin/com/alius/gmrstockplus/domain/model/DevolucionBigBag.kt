package com.alius.gmrstockplus.domain.model

import kotlinx.serialization.Serializable

@Serializable
data class DevolucionBigbag(
    val devolucionBbNumber: String = "",
    var devolucionBbWeight: String = ""
)
