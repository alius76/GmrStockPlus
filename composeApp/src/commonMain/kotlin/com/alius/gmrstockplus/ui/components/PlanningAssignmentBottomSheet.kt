package com.alius.gmrstockplus.ui.components

import androidx.compose.animation.core.animateFloatAsState
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.coerceAtLeast
import androidx.compose.ui.util.lerp
import com.alius.gmrstockplus.core.utils.formatInstant
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
    plantId: String,
    currentUserEmail: String,
    clientRepository: ClientRepository,
    onLoteAssignmentSuccess: () -> Unit,
    snackbarHostState: SnackbarHostState
) {
    val scope = rememberCoroutineScope()
    val loteRepository = remember(plantId) { getLoteRepository(plantId) }
    val comandaRepository = remember(plantId) { getComandaRepository(plantId) }
    val certificadoRepository = remember(plantId) { getCertificadoRepository(plantId) }

    val materialDescription = remember(selectedComanda) { selectedComanda.descriptionLoteComanda }
    val comandaClientName = remember(selectedComanda) { selectedComanda.bookedClientComanda?.cliNombre ?: "Cliente Desconocido" }
    val isComandaVendida = selectedComanda.fueVendidoComanda
    val density = LocalDensity.current

    var lotesDisponibles by remember { mutableStateOf<List<LoteModel>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }
    var certificados by remember { mutableStateOf<Map<String, Certificado?>>(emptyMap()) }
    var occupancyDetails by remember { mutableStateOf<Map<String, List<OccupancyInfo>>>(emptyMap()) }

    val pagerBoxHeightDp = 420.dp

    val loadLotesForAssignment: () -> Unit = {
        scope.launch {
            isLoading = true
            try {
                val loadedLotes = loteRepository.listarLotesPorDescripcion(materialDescription)
                val todasLasComandas = comandaRepository.listarTodasComandas()

                // 1. Calculamos la ocupación real:
                // Solo cuentan las asignaciones que NO han sido vendidas individualmente
                occupancyDetails = todasLasComandas
                    .filter { !it.fueVendidoComanda } // Comandas activas
                    .flatMap { comanda ->
                        comanda.listaAsignaciones
                            .filter { !it.fueVendido && it.numeroLote.isNotBlank() } // SOLO LOTES NO VENDIDOS
                            .map { asig ->
                                asig.numeroLote to OccupancyInfo(
                                    cliente = comanda.bookedClientComanda?.cliNombre ?: "Sin Nombre",
                                    cantidad = asig.cantidadBB,
                                    numeroComanda = comanda.numeroDeComanda.toString(),
                                    fecha = formatInstant(comanda.dateBookedComanda).ifEmpty { "--/--/--" },
                                    // Trazabilidad: Usamos el usuario que asignó el lote si existe
                                    usuario = asig.userAsignacion.ifEmpty { comanda.userEmailComanda }.split("@").first()
                                )
                            }
                    }
                    .groupBy({ it.first }, { it.second })

                // 2. Filtramos los lotes según su disponibilidad real (Total - Ocupado no vendido)
                val filteredLotes = loadedLotes.filter { lote ->
                    val isDescriptionMatch = lote.description.equals(materialDescription, ignoreCase = true)
                    val bookedClient = lote.booked?.cliNombre

                    // Sumamos lo ocupado por comandas activas que aún no han pasado por báscula
                    val totalOcupadoActivo = occupancyDetails[lote.number]?.sumOf { it.cantidad } ?: 0
                    val totalLote = lote.count.toIntOrNull() ?: 0
                    val stockDisponible = totalLote - totalOcupadoActivo

                    // Un lote es válido si:
                    // a) Ya está en esta comanda (para poder desasignarlo o verlo)
                    // b) Tiene stock y el cliente coincide o no tiene reserva previa
                    val yaEstaEnEstaComanda = selectedComanda.listaAsignaciones.any { it.numeroLote == lote.number }
                    val tieneStock = stockDisponible > 0
                    val esClienteCorrecto = (lote.booked == null || bookedClient == comandaClientName)

                    isDescriptionMatch && esClienteCorrecto && (tieneStock || yaEstaEnEstaComanda)
                }

                lotesDisponibles = filteredLotes.sortedBy { it.number }
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

    val assignLoteToComanda: (LoteModel, Boolean, Int) -> Unit = { loteToProcess, shouldClearBooking, cantidad ->
        if (isComandaVendida) {
            scope.launch { snackbarHostState.showSnackbar("No se puede modificar una comanda vendida.") }
        } else {
            scope.launch {
                val comandaId = selectedComanda.idComanda
                val loteNumber = loteToProcess.number

                // 1. Verificar si el lote ya está asignado (para desasignar)
                val asignacionExistente = selectedComanda.listaAsignaciones.find { it.numeroLote == loteNumber }

                if (asignacionExistente != null) {
                    val quitSuccess = comandaRepository.quitarAsignacionLote(comandaId, loteNumber)
                    if (quitSuccess) {
                        if (shouldClearBooking) loteRepository.updateLoteBooked(loteToProcess.id, null, null, null, null)
                        loadLotesForAssignment()
                        onLoteAssignmentSuccess()
                        snackbarHostState.showSnackbar("Lote $loteNumber desasignado.")
                    }
                } else {
                    // --- ASIGNACIÓN INTELIGENTE ---

                    // 2. Buscamos si existe un registro PENDIENTE (idLote vacío o numeroLote vacío) para este material
                    val huecoPendiente = selectedComanda.listaAsignaciones.find {
                        it.materialNombre.equals(loteToProcess.description, ignoreCase = true) &&
                                it.numeroLote.isBlank()
                    }

                    val nuevaAsignacion = AsignacionLote(
                        idLote = loteToProcess.id,
                        numeroLote = loteNumber,
                        cantidadBB = cantidad,
                        materialNombre = loteToProcess.description,
                        userAsignacion = currentUserEmail,
                        fueVendido = false
                    )

                    val asignacionSuccess = if (huecoPendiente != null) {
                        // CASO ACTUALIZAR: Existe la línea de material pero no tenía lote
                        // Aquí usamos una función del repo que sustituya o use el índice/ID de la pendiente
                        comandaRepository.actualizarAsignacionLote(comandaId, huecoPendiente, nuevaAsignacion)
                    } else {
                        // CASO AGREGAR: Es un material nuevo o ya se llenaron los huecos anteriores
                        comandaRepository.agregarAsignacionLote(comandaId, nuevaAsignacion)
                    }

                    val comandaSuccess = comandaRepository.updateComandaLoteNumber(comandaId, loteNumber)
                    val userUpdated = comandaRepository.updateComandaUser(comandaId, currentUserEmail)

                    if (comandaSuccess && userUpdated && asignacionSuccess) {
                        loadLotesForAssignment()
                        onLoteAssignmentSuccess()
                        snackbarHostState.showSnackbar("Lote $loteNumber asignado correctamente.")
                    } else {
                        snackbarHostState.showSnackbar("Error al procesar la asignación.")
                    }
                }
            }
        }
    }

    LaunchedEffect(materialDescription, plantId, selectedComanda.fueVendidoComanda) {
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
            text = if (isComandaVendida) "Consulta de Lotes (Vendida)" else "Asignar lote a comanda",
            style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold),
            color = if (isComandaVendida) Color.Gray else PrimaryColor
        )
        Text(
            text = "Material: $materialDescription",
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(
            text = "Cliente: $comandaClientName",
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
                            occupancyList = occupancyDetails[lote.number] ?: emptyList(),
                            currentUserEmail = currentUserEmail,
                            snackbarHostState = snackbarHostState,
                            onAssignLote = { loteTarget, clear, cant ->
                                assignLoteToComanda(loteTarget, clear, cant)
                            },
                            onViewBigBags = { },
                            modifier = Modifier.fillMaxWidth(0.85f)
                        )
                    }
                }

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