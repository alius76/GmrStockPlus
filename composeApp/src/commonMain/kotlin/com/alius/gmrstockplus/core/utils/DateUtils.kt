package com.alius.gmrstockplus.core.utils

import kotlinx.datetime.Instant
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime

fun formatInstant(instant: Instant?): String {
    if (instant == null) return ""
    val localDate = instant.toLocalDateTime(TimeZone.currentSystemDefault()).date
    return "${localDate.dayOfMonth.toString().padStart(2, '0')}/" +
            "${localDate.monthNumber.toString().padStart(2, '0')}/" +
            "${localDate.year}"
}



