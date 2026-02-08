package com.alius.gmrstockplus.data

import com.alius.gmrstockplus.domain.model.Certificado
import dev.gitlive.firebase.Firebase
import dev.gitlive.firebase.firestore.firestore
import dev.gitlive.firebase.firestore.where
import io.github.aakira.napier.Napier

class CertificadoRepositoryImpl(
    private val plantName: String
) : CertificadoRepository {

    // Obtenemos la instancia del SDK de GitLive
    private val firestore = Firebase.firestore

    override suspend fun getCertificadoByLoteNumber(loteNumber: String): Certificado? {
        Napier.i(message = "🚀 [SDK] Buscando certificado en planta: $plantName para lote: $loteNumber")

        return try {
            // El SDK gestiona la conexión y la seguridad automáticamente
            val querySnapshot = firestore.collection("certificados")
                .where("loteNumber", equalTo = loteNumber)
                .get()

            if (querySnapshot.documents.isNotEmpty()) {

                val certificado = querySnapshot.documents.first().data<Certificado>()
                Napier.i(message = "✅ [SDK] Certificado mapeado correctamente")
                certificado
            } else {
                null
            }
        } catch (e: Exception) {
            Napier.e(message = "❌ [SDK] Error en Firestore: ${e.message}")
            null
        }
    }
}