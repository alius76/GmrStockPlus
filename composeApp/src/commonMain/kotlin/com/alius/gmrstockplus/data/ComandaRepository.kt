package com.alius.gmrstockplus.data

import com.alius.gmrstockplus.domain.model.Comanda
import com.alius.gmrstockplus.domain.model.Cliente
import kotlinx.datetime.Instant

interface ComandaRepository {

    // --- Consultas y Listados ---

    /**
     * Lista comandas filtradas por fecha (o lista completa si no se especifica filtro en algunas implementaciones).
     */
    suspend fun listarComandas(filter: String = ""): List<Comanda>

    /**
     * 🔑 NUEVO: Lista todas las comandas activas/reservadas (fueVendidoComanda = false).
     * Usado para la pantalla de Planning.
     */
    suspend fun listarTodasComandas(): List<Comanda> // 🔑 Nueva función

    /**
     * Obtiene una comanda por su número de documento exacto.
     */
    suspend fun getComandaByNumber(number: String): Comanda?

    suspend fun getComandaByLoteNumber(loteNumber: String): Comanda?

    /**
     * Consulta de Comandas Pendientes por Cliente.
     */
    suspend fun getPendingComandasByClient(clientName: String): List<Comanda>


    // --- Operaciones de Escritura (CRUD) ---

    /**
     * Agrega una nueva comanda (incluyendo la generación del número de comanda).
     */
    suspend fun addComanda(comanda: Comanda): Boolean

    /**
     * Actualiza el campo de observaciones (remark) de una comanda específica.
     */
    suspend fun updateComandaRemark(comandaId: String, newRemark: String): Boolean

    /**
     * Actualiza la fecha de reserva (dateBookedComanda).
     */
    suspend fun updateComandaDate(
        comandaId: String,
        dateBooked: Instant
    ): Boolean

    /**
     * Actualiza el cliente asociado, la fecha de reserva y la observación de la reserva.
     */
    suspend fun updateComandaBooked(
        comandaId: String,
        cliente: Cliente?,
        dateBooked: Instant?,
        bookedRemark: String? = null
    ): Boolean

    /**
     * Asignación del Número de Lote a la Comanda.
     */
    suspend fun updateComandaLoteNumber(comandaId: String, loteNumber: String): Boolean

    /**
     * Elimina una comanda por su ID.
     */
    suspend fun deleteComanda(comandaId: String): Boolean
}



// Implementación expect
expect fun getComandaRepository(databaseUrl: String): ComandaRepository