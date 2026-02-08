package com.alius.gmrstockplus.data

import com.alius.gmrstockplus.domain.model.LoteModel
import com.alius.gmrstockplus.domain.model.MaterialGroup

fun Double.formatWeight(): String {
    return if (this == this.toInt().toDouble()) {
        this.toInt().toString()
    } else {
        val rounded = kotlin.math.round(this * 100) / 100
        rounded.toString()
    }
}

fun agruparPorMaterial(lotes: List<LoteModel>): List<MaterialGroup> {
    // Filtramos descripciones vacías
    val lotesValidos = lotes.filter { it.description.isNotBlank() }

    return lotesValidos.groupBy { it.description }
        .map { (descripcion, lotesDelGrupo) ->
            val totalWeightDouble = lotesDelGrupo.sumOf {
                it.totalWeight.replace(",", ".").toDoubleOrNull() ?: 0.0
            }

            val totalBigBags = lotesDelGrupo.sumOf {
                it.count.toIntOrNull() ?: 0
            }

            MaterialGroup(
                description = descripcion,
                totalWeight = totalWeightDouble.formatWeight(),
                totalLotes = lotesDelGrupo.size,
                totalBigBags = totalBigBags,
                // 🔑 Mantenemos String para que tu GroupMaterialBottomSheetContent
                // siga funcionando con su propia carga por número de lote.
                loteNumbers = lotesDelGrupo.map { it.number }
            )
        }
        .sortedBy { it.description }
}