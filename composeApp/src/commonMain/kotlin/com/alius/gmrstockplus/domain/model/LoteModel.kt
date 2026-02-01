package com.alius.gmrstockplus.domain.model

import kotlinx.serialization.Serializable
import kotlinx.datetime.Instant
import com.alius.gmrstockplus.core.utils.FirebaseInstantSerializer


@Serializable
data class LoteModel(
    val id: String = "",
    val number: String = "",
    val description: String = "",
    @Serializable(with = FirebaseInstantSerializer::class)
    val date: Instant? = null,
    val location: String = "",
    val count: String = "",
    val weight: String = "",
    val status: String = "",
    val totalWeight: String = "",
    val qrCode: String? = null,
    val bigBag: List<BigBags> = emptyList(),
    val booked: Cliente? = null,
    @Serializable(with = FirebaseInstantSerializer::class)
    val dateBooked: Instant? = null,
    val bookedByUser: String? = null,
    val bookedRemark: String? = null,
    val remark: String = "",
    val certificateOk: Boolean = false,
    @Serializable(with = FirebaseInstantSerializer::class)
    val createdAt: Instant? = null
)