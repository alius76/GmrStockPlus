package com.alius.gmrstockplus.domain.model

import kotlinx.serialization.Serializable

@Serializable
data class AsignacionLote(
    val idLote: String = "",          // Para enlace rápido al documento
    val numeroLote: String = "",      // Para mostrar en la UI sin cargar el lote
    val cantidadBB: Int = 0,          // Cuántos BigBags de este lote van a esta comanda
    val materialNombre: String = "",    // Nombre del material (por si hay varios en una comanda)
    val fueVendido: Boolean = false,       // Para saber si este lote ya salió
    val userAsignacion: String = ""        // Email del usuario que asignó este lote
)
