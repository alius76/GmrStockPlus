package com.alius.gmrstockplus.data

import com.alius.gmrstockplus.domain.model.*
import kotlinx.datetime.Instant

interface ComandaRepository {

    // --- Consultas y Listados ---

    /**
     * Lista comandas filtradas por fecha.
     */
    suspend fun listarComandas(filter: String = ""): List<Comanda>

    /**
     * Lista todas las comandas activas/reservadas (fueVendidoComanda = false).
     * Esencial para la vista de Planning Global.
     */
    suspend fun listarTodasComandas(): List<Comanda>

    /**
     * Obtiene una comanda por su número correlativo (ej: 000123).
     */
    suspend fun getComandaByNumber(number: String): Comanda?

    /**
     * Busca una comanda que contenga un lote específico, ya sea en el campo
     * legacy o dentro de la lista de asignaciones.
     */
    suspend fun getComandaByLoteNumber(loteNumber: String): Comanda?

    /**
     * Consulta de Comandas Pendientes (no vendidas) para un Cliente específico.
     */
    suspend fun getPendingComandasByClient(clientName: String): List<Comanda>

    /**
     * Obtiene el desglose de ocupación para un lote específico.
     * Crucial para calcular el stock disponible real (Total - Ocupado).
     */
    suspend fun getOccupancyByLote(loteNumber: String): List<OccupancyInfo>


    // --- Operaciones de Escritura (CRUD) ---

    /**
     * Crea una nueva comanda. Genera automáticamente el número y registra el usuario autor.
     */
    suspend fun addComanda(comanda: Comanda): Boolean

    /**
     * Añade una nueva asignación (Lote + cantidad BB) a la lista existente sin sobrescribir.
     */
    suspend fun agregarAsignacionLote(comandaId: String, asignacion: AsignacionLote): Boolean

    /**
     * Elimina una asignación específica de la lista de la comanda.
     */
    suspend fun quitarAsignacionLote(comandaId: String, loteNumber: String): Boolean

    /**
     * Actualiza las notas internas de la comanda.
     */
    suspend fun updateComandaRemark(comandaId: String, newRemark: String): Boolean

    /**
     * Cambia la fecha planificada de la comanda.
     */
    suspend fun updateComandaDate(comandaId: String, dateBooked: Instant): Boolean

    /**
     * Vincula un cliente y una fecha a la comanda (proceso de reserva).
     */
    suspend fun updateComandaBooked(
        comandaId: String,
        cliente: Cliente?,
        dateBooked: Instant?,
        bookedRemark: String? = null
    ): Boolean

    /**
     * Legacy: Actualiza el número de lote principal (para compatibilidad).
     */
    suspend fun updateComandaLoteNumber(comandaId: String, loteNumber: String): Boolean

    /**
     * Registra el email del usuario que realiza cambios o asignaciones en la comanda.
     * Esencial para la trazabilidad de GmrStockPlus.
     */
    suspend fun updateComandaUser(comandaId: String, userEmail: String): Boolean

    /**
     * Elimina físicamente el documento de la comanda.
     */
    suspend fun deleteComanda(comandaId: String): Boolean

    /**
     * Actualiza una asignación existente.
     * Se usa para la "Asignación Inteligente": busca un registro pendiente (antigua)
     * y lo sustituye por la información real del lote (nueva).
     */
    suspend fun actualizarAsignacionLote(
        comandaId: String,
        antigua: AsignacionLote,
        nueva: AsignacionLote
    ): Boolean
}

// Implementación expect según plataforma (Android/iOS/Desktop)
expect fun getComandaRepository(plantName: String): ComandaRepository