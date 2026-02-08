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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.util.lerp
import androidx.compose.ui.text.font.FontWeight
import com.alius.gmrstockplus.data.ClientRepository
import com.alius.gmrstockplus.data.getCertificadoRepository
import com.alius.gmrstockplus.data.getLoteRepository
import com.alius.gmrstockplus.domain.model.BigBags
import com.alius.gmrstockplus.domain.model.Certificado
import com.alius.gmrstockplus.domain.model.CertificadoStatus
import com.alius.gmrstockplus.domain.model.LoteModel
import com.alius.gmrstockplus.ui.theme.PrimaryColor
import kotlinx.coroutines.launch
import kotlin.math.abs
import androidx.compose.ui.platform.LocalDensity

@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun GroupMaterialBottomSheetContent(
    loteNumbers: List<String>,
    onLoteClick: (LoteModel) -> Unit,
    onDismissRequest: () -> Unit,
    snackbarHostState: SnackbarHostState,
    onViewBigBags: (List<BigBags>) -> Unit,
    databaseUrl: String,
    onRemarkUpdated: (LoteModel) -> Unit,
    clientRepository: ClientRepository,
    currentUserEmail: String
) {
    val scope = rememberCoroutineScope()
    val loteRepository = remember { getLoteRepository(databaseUrl) }
    val certificadoRepository = remember { getCertificadoRepository(databaseUrl) }
    val density = LocalDensity.current

    var lotes by remember { mutableStateOf<List<LoteModel>>(emptyList()) }
    var certificados by remember { mutableStateOf<Map<String, Certificado?>>(emptyMap()) }
    var isLoading by remember { mutableStateOf(true) }

    // Carga inicial
    LaunchedEffect(loteNumbers) {
        isLoading = true
        try {
            val loadedLotes = loteNumbers.mapNotNull { loteRepository.getLoteByNumber(it) }
            lotes = loadedLotes
            certificados = loadedLotes.associate { it.number to certificadoRepository.getCertificadoByLoteNumber(it.number) }
        } catch (e: Exception) {
            scope.launch { snackbarHostState.showSnackbar("Error cargando lotes: ${e.message}") }
        } finally {
            isLoading = false
        }
    }

    if (isLoading) {
        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator(color = PrimaryColor)
        }
        return
    }

    if (lotes.isEmpty()) {
        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text(
                text = "No hay lotes disponibles para este material.",
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        return
    }

    val pagerState = rememberPagerState(initialPage = 0) { lotes.size }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 14.dp)
            .navigationBarsPadding(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "Lotes disponibles",
            style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.Bold),
            color = PrimaryColor
        )

        Spacer(modifier = Modifier.height(16.dp))

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(420.dp),
            contentAlignment = Alignment.Center
        ) {
            VerticalPager(
                state = pagerState,
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(vertical = 60.dp)
            ) { index ->
                val lote = lotes[index]
                val cert = certificados[lote.number]
                val certColor = when (cert?.status) {
                    CertificadoStatus.ADVERTENCIA -> MaterialTheme.colorScheme.error
                    CertificadoStatus.CORRECTO -> PrimaryColor
                    else -> MaterialTheme.colorScheme.onSurfaceVariant
                }

                val pageOffset = pagerState.currentPage - index + pagerState.currentPageOffsetFraction

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
                        .graphicsLayer {
                            scaleX = scale
                            scaleY = scale
                            this.alpha = alpha
                            translationY = translation
                        },
                    contentAlignment = Alignment.Center
                ) {
                    LoteCard(
                        lote = lote,
                        certificado = cert,
                        certificadoIconColor = certColor,
                        modifier = Modifier.fillMaxWidth(0.85f),
                        scope = scope,
                        snackbarHostState = snackbarHostState,
                        onViewBigBags = onViewBigBags,
                        databaseUrl = databaseUrl,
                        onRemarkUpdated = { updatedLote ->
                            lotes = lotes.map { if (it.id == updatedLote.id) updatedLote else it }
                            onRemarkUpdated(updatedLote)
                        },
                        clientRepository = clientRepository,
                        currentUserEmail = currentUserEmail
                    )
                }
            }

            // --- BARRA VERTICAL DE PROGRESO CORREGIDA ---
            if (lotes.size > 1) {
                val barWidth = 4.dp
                val indicatorHeightDp = 420.dp
                val minThumbHeight = 20.dp

                val thumbHeight = (indicatorHeightDp / lotes.size.toFloat()).coerceAtLeast(minThumbHeight)
                val currentPosition = pagerState.currentPage + pagerState.currentPageOffsetFraction
                val normalizedPosition = currentPosition / (lotes.size - 1).toFloat()
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
                        .background(MaterialTheme.colorScheme.onSurface.copy(alpha = 0.1f))
                        .clip(CircleShape)
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

        Spacer(modifier = Modifier.height(10.dp))
    }
}