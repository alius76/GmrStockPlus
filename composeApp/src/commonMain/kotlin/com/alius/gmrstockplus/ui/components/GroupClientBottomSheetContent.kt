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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.util.lerp
import androidx.compose.ui.text.font.FontWeight
import com.alius.gmrstockplus.data.getCertificadoRepository
import com.alius.gmrstockplus.domain.model.Certificado
import com.alius.gmrstockplus.domain.model.Cliente
import com.alius.gmrstockplus.domain.model.Venta
import com.alius.gmrstockplus.ui.theme.PrimaryColor
import kotlin.math.abs

@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun GroupClientBottomSheetContent(
    cliente: Cliente,
    ventas: List<Venta>,
    plantId: String,             // 🔑 Cambiado: databaseUrl -> plantId
    onDismissRequest: () -> Unit
) {
    // Inicializamos el repositorio con el ID de la planta (usando el SDK)
    val certificadoRepository = remember(plantId) { getCertificadoRepository(plantId) }

    var certificados by remember { mutableStateOf<Map<String, Certificado?>>(emptyMap()) }
    var loading by remember { mutableStateOf(true) }

    // --- CARGA DE CERTIFICADOS ---
    LaunchedEffect(ventas, plantId) {
        loading = true
        // Optimizamos la carga para obtener los certificados de los lotes vendidos
        certificados = ventas.associate { venta ->
            venta.ventaLote to certificadoRepository.getCertificadoByLoteNumber(venta.ventaLote)
        }
        loading = false
    }

    if (loading) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(420.dp),
            contentAlignment = Alignment.Center
        ) {
            CircularProgressIndicator(color = PrimaryColor)
        }
    } else if (ventas.isEmpty()) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(420.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "No hay ventas disponibles para este cliente.",
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    } else {
        val pagerState = rememberPagerState(initialPage = 0) { ventas.size }
        val density = LocalDensity.current

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 14.dp)
                .navigationBarsPadding(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = "Lotes vendidos",
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
                    val venta = ventas[index]
                    val certificado = certificados[venta.ventaLote]

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
                            .graphicsLayer {
                                scaleX = scale
                                scaleY = scale
                                this.alpha = alpha
                                translationY = translation
                            },
                        contentAlignment = Alignment.Center
                    ) {
                        // 🔑 Actualizado: Pasamos plantId a ClientCard
                        ClientCard(
                            cliente = cliente,
                            venta = venta,
                            certificado = certificado,
                            plantId = plantId,
                            modifier = Modifier.fillMaxWidth(0.85f)
                        )
                    }
                }

                // Barra vertical de progreso (Scroll indicator)
                val totalItems = ventas.size
                if (totalItems > 1) {
                    val barWidth = 4.dp
                    val indicatorHeightDp = 420.dp
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
            Spacer(modifier = Modifier.height(10.dp))
        }
    }
}