package com.alius.gmrstockplus.domain.model

import com.alius.gmrstockplus.core.utils.FirebaseInstantSerializer
import kotlinx.serialization.Serializable
import kotlinx.datetime.Instant

@Serializable
data class Process(
    var id: String = "",
    var number: String = "",
    var description: String = "",
    @Serializable(with = FirebaseInstantSerializer::class)
    var date: Instant? = null
)
