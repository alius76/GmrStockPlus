package com.alius.gmrstockplus.data.mappers

import com.alius.gmrstockplus.domain.model.*

object TraceMapper {

    fun fromLote(lote: LoteModel) = TraceEvent(
        date = lote.createdAt,
        type = TraceEventType.CREACION,
        title = "Creación: ${lote.number}",
        subtitle = lote.description,
        totalWeight = lote.totalWeight,
        bigBags = lote.bigBag.map { TraceBigBag(it.bbNumber, it.bbWeight) },
        referenceId = lote.id
    )

    fun fromVenta(venta: Venta) = TraceEvent(
        date = venta.ventaFecha,
        type = TraceEventType.VENTA,
        title = "Venta: ${venta.ventaCliente}",
        subtitle = venta.ventaMaterial,
        totalWeight = venta.ventaPesoTotal ?: "0",
        bigBags = venta.ventaBigbags.map { TraceBigBag(it.ventaBbNumber, it.ventaBbWeight) },
        referenceId = venta.ventaLote
    )

    /**
     * Adaptado a los nombres reales del modelo Reprocesar
     */
    fun fromReproceso(repro: Reprocesar) = TraceEvent(
        date = repro.reprocesarDate ?: repro.reprocesarFechaReproceso, // Usamos la fecha disponible
        type = TraceEventType.REPROCESO,
        title = "Reprocesado a: ${repro.reprocesarLoteDestino}", // Cambiado de targetLoteNumber
        subtitle = repro.reprocesarDescription,
        totalWeight = repro.reprocesarTotalWeight, // Cambiado de LoteWeight
        bigBags = repro.bigBagsReprocesados.map { TraceBigBag(it.bbNumber, it.bbWeight) }, // Cambiado de reprocesoBigBag
        referenceId = repro.reprocesarLoteNumber // Cambiado de reprocesoNumber
    )

    fun fromDevolucion(dev: Devolucion) = TraceEvent(
        date = dev.devolucionFecha,
        type = TraceEventType.DEVOLUCION,
        title = "Devolución: ${dev.devolucionCliente}",
        subtitle = dev.devolucionMaterial,
        totalWeight = dev.devolucionPesoTotal ?: "0",
        bigBags = dev.devolucionBigbags.map { TraceBigBag(it.devolucionBbNumber, it.devolucionBbWeight) },
        referenceId = dev.devolucionLote
    )
}