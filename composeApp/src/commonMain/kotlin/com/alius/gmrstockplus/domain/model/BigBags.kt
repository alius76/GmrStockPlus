package com.alius.gmrstockplus.domain.model

import kotlinx.serialization.Serializable

@Serializable
data class BigBags(
    val bbNumber: String = "",
    var bbWeight: String = "",
    var bbLocation: String = "",
    val bbStatus: String = "",
    var bbRemark: String = ""
)