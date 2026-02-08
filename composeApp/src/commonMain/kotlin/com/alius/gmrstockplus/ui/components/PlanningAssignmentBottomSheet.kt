package com.alius.gmrstockplus.ui.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.pager.VerticalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.coerceAtLeast
import androidx.compose.ui.util.lerp
import com.alius.gmrstockplus.data.ClientRepository
import com.alius.gmrstockplus.data.getCertificadoRepository
import com.alius.gmrstockplus.data.getComandaRepository
import com.alius.gmrstockplus.data.getLoteRepository
import com.alius.gmrstockplus.domain.model.*
import com.alius.gmrstockplus.ui.theme.PrimaryColor
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlin.math.abs

@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun PlanningAssignmentBottomSheet(
    selectedComanda: Comanda,
    plantId: String, // Único cambio: databaseUrl -> plantId
    currentUserEmail: String,
    clientRepository: ClientRepository,
    onLoteAssignmentSuccess: () -> Unit,
    snackbarHostState: SnackbarHostState
) {
    val scope = rememberCoroutineScope()
    // Repositorios inicializados con plantId
    val loteRepository = remember(plantId) { getLoteRepository(plantId) }
    val comandaRepository = remember(plantId) { getComandaRepository(plantId) }
    val certificadoRepository = remember(plantId) { getCertificadoRepository(plantId) }

    val materialDescription = remember { selectedComanda.descriptionLoteComanda }
    val comandaClientName = remember { selectedComanda.bookedClientComanda?.cliNombre ?: "Cliente Desconocido" }
    val density = LocalDensity.current

    var lotesDisponibles by remember { mutableStateOf<List<LoteModel>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }
    var certificados by remember { mutableStateOf<Map<String, Certificado?>>(emptyMap()) }

    val pagerBoxHeightDp = 420.dp

    // Función de carga (Mantiene toda tu lógica de filtrado de lotes ocupados)
    val loadLotesForAssignment: () -> Unit = {
        scope.launch {
            isLoading = true
            try {
                val loadedLotes = loteRepository.listarLotesPorDescripcion(materialDescription)
                val todasLasComandas = comandaRepository.listarTodasComandas()

                val lotesOcupadosEnOtrasComandas = todasLasComandas
                    .filter {
                        it.idComanda != selectedComanda.idComanda &&
                                it.numberLoteComanda.isNotBlank() &&
                                !it.fueVendidoComanda
                    }
                    .map { it.numberLoteComanda }
                    .toSet()

                var filteredLotes = loadedLotes
                    .filter { lote ->
                        val isDescriptionMatch = lote.description.equals(materialDescription, ignoreCase = true)
                        val bookedClient = lote.booked?.cliNombre
                        val estaOcupadoPorOtraComanda = lotesOcupadosEnOtrasComandas.contains(lote.number)

                        if (selectedComanda.numberLoteComanda.isNotBlank()) {
                            lote.number == selectedComanda.numberLoteComanda
                        } else {
                            val isAvailableOrReservedByMe = (lote.booked == null || bookedClient == comandaClientName)
                            isDescriptionMatch && isAvailableOrReservedByMe && !estaOcupadoPorOtraComanda
                        }
                    }

                filteredLotes = filteredLotes.sortedBy { it.number }
                lotesDisponibles = filteredLotes
                certificados = lotesDisponibles.associate { it.number to certificadoRepository.getCertificadoByLoteNumber(it.number) }

            } catch (e: Exception) {
                if (e !is CancellationException) {
                    scope.launch { snackbarHostState.showSnackbar("Error cargando lotes: ${e.message}") }
                }
            } finally {
                if (scope.isActive) isLoading = false
            }
        }
    }

    // Lógica de asignación/anulación centralizada (Intacta)
    val assignLoteToComanda: (LoteModel, Boolean) -> Unit = { loteToProcess, shouldClearBooking ->
        scope.launch {
            val cliente = selectedComanda.bookedClientComanda
            val comandaId = selectedComanda.idComanda
            val loteNumber = loteToProcess.number
            val isAssignedToThisComanda = loteToProcess.number == selectedComanda.numberLoteComanda

            if (cliente == null) return@launch

            if (isAssignedToThisComanda) {
                val comandaSuccess = comandaRepository.updateComandaLoteNumber(comandaId, "")
                var bookingCleared = true
                var message = ""

                if (shouldClearBooking) {
                    bookingCleared = loteRepository.updateLoteBooked(loteToProcess.id, null, null, null, null)
                    message = if (bookingCleared) "Lote $loteNumber desasignado y reserva anulada."
                    else "Error al anular reserva."
                } else {
                    message = "Lote $loteNumber desasignado (Reserva mantenida)."
                }

                if (comandaSuccess && bookingCleared) {
                    loadLotesForAssignment()
                    onLoteAssignmentSuccess()
                    snackbarHostState.showSnackbar(message)
                } else {
                    snackbarHostState.showSnackbar("Error al desasignar el lote $loteNumber.")
                }
                return@launch
            }

            if (selectedComanda.numberLoteComanda.isNotBlank()) return@launch

            val loteSuccess = loteRepository.updateLoteBooked(
                loteToProcess.id,
                cliente,
                selectedComanda.dateBookedComanda,
                currentUserEmail,
                null
            )
            val comandaSuccess = comandaRepository.updateComandaLoteNumber(comandaId, loteNumber)

            if (loteSuccess && comandaSuccess) {
                loadLotesForAssignment()
                onLoteAssignmentSuccess()
                snackbarHostState.showSnackbar("Lote $loteNumber asignado a la comanda con éxito.")
            } else {
                snackbarHostState.showSnackbar("Error al asignar lote o comanda.")
            }
        }
    }

    LaunchedEffect(materialDescription, plantId) {
        loadLotesForAssignment()
    }

    val pagerState = rememberPagerState(initialPage = 0) { lotesDisponibles.size }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 14.dp)
            .navigationBarsPadding(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "Asignar lote a comanda",
            style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold),
            color = PrimaryColor
        )
        Text(
            text = "Material: $materialDescription",
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(
            text = "Cliente: ${selectedComanda.bookedClientComanda?.cliNombre}",
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Spacer(modifier = Modifier.height(16.dp))

        Box(
            modifier = Modifier.fillMaxWidth().height(pagerBoxHeightDp),
            contentAlignment = Alignment.Center
        ) {
            if (isLoading) {
                CircularProgressIndicator(color = PrimaryColor)
            } else if (lotesDisponibles.isEmpty()) {
                Text("No hay lotes disponibles para este material.")
            } else {
                VerticalPager(
                    state = pagerState,
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(vertical = 60.dp)
                ) { index ->
                    val lote = lotesDisponibles[index]
                    val certificado = certificados[lote.number]
                    val pageOffset = (pagerState.currentPage - index + pagerState.currentPageOffsetFraction)

                    val scale by animateFloatAsState(targetValue = lerp(0.85f, 1f, 1f - abs(pageOffset)))
                    val alpha by animateFloatAsState(targetValue = lerp(0.55f, 1f, 1f - abs(pageOffset)))
                    val translation by animateFloatAsState(targetValue = pageOffset * 40f)

                    Box(
                        modifier = Modifier.fillMaxWidth().graphicsLayer {
                            scaleX = scale; scaleY = scale; this.alpha = alpha; translationY = translation
                        },
                        contentAlignment = Alignment.Center
                    ) {
                        PlanningLoteCard(
                            lote = lote,
                            comanda = selectedComanda,
                            certificado = certificado,
                            snackbarHostState = snackbarHostState,
                            onAssignLote = assignLoteToComanda,
                            onViewBigBags = { },
                            modifier = Modifier.fillMaxWidth(0.85f)
                        )
                    }
                }

                // Barra de progreso vertical (Misma lógica original)
                val totalItems = lotesDisponibles.size
                if (totalItems > 1) {
                    val barWidth = 4.dp
                    val thumbHeight = (pagerBoxHeightDp / totalItems.toFloat()).coerceAtLeast(20.dp)
                    val currentPosition = pagerState.currentPage + pagerState.currentPageOffsetFraction
                    val normalizedPosition = currentPosition / (totalItems - 1).toFloat()
                    val travelRangePx = with(density) { (pagerBoxHeightDp - thumbHeight).toPx() }
                    val thumbOffsetPx by animateFloatAsState(targetValue = normalizedPosition * travelRangePx)

                    Box(
                        modifier = Modifier.fillMaxHeight().width(barWidth).align(Alignment.CenterEnd)
                            .padding(vertical = 10.dp).clip(CircleShape)
                            .background(MaterialTheme.colorScheme.onSurface.copy(alpha = 0.1f))
                    ) {
                        Box(
                            modifier = Modifier.offset(y = with(density) { thumbOffsetPx.toDp() })
                                .width(barWidth).height(thumbHeight).clip(CircleShape).background(PrimaryColor)
                        )
                    }
                }
            }
        }
        Spacer(modifier = Modifier.height(10.dp))
    }
}