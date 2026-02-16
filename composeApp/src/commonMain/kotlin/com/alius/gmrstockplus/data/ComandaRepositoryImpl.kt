package com.alius.gmrstockplus.data

import com.alius.gmrstockplus.data.firestore.FirebaseClient
import com.alius.gmrstockplus.domain.model.*
import dev.gitlive.firebase.firestore.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.withContext
import kotlinx.datetime.*

class ComandaRepositoryImpl(private val plantName: String) : ComandaRepository {

    private val firestore by lazy {
        if (plantName == "P08") FirebaseClient.db08 else FirebaseClient.db07
    }

    private val collection by lazy { firestore.collection("comanda") }
    private val metadataCollection by lazy { firestore.collection("metadata") }

    // --- MAPEO SEGURO ---
    private fun DocumentSnapshot.toComandaSafe(): Comanda? {
        return try {
            val comanda = this.data<Comanda>()
            val firebaseDate = try { this.get<Timestamp>("dateBookedComanda") } catch (e: Exception) { null }

            comanda.copy(
                idComanda = this.id,
                dateBookedComanda = firebaseDate?.let { Instant.fromEpochSeconds(it.seconds, it.nanoseconds) } ?: comanda.dateBookedComanda
            )
        } catch (e: Exception) {
            println("❌ Error mapeando comanda ${this.id}: ${e.message}")
            null
        }
    }

    private suspend fun obtenerSiguienteNumeroDeComanda(): Long = withContext(Dispatchers.IO) {
        val counterDoc = metadataCollection.document("comanda_counter")
        try {
            firestore.runTransaction {
                val snapshot = get(counterDoc)
                val ultimo = snapshot.get<Long>("ultimo") ?: 0L
                val nuevo = ultimo + 1
                update(counterDoc, "ultimo" to nuevo)
                nuevo
            }
        } catch (e: Exception) {
            1L
        }
    }

    // --- QUERIES ---

    override suspend fun listarComandas(filter: String): List<Comanda> = withContext(Dispatchers.IO) {
        try {
            val localDate = LocalDate.parse(filter)
            val inicioDia = localDate.atStartOfDayIn(TimeZone.UTC)
            val finDia = inicioDia.plus(1, DateTimeUnit.DAY, TimeZone.UTC)

            val startTs = Timestamp(inicioDia.epochSeconds, inicioDia.nanosecondsOfSecond)
            val endTs = Timestamp(finDia.epochSeconds, finDia.nanosecondsOfSecond)

            collection
                .where { "dateBookedComanda" greaterThanOrEqualTo startTs }
                .where { "dateBookedComanda" lessThan endTs }
                .orderBy("dateBookedComanda", Direction.ASCENDING)
                .get()
                .documents
                .mapNotNull { it.toComandaSafe() }
        } catch (e: Exception) { emptyList() }
    }

