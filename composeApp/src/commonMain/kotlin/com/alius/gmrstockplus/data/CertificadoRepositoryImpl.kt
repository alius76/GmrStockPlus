package com.alius.gmrstockplus.data

import com.alius.gmrstockplus.data.firestore.FirebaseClient
import com.alius.gmrstockplus.domain.model.Certificado
import dev.gitlive.firebase.firestore.DocumentSnapshot
import dev.gitlive.firebase.firestore.Timestamp
import dev.gitlive.firebase.firestore.where
import io.github.aakira.napier.Napier
import kotlinx.datetime.Instant

class CertificadoRepositoryImpl(
    private val plantName: String
) : CertificadoRepository {

    // 1. REUTILIZAMOS LA INSTANCIA: Evitamos llamar a Firebase.firestore directamente
    // para que iOS no intente re-configurar los settings y lance el crash.
    private val firestore by lazy {
        if (plantName == "P08") FirebaseClient.db08 else FirebaseClient.db07
    }

    override suspend fun getCertificadoByLoteNumber(loteNumber: String): Certificado? {
        Napier.i(message = "🚀 [CertificadoRepo] Buscando en planta: $plantName para lote: $loteNumber")

        return try {
            // Usamos el bloque where { ... } recomendado por GitLive
            val querySnapshot = firestore.collection("certificados")
                .where { "loteNumber" equalTo loteNumber }
                .get()

            val doc = querySnapshot.documents.firstOrNull()

            if (doc != null) {
                // 2. MAPEO SEGURO: Usamos la función manual para procesar la fecha
                val certificado = doc.toCertificadoSafe()
                Napier.i(message = "✅ [CertificadoRepo] Certificado obtenido y mapeado")
                certificado
            } else {
                Napier.w(message = "⚠️ [CertificadoRepo] No se encontró certificado para $loteNumber")
                null
            }
        } catch (e: Exception) {
            Napier.e(message = "❌ [CertificadoRepo] Error en Firestore: ${e.message}")
            null
        }
    }

    /**
     * Función de extensión para mapear el documento de forma segura en iOS.
     * Extrae el Timestamp nativo de Firebase y lo convierte a Kotlin Instant.
     */
    private fun DocumentSnapshot.toCertificadoSafe(): Certificado? {
        return try {
            // Mapeamos los campos simples (status, parametros, loteNumber)
            val base = this.data<Certificado>()

            // Extraemos la fecha manualmente como Timestamp nativo
            val firebaseFecha = try { this.get<Timestamp>("fecha") } catch (e: Exception) { null }

            // Reconstruimos el objeto con la fecha correctamente convertida
            base.copy(
                fecha = firebaseFecha?.let { Instant.fromEpochSeconds(it.seconds, it.nanoseconds) } ?: base.fecha
            )
        } catch (e: Exception) {
            Napier.e(message = "❌ [CertificadoRepo] Error mapeando datos: ${e.message}")
            // Si el mapeo falla totalmente, devolvemos null para no romper la UI
            null
        }
    }
}