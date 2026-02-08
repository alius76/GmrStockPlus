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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.coerceAtLeast
import androidx.compose.ui.util.lerp
import com.alius.gmrstockplus.data.ComandaRepository
import com.alius.gmrstockplus.data.getComandaRepository
import com.alius.gmrstockplus.domain.model.Comanda
import com.alius.gmrstockplus.ui.theme.PrimaryColor
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.datetime.Clock
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import kotlinx.datetime.LocalDate
import kotlin.math.abs

@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun SalidasProgramadasBottomSheet(
    databaseUrl: String,
    snackbarHostState: SnackbarHostState,
    // Dejo onComandaClick para mantener la API externa, aunque internamente solo gestionemos la selección visual.
    onComandaClick: (Comanda) -> Unit
) {
    val scope = rememberCoroutineScope()
    // Inicialización del repositorio de Comanda
    val comandaRepository: ComandaRepository = remember { getComandaRepository(databaseUrl) }

    var comandasHoy by remember { mutableStateOf<List<Comanda>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }

    // 🆕 ESTADO DE SELECCIÓN: Guarda la ID de la comanda actualmente seleccionada visualmente
    var selectedComandaId by remember { mutableStateOf<String?>(null) }


    val density = LocalDensity.current
    val pagerBoxHeightDp = 420.dp

    // --- CÁLCULO Y FORMATO DE FECHA ADAPTADO A TU UTILS ---
    val today: LocalDate = remember {
        // Obtenemos la fecha de hoy en la zona horaria del sistema
        Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault()).date
    }

    // 1. Formato para la Interfaz (dd/MM/yyyy)
    val todayDateStringUI = remember {
        val d = today.dayOfMonth.toString().padStart(2, '0')
        val m = today.monthNumber.toString().padStart(2, '0')
        val y = today.year
        "$d/$m/$y"
    }

    // 2. Formato para la Query (YYYY-MM-DD)
    val todayDateFilter = remember {
        val d = today.dayOfMonth.toString().padStart(2, '0')
        val m = today.monthNumber.toString().padStart(2, '0')
        val y = today.year
        // Requerido por tu buildQueryPorFecha: YYYY-MM-DD
        "$y-$m-$d"
    }

    // Función de carga adaptada
    val loadComandasForToday: () -> Unit = {
        scope.launch {
            isLoading = true
            try {
                // Usamos listarComandas con el filtro de fecha (YYYY-MM-DD)
                val loadedComandas = comandaRepository.listarComandas(todayDateFilter)

                // Ordenamos por la marca de tiempo de reserva (Instant)
                comandasHoy = loadedComandas.sortedBy { it.dateBookedComanda }

            } catch (e: Exception) {
                if (e is CancellationException) {
                    // Manejo silencioso de la cancelación
                } else {
                    scope.launch {
                        snackbarHostState.showSnackbar("Error cargando comandas: ${e.message}")
                    }
                }
            } finally {
                if (scope.isActive) {
                    isLoading = false
                }
            }
        }
    }

    LaunchedEffect(Unit) {
        loadComandasForToday()
    }

    val pagerState = rememberPagerState(initialPage = 0) { comandasHoy.size }

    // --- UI ---
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 14.dp)
            .navigationBarsPadding(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {

        Text(
            text = "Salidas programadas",
            style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold),
            color = PrimaryColor
        )
        Text(
            text = "Confirmadas para hoy: $todayDateStringUI",
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.secondary
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Contenedor de Altura Fija para Pager/Carga/Vacío
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(pagerBoxHeightDp),
            contentAlignment = Alignment.Center
        ) {

            if (isLoading) {
                CircularProgressIndicator(color = PrimaryColor)
            } else if (comandasHoy.isEmpty()) {
                Text(
                    text = "No hay salidas programadas para hoy.",
                    modifier = Modifier.padding(16.dp),
                    color = Color.Gray
                )
            } else {
                // --- 1. VERTICAL PAGER (El Carrusel) ---
                VerticalPager(
                    state = pagerState,
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(vertical = 60.dp)
                ) { index ->
                    val comanda = comandasHoy[index]

                    // 🆕 Determinar si esta tarjeta está seleccionada
                    val isSelected = comanda.idComanda == selectedComandaId

                    // Cálculo del Page Offset para las animaciones (mismo efecto 3D)
                    val pageOffset = (pagerState.currentPage - index + pagerState.currentPageOffsetFraction)

                    val scale by animateFloatAsState(
                        targetValue = lerp(0.85f, 1f, 1f - abs(pageOffset)),
                        animationSpec = tween(300)
                    )

                    val alpha by animateFloatAsState(
                        targetValue = lerp(0.55f, 1f, 1f - abs(pageOffset)),
                        animationSpec = tween(300)
                    )

                    val translation by animateFloatAsState(
                        targetValue = pageOffset * 40f,
                        animationSpec = tween(300)
                    )

                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .graphicsLayer { // Aplicar las animaciones 3D
                                scaleX = scale
                                scaleY = scale
                                this.alpha = alpha
                                translationY = translation
                            },
                        contentAlignment = Alignment.Center
                    ) {
                        // 🆕 Pasar el estado de selección y la función onClick
                        SalidasProgramadasComandaCard(
                            comanda = comanda,
                            onClick = {
                                // Alterna el estado de selección
                                selectedComandaId = if (isSelected) null else comanda.idComanda

                                // Llama a la acción externa (si la hubiera, aunque el requisito es solo visual)
                                // onComandaClick(comanda)
                            },
                            isSelected = isSelected, // Pasar el estado
                            modifier = Modifier.fillMaxWidth(0.85f)
                        )
                    }
                }

                // --- 2. BARRA VERTICAL DE PROGRESO (Idéntica al ejemplo) ---
                val totalItems = comandasHoy.size
                if (totalItems > 1) {
                    val barWidth = 4.dp
                    val indicatorHeightDp = pagerBoxHeightDp
                    val minThumbHeight = 20.dp

                    val thumbHeight = (indicatorHeightDp / totalItems.toFloat()).coerceAtLeast(minThumbHeight)
                    val currentPosition = pagerState.currentPage + pagerState.currentPageOffsetFraction
                    val normalizedPosition = currentPosition / (totalItems - 1).toFloat()
                    val travelRangePx = with(density) { (indicatorHeightDp - thumbHeight).toPx() }

                    val thumbOffsetPx by animateFloatAsState(
                        targetValue = normalizedPosition * travelRangePx,
                        animationSpec = tween(300)
                    )

                    Box(
                        modifier = Modifier
                            .fillMaxHeight()
                            .width(barWidth)
                            .align(Alignment.CenterEnd)
                            .padding(vertical = 10.dp)
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.onSurface.copy(alpha = 0.1f))
                    ) {
                        Box(
                            modifier = Modifier
                                .offset(y = with(density) { thumbOffsetPx.toDp() })
                                .width(barWidth)
                                .height(thumbHeight)
                                .clip(CircleShape)
                                .background(PrimaryColor)
                        )
                    }
                }
            }
        }
        Spacer(modifier = Modifier.height(10.dp))
    }
}