    override suspend fun listarTodasComandas(): List<Comanda> = withContext(Dispatchers.IO) {
        try {
            val monthAgo = Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault()).date.minus(DatePeriod(months = 1))
            val startTs = Timestamp(monthAgo.atStartOfDayIn(TimeZone.UTC).epochSeconds, 0)

            collection
                .where { "dateBookedComanda" greaterThanOrEqualTo startTs }
                .orderBy("dateBookedComanda", Direction.ASCENDING)
                .get()
                .documents
                .mapNotNull { it.toComandaSafe() }
        } catch (e: Exception) { emptyList() }
    }

    override suspend fun getComandaByNumber(number: String): Comanda? = withContext(Dispatchers.IO) {
        val numLong = number.toLongOrNull() ?: return@withContext null
        try {
            collection.where { "numeroDeComanda" equalTo numLong }.get().documents.firstOrNull()?.toComandaSafe()
        } catch (e: Exception) { null }
    }

    override suspend fun getComandaByLoteNumber(loteNumber: String): Comanda? = withContext(Dispatchers.IO) {
        if (loteNumber.isBlank()) return@withContext null
        try {
            val queryLegacy = collection.where { "numberLoteComanda" equalTo loteNumber }.get()
            if (queryLegacy.documents.isNotEmpty()) {
                return@withContext queryLegacy.documents.first().toComandaSafe()
            }

            val snapshot = collection
                .where { "fueVendidoComanda" equalTo false }
                .get()

            snapshot.documents
                .mapNotNull { it.toComandaSafe() }
                .find { comanda ->
                    comanda.listaAsignaciones.any { it.numeroLote == loteNumber }
                }

        } catch (e: Exception) {
            println("❌ Error en getComandaByLoteNumber: ${e.message}")
            null
        }
    }

    override suspend fun getPendingComandasByClient(clientName: String): List<Comanda> = withContext(Dispatchers.IO) {
        try {
            collection
                .where { "bookedClientComanda.cliNombre" equalTo clientName }
                .where { "fueVendidoComanda" equalTo false }
                .get()
                .documents
                .mapNotNull { it.toComandaSafe() }
        } catch (e: Exception) { emptyList() }
    }

    // --- ESCRITURA ---

    override suspend fun addComanda(comanda: Comanda): Boolean = withContext(Dispatchers.IO) {
        try {
            val nuevoNumero = obtenerSiguienteNumeroDeComanda()
            val dateTs = comanda.dateBookedComanda?.let { Timestamp(it.epochSeconds, it.nanosecondsOfSecond) }

            val firstAsignacion = comanda.listaAsignaciones.firstOrNull()

            val comandaMap = mutableMapOf(
                "numeroDeComanda" to nuevoNumero,
                "userEmailComanda" to comanda.userEmailComanda,
                "dateBookedComanda" to dateTs,
                "bookedClientComanda" to comanda.bookedClientComanda,
                "remarkComanda" to comanda.remarkComanda,
                "fueVendidoComanda" to comanda.fueVendidoComanda,
                "listaAsignaciones" to comanda.listaAsignaciones,
                "numberLoteComanda" to (firstAsignacion?.numeroLote ?: comanda.numberLoteComanda),
                "descriptionLoteComanda" to (firstAsignacion?.materialNombre ?: comanda.descriptionLoteComanda),
                "totalWeightComanda" to comanda.totalWeightComanda
            )

            collection.add(comandaMap)
            true
        } catch (e: Exception) { false }
    }

    override suspend fun agregarAsignacionLote(comandaId: String, asignacion: AsignacionLote): Boolean = withContext(Dispatchers.IO) {
        try {
            // Usamos arrayUnion para añadir la nueva asignación (incluyendo userAsignacion)
            collection.document(comandaId).update(
                "listaAsignaciones" to FieldValue.arrayUnion(asignacion)
            )
            true
        } catch (e: Exception) { false }
    }

    /**
     * Sustitución Inteligente: Reemplaza un registro pendiente por uno con lote real.
     * Mantiene la trazabilidad del usuario que realiza la acción.
     */
    override suspend fun actualizarAsignacionLote(
        comandaId: String,
        antigua: AsignacionLote,
        nueva: AsignacionLote
    ): Boolean = withContext(Dispatchers.IO) {
        try {
            val docRef = collection.document(comandaId)

            firestore.runTransaction {
                val snapshot = get(docRef)
                val comanda = snapshot.data<Comanda>()
                val listaActual = comanda.listaAsignaciones.toMutableList()

                // Buscamos el registro "Pendiente" (mismo material, sin lote)
                val indice = listaActual.indexOfFirst {
                    it.materialNombre == antigua.materialNombre && it.numeroLote.isBlank()
                }

                if (indice != -1) {
                    listaActual[indice] = nueva
                } else {
                    listaActual.add(nueva)
                }

                // Actualizamos el documento con la lista nueva y el usuario de la acción
                update(docRef,
                    "listaAsignaciones" to listaActual,
                    "userEmailComanda" to nueva.userAsignacion, // Registramos quién hizo el último cambio
                    "numberLoteComanda" to nueva.numeroLote,
                    "descriptionLoteComanda" to nueva.materialNombre
                )
            }
            true
        } catch (e: Exception) {
            println("❌ Error en actualizarAsignacionLote: ${e.message}")
            false
        }
    }

    override suspend fun quitarAsignacionLote(comandaId: String, loteNumber: String): Boolean = withContext(Dispatchers.IO) {
        try {
            val docRef = collection.document(comandaId)

            firestore.runTransaction {
                val snapshot = get(docRef)
                val comanda = snapshot.data<Comanda>()
                val listaActual = comanda.listaAsignaciones.toMutableList()

                // Buscamos el índice de la asignación que tiene ese número de lote
                val indice = listaActual.indexOfFirst { it.numeroLote == loteNumber }

                if (indice != -1) {
                    // En lugar de borrar la línea, la "limpiamos"
                    // Conservamos el materialNombre para que la comanda sepa qué le falta
                    val asignacionLimpia = listaActual[indice].copy(
                        idLote = "",
                        numeroLote = "",
                        cantidadBB = 0,
                        userAsignacion = "",
                        fueVendido = false
                    )
                    listaActual[indice] = asignacionLimpia

                    // Actualizamos la lista y limpiamos el campo de cabecera 'numberLoteComanda'
                    update(docRef,
                        "listaAsignaciones" to listaActual,
                        "numberLoteComanda" to "" // Opcional: limpiar el lote principal de la cabecera
                    )
                }
            }
            true
        } catch (e: Exception) {
            println("❌ Error en quitarAsignacionLote (Reset): ${e.message}")
            false
        }
    }

    override suspend fun updateComandaRemark(comandaId: String, newRemark: String): Boolean = withContext(Dispatchers.IO) {
        try {
            collection.document(comandaId).update("remarkComanda" to newRemark)
            true
        } catch (e: Exception) { false }
    }

    override suspend fun updateComandaDate(comandaId: String, dateBooked: Instant): Boolean = withContext(Dispatchers.IO) {
        try {
            val dateTs = Timestamp(dateBooked.epochSeconds, dateBooked.nanosecondsOfSecond)
            collection.document(comandaId).update("dateBookedComanda" to dateTs)
            true
        } catch (e: Exception) { false }
    }

    override suspend fun updateComandaBooked(comandaId: String, cliente: Cliente?, dateBooked: Instant?, bookedRemark: String?): Boolean = withContext(Dispatchers.IO) {
        try {
            val dateTs = dateBooked?.let { Timestamp(it.epochSeconds, it.nanosecondsOfSecond) }
            collection.document(comandaId).update(
                "bookedClientComanda" to cliente,
                "dateBookedComanda" to dateTs,
                "remarkComanda" to (bookedRemark ?: "")
            )
            true
        } catch (e: Exception) { false }
    }

    override suspend fun updateComandaLoteNumber(comandaId: String, loteNumber: String): Boolean = withContext(Dispatchers.IO) {
        try {
            collection.document(comandaId).update("numberLoteComanda" to loteNumber)
            true
        } catch (e: Exception) { false }
    }

    override suspend fun updateComandaUser(comandaId: String, userEmail: String): Boolean = withContext(Dispatchers.IO) {
        try {
            collection.document(comandaId).update("userEmailComanda" to userEmail)
            true
        } catch (e: Exception) {
            println("❌ Error actualizando usuario de comanda: ${e.message}")
            false
        }
    }

    override suspend fun deleteComanda(comandaId: String): Boolean = withContext(Dispatchers.IO) {
        try {
            collection.document(comandaId).delete()
            true
        } catch (e: Exception) { false }
    }
}