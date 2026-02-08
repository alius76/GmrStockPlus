package com.alius.gmrstockplus.data

import com.alius.gmrstockplus.data.firestore.FirebaseClient
import com.alius.gmrstockplus.domain.model.*
import dev.gitlive.firebase.firestore.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.withContext
import kotlinx.datetime.*

class ComandaRepositoryImpl(private val plantName: String) : ComandaRepository {

    // Centralización de la base de datos según la planta
    private val firestore by lazy {
        if (plantName == "P08") FirebaseClient.db08 else FirebaseClient.db07
    }

    private val collection by lazy { firestore.collection("comanda") }
    private val metadataCollection by lazy { firestore.collection("metadata") }

    // --- MAPEO SEGURO (ID + TIMESTAMPS) ---
    private fun DocumentSnapshot.toComandaSafe(): Comanda? {
        return try {
            val comanda = this.data<Comanda>()
            // Extraemos el Timestamp nativo de Firestore para asegurar la precisión del Instant
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

    // --- 1. CONTADOR (Transacción) ---
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
            println("❌ Error en contador comanda: ${e.message}")
            1L
        }
    }

    // --- 2. QUERIES BLINDADAS ---

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
        } catch (e: Exception) {
            println("❌ Error listarComandas: ${e.message}")
            emptyList()
        }
    }

    override suspend fun listarTodasComandas(): List<Comanda> = withContext(Dispatchers.IO) {
        try {
            val now = Clock.System.now()
            val today = now.toLocalDateTime(TimeZone.currentSystemDefault()).date
            val monthAgo = today.minus(DatePeriod(months = 1))
            val firstDay = LocalDate(monthAgo.year, monthAgo.monthNumber, 1).atStartOfDayIn(TimeZone.UTC)

            val startTs = Timestamp(firstDay.epochSeconds, firstDay.nanosecondsOfSecond)

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
            collection
                .where { "numeroDeComanda" equalTo numLong }
                .get()
                .documents
                .firstOrNull()?.toComandaSafe()
        } catch (e: Exception) { null }
    }

    override suspend fun getComandaByLoteNumber(loteNumber: String): Comanda? = withContext(Dispatchers.IO) {
        if (loteNumber.isBlank()) return@withContext null
        try {
            collection
                .where { "numberLoteComanda" equalTo loteNumber }
                .get()
                .documents
                .firstOrNull()?.toComandaSafe()
        } catch (e: Exception) { null }
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

    // --- 3. CRUD CON CONVERSIÓN EXPLÍCITA A TIMESTAMP ---

    override suspend fun addComanda(comanda: Comanda): Boolean = withContext(Dispatchers.IO) {
        try {
            val nuevoNumero = obtenerSiguienteNumeroDeComanda()

            // Convertimos explícitamente a Timestamp nativo para que Firebase lo reconozca
            val dateTs = comanda.dateBookedComanda?.let {
                Timestamp(it.epochSeconds, it.nanosecondsOfSecond)
            }

            // Usamos un mapa para evitar que el serializador de Kotlin convierta el Instant en un Long
            val comandaMap = mutableMapOf<String, Any?>(
                "numeroDeComanda" to nuevoNumero,
                "numberLoteComanda" to comanda.numberLoteComanda,
                "descriptionLoteComanda" to comanda.descriptionLoteComanda,
                "dateBookedComanda" to dateTs, // <--- Esto genera el formato visual correcto en la DB
                "totalWeightComanda" to comanda.totalWeightComanda,
                "bookedClientComanda" to comanda.bookedClientComanda,
                "remarkComanda" to comanda.remarkComanda,
                "fueVendidoComanda" to comanda.fueVendidoComanda
            )

            collection.add(comandaMap)
            true
        } catch (e: Exception) {
            println("❌ Error en addComanda: ${e.message}")
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
            // Conversión a Timestamp para mantener el formato en la DB
            val dateTs = Timestamp(dateBooked.epochSeconds, dateBooked.nanosecondsOfSecond)
            collection.document(comandaId).update("dateBookedComanda" to dateTs)
            true
        } catch (e: Exception) { false }
    }

    override suspend fun updateComandaBooked(
        comandaId: String,
        cliente: Cliente?,
        dateBooked: Instant?,
        bookedRemark: String?
    ): Boolean = withContext(Dispatchers.IO) {
        try {
            // Conversión a Timestamp para mantener el formato en la DB
            val dateTs = dateBooked?.let { Timestamp(it.epochSeconds, it.nanosecondsOfSecond) }
            collection.document(comandaId).update(
                "bookedClientComanda" to cliente,
                "dateBookedComanda" to dateTs,
                "remarkComanda" to (bookedRemark ?: "")
            )
            true
        } catch (e: Exception) { false }
    }

    override suspend fun deleteComanda(comandaId: String): Boolean = withContext(Dispatchers.IO) {
        try {
            collection.document(comandaId).delete()
            true
        } catch (e: Exception) { false }
    }

    override suspend fun updateComandaLoteNumber(comandaId: String, loteNumber: String): Boolean = withContext(Dispatchers.IO) {
        try {
            collection.document(comandaId).update("numberLoteComanda" to loteNumber)
            true
        } catch (e: Exception) { false }
    }
}