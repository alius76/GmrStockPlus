package com.alius.gmrstockplus.domain.model

import kotlinx.serialization.Serializable
import kotlinx.datetime.Instant

@Serializable
data class Process(
    var id: String = "",
    var number: String = "",
    var description: String = "",
    var date: Instant? = null
)
