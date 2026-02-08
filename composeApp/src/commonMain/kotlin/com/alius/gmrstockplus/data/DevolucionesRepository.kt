package com.alius.gmrstockplus.data

import com.alius.gmrstockplus.domain.model.LoteModel

interface DevolucionesRepository {

    /**
     * Devuelve un lote específico por su número para verificar su estado actual.
     */
    suspend fun getLoteByNumber(number: String): LoteModel?

    /**
     * Marca un BigBag individual como devuelto.
     * Implementa la lógica de bbStatus = "s" y recalcula totales.
     */
    suspend fun devolverBigBag(loteNumber: String, bigBagNumber: String): Boolean

    /**
     * Marca MÚLTIPLES BigBags como devueltos en una única operación.
     * Recalcula automáticamente el count y totalWeight en el repositorio
     * para evitar inconsistencias de datos desde la UI.
     *
     * @param loteNumber El número del lote a actualizar.
     * @param bigBagNumbers La lista de identificadores de BigBags a retornar al stock.
     */
    suspend fun devolverBigBags(
        loteNumber: String,
        bigBagNumbers: List<String>
    ): Boolean
}

/**
 * Función expect para la inyección multiplataforma.
 * Ahora utiliza [plantId] para determinar si conectar con la base de datos P07 o P08.
 */
expect fun getDevolucionesRepository(plantId: String): DevolucionesRepository