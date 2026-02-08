package com.alius.gmrstockplus.data

import com.alius.gmrstockplus.domain.model.Trasvase
import com.alius.gmrstockplus.domain.model.TrasvaseBigBag

/**
 * Interface que define las operaciones de acceso a datos para la colección "trasvase".
 * Implementada utilizando el SDK nativo de Firebase para asegurar la sincronización y el rendimiento.
 */
interface TrasvaseRepository {

    /**
     * Retorna el primer objeto Trasvase encontrado para un número de lote.
     * @param trasvaseNumber El número de lote a buscar.
     * @return El objeto Trasvase o null si no existe ningún registro.
     */
    suspend fun getTrasvaseByLote(trasvaseNumber: String): Trasvase?

    /**
     * Retorna solo la lista de BigBags asociada a un número de lote.
     * Útil para desgloses de peso y conteo de bultos sin procesar toda la lógica del trasvase.
     * @param trasvaseNumber El número de lote a buscar.
     * @return Lista de [TrasvaseBigBag], vacía si no se encuentra el lote.
     */
    suspend fun getTrasvaseBigBagsByLote(trasvaseNumber: String): List<TrasvaseBigBag>

    /**
     * Retorna todos los trasvases asociados a un número de lote.
     * @param trasvaseNumber El número de lote a buscar.
     * @return Lista de objetos [Trasvase]. Retorna lista vacía si no se encuentra ninguno.
     */
    suspend fun getTrasvasesByLote(trasvaseNumber: String): List<Trasvase>
}

/**
 * Función multiplataforma (expect) para obtener una instancia de TrasvaseRepository.
 * * @param databaseUrl En tu arquitectura, este parámetro se utiliza para inyectar
 * el identificador de la planta (P07 o P08) y seleccionar la base de datos correcta.
 */
expect fun getTrasvaseRepository(plantId: String): TrasvaseRepository
