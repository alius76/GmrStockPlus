package com.alius.gmrstockplus.data.mappers

import com.alius.gmrstockplus.domain.model.Ratio
import kotlinx.datetime.Instant

object RatioMapper {

    /**
     * Mapea el Map directo que devuelve el SDK de Firebase a nuestro dominio.
     * Ya no hay "stringValue" o "jsonObject" manual.
     */
    fun fromFirestore(data: Map<String, Any>): Ratio {
        return Ratio(
            ratioId = data["ratioId"] as? String ?: "",
            // El SDK suele devolver los timestamps de Firestore como objetos nativos o Strings ISO
            ratioDate = (data["ratioDate"] as? String)?.let {
                try { Instant.parse(it).toEpochMilliseconds() } catch(e: Exception) { 0L }
            } ?: 0L,
            ratioTotalWeight = data["ratioTotalWeight"]?.toString() ?: "0",
            ratioLoteId = data["ratioLoteId"] as? String ?: ""
        )
    }
}