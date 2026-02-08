package com.alius.gmrstockplus.data

import com.alius.gmrstockplus.data.mappers.TraceMapper
import com.alius.gmrstockplus.domain.model.TraceEvent
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.async
import kotlinx.coroutines.withContext
import kotlinx.datetime.Instant

class GetLoteTraceUseCase(
    private val loteRepo: LoteRepository,
    private val historialRepo: HistorialRepository,
    private val ventaRepo: VentaRepository,
    private val reproRepo: ReprocesarRepository,
    private val devRepo: DevolucionRepository
) {
    suspend fun execute(loteNumber: String): List<TraceEvent> = withContext(Dispatchers.IO) {
        println("🔍 [DEBUG] Buscando trazabilidad para: $loteNumber")

        // 1. ORIGEN (Consultas en paralelo para encontrar el lote madre)
        val loteOrigen = async {
            val activo = loteRepo.getLoteByNumber(loteNumber)
            if (activo != null) {
                println("✅ [DEBUG] Lote encontrado en STOCK ACTIVO")
                activo
            } else {
                val historial = historialRepo.getLoteHistorialByNumber(loteNumber)
                if (historial != null) println("✅ [DEBUG] Lote encontrado en HISTORIAL")
                historial
            }
        }

        // 2. OTROS MOVIMIENTOS (Consultas simultáneas en otras colecciones)
        val ventasDef = async { ventaRepo.obtenerVentasPorLote(loteNumber) }
        val reprocesoDef = async { reproRepo.getReprocesoByNumber(loteNumber) }
        val devolucionesDef = async { devRepo.obtenerDevolucionesPorLote(loteNumber) }

        val events = mutableListOf<TraceEvent>()

        // 3. PROCESAR ORIGEN + Lógica de Peso (Reconstrucción si el header está en 0)
        loteOrigen.await()?.let { lote ->
            var event = TraceMapper.fromLote(lote)

            val pesoHeader = lote.totalWeight.toDoubleOrNull() ?: 0.0
            if (pesoHeader <= 0.0) {
                val pesoCalculado = lote.bigBag.sumOf { it.bbWeight.toDoubleOrNull() ?: 0.0 }
                println("⚖️ [DEBUG] Peso 0 detectado. Peso calculado de BBs: $pesoCalculado")
                event = event.copy(totalWeight = pesoCalculado.toString())
            }
            events.add(event)
        }

        // 4. PROCESAR VENTAS
        val ventas = ventasDef.await()
        println("📦 [DEBUG] Ventas encontradas: ${ventas.size}")
        ventas.forEach { events.add(TraceMapper.fromVenta(it)) }

        // 5. PROCESAR REPROCESOS
        val repro = reprocesoDef.await()
        if (repro != null) {
            println("♻️ [DEBUG] REPROCESO ENCONTRADO: $repro")
            events.add(TraceMapper.fromReproceso(repro))
        } else {
            println("⚠️ [DEBUG] No se encontró reproceso para el lote $loteNumber")
        }

        // 6. PROCESAR DEVOLUCIONES
        val devs = devolucionesDef.await()
        println("↩️ [DEBUG] Devoluciones encontradas: ${devs.size}")
        devs.forEach { events.add(TraceMapper.fromDevolucion(it)) }

        // 7. ORDENACIÓN (Más reciente primero)
        events.sortedByDescending { it.date ?: Instant.DISTANT_PAST }
    }
}