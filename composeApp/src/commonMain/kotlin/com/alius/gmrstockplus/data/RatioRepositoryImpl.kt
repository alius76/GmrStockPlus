package com.alius.gmrstockplus.data

import com.alius.gmrstockplus.data.firestore.FirebaseClient
import com.alius.gmrstockplus.data.mappers.RatioMapper
import com.alius.gmrstockplus.domain.model.Ratio
import io.github.aakira.napier.Napier
import kotlinx.datetime.*

class RatioRepositoryImpl(
    private val plantId: String
) : RatioRepository {

    // Obtenemos la instancia de Firestore (P07 o P08) desde nuestro mediador central
    private val db = FirebaseClient.getDb(plantId)
    private val collection = db.collection("ratios")

    override suspend fun listarRatiosDelDia(): List<Ratio> {
        val hoy = Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault()).date
        return listarRatiosPorRango(hoy, hoy)
    }

    override suspend fun listarRatiosDelMes(): List<Ratio> {
        val ahora = Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault())
        val inicioMes = LocalDate(ahora.year, ahora.month, 1)
        // Calculamos el último día del mes actual
        val finMes = inicioMes.plus(1, DateTimeUnit.MONTH).minus(1, DateTimeUnit.DAY)
        return listarRatiosPorRango(inicioMes, finMes)
    }

    override suspend fun listarRatiosDelAno(): List<Ratio> {
        val ahora = Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault())
        val inicioAno = LocalDate(ahora.year, 1, 1)
        val finAno = LocalDate(ahora.year, 12, 31)
        return listarRatiosPorRango(inicioAno, finAno)
    }

    override suspend fun listarRatiosPorRango(inicio: LocalDate, fin: LocalDate): List<Ratio> {
        Napier.d { "🔥 Firestore SDK: Listando ratios de $plantId desde $inicio hasta $fin" }
        return try {
            // ✅ SINTAXIS COMPATIBLE:
            // Usamos el FilterBuilder de forma explícita para que devuelva un Filter (no Unit)
            val snapshot = collection
                .where {
                    all(
                        "ratioDate" greaterThanOrEqualTo inicio.toString(),
                        "ratioDate" lessThanOrEqualTo fin.toString()
                    )
                }
                .get()

            snapshot.documents.map { doc ->
                RatioMapper.fromFirestore(doc.data())
            }
        } catch (e: Exception) {
            Napier.e(e) { "❌ Error Firestore SDK en $plantId: ${e.message}" }
            // Imprime el error completo para debuguear si es un error de índices de Firestore
            e.printStackTrace()
            emptyList()
        }
    }

    suspend fun obtenerProgresoMensual(): Float {
        val objetivoMensual = 1_500_000.0

        // Obtenemos los ratios usando tu función que ya filtra por mes
        val ratiosDelMes = listarRatiosDelMes()

        // 🛠️ SUMA CORRECTA: Convertimos el String a Double para sumar
        val totalKilosMes = ratiosDelMes.sumOf {
            it.ratioTotalWeight.toDoubleOrNull() ?: 0.0
        }

        // Calculamos el porcentaje (0.0 a 1.0)
        val porcentaje = (totalKilosMes / objetivoMensual).toFloat()

        return porcentaje.coerceAtMost(1.0f)
    }

    override suspend fun listarRatiosUltimos12Meses(): List<Ratio> {
        val hoy = Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault()).date
        val haceUnAno = hoy.minus(1, DateTimeUnit.YEAR)
        return listarRatiosPorRango(haceUnAno, hoy)
    }
}