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

    // --- FUNCIONES DE CONVERSIÓN MANUAL (CRUCIAL PARA EVITAR CRASHES EN iOS) ---

    private fun AsignacionLote.toMap(): Map<String, Any?> = mapOf(
        "idLote" to idLote,
        "numeroLote" to numeroLote,
        "materialNombre" to materialNombre,
        "cantidadBB" to cantidadBB,
        "userAsignacion" to userAsignacion,
        "fueVendido" to fueVendido
    )

    private fun Cliente.toMap(): Map<String, Any?> = mapOf(
        "cliNombre" to cliNombre,
        "cliObservaciones" to cliObservaciones
    )

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

    override suspend fun getOccupancyByLote(loteNumber: String): List<OccupancyInfo> = withContext(Dispatchers.IO) {
        if (loteNumber.isBlank()) return@withContext emptyList()
        try {
            val snapshot = collection
                .where { "fueVendidoComanda" equalTo false }
                .get()

            snapshot.documents
                .mapNotNull { it.toComandaSafe() }
                .flatMap { comanda ->
                    comanda.listaAsignaciones
                        .filter { it.numeroLote == loteNumber && !it.fueVendido && it.cantidadBB > 0 }
                        .map { asig ->
                            OccupancyInfo(
                                cliente = comanda.bookedClientComanda?.cliNombre ?: "Sin Nombre",
                                cantidad = asig.cantidadBB,
                                numeroComanda = comanda.numeroDeComanda.toString(),
                                fecha = comanda.dateBookedComanda?.let { com.alius.gmrstockplus.core.utils.formatInstant(it) } ?: "--/--/--",
                                usuario = asig.userAsignacion.ifEmpty { comanda.userEmailComanda }.split("@").first()
                            )
                        }
                }
        } catch (e: Exception) {
            println("❌ Error en getOccupancyByLote: ${e.message}")
            emptyList()
        }
    }

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

    // --- ESCRITURA (USANDO CONVERSIÓN MANUAL) ---

    override suspend fun addComanda(comanda: Comanda): Boolean = withContext(Dispatchers.IO) {
        try {
            val nuevoNumero = obtenerSiguienteNumeroDeComanda()
            val dateTs = comanda.dateBookedComanda?.let { Timestamp(it.epochSeconds, it.nanosecondsOfSecond) }

            val firstAsignacion = comanda.listaAsignaciones.firstOrNull()

            val comandaMap = mutableMapOf(
                "numeroDeComanda" to nuevoNumero,
                "userEmailComanda" to comanda.userEmailComanda,
                "dateBookedComanda" to dateTs,
                "bookedClientComanda" to comanda.bookedClientComanda?.toMap(),
                "remarkComanda" to comanda.remarkComanda,
                "fueVendidoComanda" to comanda.fueVendidoComanda,
                "listaAsignaciones" to comanda.listaAsignaciones.map { it.toMap() },
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
            // Arreglo para iOS: pasar asignacion como Map
            collection.document(comandaId).update(
                "listaAsignaciones" to FieldValue.arrayUnion(asignacion.toMap())
            )
            true
        } catch (e: Exception) { false }
    }

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

                val indice = listaActual.indexOfFirst {
                    it.materialNombre == antigua.materialNombre && it.numeroLote.isBlank()
                }

                if (indice != -1) {
                    listaActual[indice] = nueva
                } else {
                    listaActual.add(nueva)
                }

                update(docRef,
                    "listaAsignaciones" to listaActual.map { it.toMap() }, // Convertir lista completa
                    "userEmailComanda" to nueva.userAsignacion,
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

                val indice = listaActual.indexOfFirst { it.numeroLote == loteNumber }

                if (indice != -1) {
                    val asignacionLimpia = listaActual[indice].copy(
                        idLote = "",
                        numeroLote = "",
                        cantidadBB = 0,
                        userAsignacion = "",
                        fueVendido = false
                    )
                    listaActual[indice] = asignacionLimpia

                    update(docRef,
                        "listaAsignaciones" to listaActual.map { it.toMap() }, // Convertir lista completa
                        "numberLoteComanda" to ""
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
                "bookedClientComanda" to cliente?.toMap(), // Usar toMap()
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