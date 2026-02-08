package com.alius.gmrstockplus.data

import com.alius.gmrstockplus.data.firestore.FirebaseClient
import com.alius.gmrstockplus.domain.model.Ratio
import io.github.aakira.napier.Napier
import kotlinx.datetime.*
import dev.gitlive.firebase.firestore.where
import dev.gitlive.firebase.firestore.Timestamp
import dev.gitlive.firebase.firestore.DocumentSnapshot

class RatioRepositoryImpl(
    private val plantId: String
) : RatioRepository {

    private val db = FirebaseClient.getDb(plantId)
    private val collection = db.collection("ratio")
    private val TAG = "GMR_REPO"

    /**
     * Mapper manual para convertir el DocumentSnapshot de Firestore
     * al modelo de dominio Ratio, manejando la conversión de Timestamp a Long.
     */
    private fun mapDocumentToRatio(doc: DocumentSnapshot): Ratio {
        // Usamos .get() con el tipo explícito para cada campo.
        // Esto evita que Firebase intente serializar el mapa completo.

        val id = try { doc.get<String>("ratioId") } catch (e: Exception) { "" }
        val loteId = try { doc.get<String>("ratioLoteId") } catch (e: Exception) { "" }
        val weight = try { doc.get<String>("ratioTotalWeight") } catch (e: Exception) { "0" }

        // Para la fecha, obtenemos el Timestamp y lo pasamos a Long
        val dateLong = try {
            val ts = doc.get<Timestamp>("ratioDate")
            ts.seconds * 1000
        } catch (e: Exception) {
            0L
        }

        return Ratio(
            ratioId = id ?: "",
            ratioLoteId = loteId ?: "",
            ratioTotalWeight = weight ?: "0",
            ratioDate = dateLong
        )
    }

    override suspend fun listarRatiosPorRango(inicio: LocalDate, fin: LocalDate): List<Ratio> {
        val inicioTimestamp = Timestamp(inicio.atStartOfDayIn(TimeZone.currentSystemDefault()).epochSeconds, 0)
        val finTimestamp = Timestamp(fin.plus(1, DateTimeUnit.DAY).atStartOfDayIn(TimeZone.currentSystemDefault()).epochSeconds, 0)

        Napier.d(tag = TAG) { "🔍 [PLANTA $plantId] Buscando entre $inicio y $fin" }

        return try {
            val snapshot = collection
                .where {
                    all(
                        "ratioDate" greaterThanOrEqualTo inicioTimestamp,
                        "ratioDate" lessThan finTimestamp
                    )
                }
                .get()

            Napier.d(tag = TAG) { "📦 [PLANTA $plantId] Documentos encontrados: ${snapshot.documents.size}" }

            snapshot.documents.map { doc ->
                mapDocumentToRatio(doc)
            }
        } catch (e: Exception) {
            Napier.e(tag = TAG, throwable = e) { "❌ Error en Firestore: ${e.message}" }
            emptyList()
        }
    }

    override suspend fun obtenerProgresoMensual(): Float {
        val objetivoMensual = 1_000_000.0
        val ratiosDelMes = listarRatiosDelMes()

        val totalKilosMes = ratiosDelMes.sumOf { ratio ->
            ratio.ratioTotalWeight
                .replace(",", ".")
                .filter { it.isDigit() || it == '.' || it == '-' }
                .toDoubleOrNull() ?: 0.0
        }

        val porcentaje = (totalKilosMes / objetivoMensual).toFloat()
        Napier.d(tag = TAG) { "📊 Suma Total: $totalKilosMes / $objetivoMensual -> $porcentaje" }

        return porcentaje.coerceIn(0f, 1f)
    }

    // --- Métodos de conveniencia delegados a listarRatiosPorRango ---

    override suspend fun listarRatiosDelDia(): List<Ratio> {
        val hoy = Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault()).date
        return listarRatiosPorRango(hoy, hoy)
    }

    override suspend fun listarRatiosDelMes(): List<Ratio> {
        val ahora = Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault())
        val inicioMes = LocalDate(ahora.year, ahora.month, 1)
        val finMes = inicioMes.plus(1, DateTimeUnit.MONTH).minus(1, DateTimeUnit.DAY)
        return listarRatiosPorRango(inicioMes, finMes)
    }

    override suspend fun listarRatiosDelAno(): List<Ratio> {
        val ahora = Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault())
        val inicioAno = LocalDate(ahora.year, 1, 1)
        val finAno = LocalDate(ahora.year, 12, 31)
        return listarRatiosPorRango(inicioAno, finAno)
    }

    override suspend fun listarRatiosUltimos12Meses(): List<Ratio> {
        val hoy = Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault()).date
        val haceUnAno = hoy.minus(1, DateTimeUnit.YEAR)
        return listarRatiosPorRango(haceUnAno, hoy)
    }
}