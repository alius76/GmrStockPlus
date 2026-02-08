package com.alius.gmrstockplus.ui.components

import androidx.compose.animation.*
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.TransformOrigin
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.alius.gmrstockplus.core.utils.formatWeight
import com.alius.gmrstockplus.domain.model.Ratio
import com.alius.gmrstockplus.presentation.screens.ProduccionMensual
import com.alius.gmrstockplus.ui.theme.PrimaryColor
import kotlinx.datetime.*

@Composable
fun ExpandableProduccionCard(
    mensual: ProduccionMensual,
    porcentaje: Float,
    loteNombresMap: Map<String, String>
) {
    var expanded by remember { mutableStateOf(false) }

    Column(modifier = Modifier.fillMaxWidth()) {
        // --- CABECERA (La parte que hace el toggle) ---
        ProduccionMensualHeader(
            mensual = mensual,
            porcentaje = porcentaje,
            isExpanded = expanded,
            onExpandClick = { expanded = !expanded }
        )

        // --- CONTENIDO DESPLEGABLE (Igual que en Ventas) ---
        AnimatedVisibility(
            visible = expanded,
            enter = expandVertically() + fadeIn(),
            exit = shrinkVertically() + fadeOut()
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 8.dp)
                    // Fondo sutil para agrupar visualmente (Estilo Ventas)
                    .background(
                        Color.Black.copy(alpha = 0.03f),
                        RoundedCornerShape(bottomStart = 16.dp, bottomEnd = 16.dp)
                    )
                    .padding(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                mensual.listaRatios.forEach { ratio ->
                    val nombreLoteReal = loteNombresMap[ratio.ratioLoteId]
                    RegistroProduccionItem(
                        ratio = ratio,
                        loteNombre = nombreLoteReal
                    )
                }
            }
        }
    }
}

@Composable
private fun ProduccionMensualHeader(
    mensual: ProduccionMensual,
    porcentaje: Float,
    isExpanded: Boolean,
    onExpandClick: () -> Unit
) {
    var targetProgress by remember { mutableStateOf(0f) }
    val animatedProgress by animateFloatAsState(
        targetValue = targetProgress,
        animationSpec = tween(durationMillis = 1000, easing = FastOutSlowInEasing),
        label = "progresoBarra"
    )

    LaunchedEffect(porcentaje) {
        targetProgress = porcentaje.coerceIn(0f, 1f)
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
            .clickable { onExpandClick() },
        colors = CardDefaults.cardColors(containerColor = Color.White),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Box(modifier = Modifier.fillMaxWidth().height(IntrinsicSize.Min)) {
            // Barra de progreso de fondo
            Box(
                modifier = Modifier
                    .matchParentSize()
                    .graphicsLayer {
                        scaleX = animatedProgress
                        transformOrigin = TransformOrigin(0f, 0.5f)
                    }
                    .background(
                        Brush.horizontalGradient(
                            colors = listOf(
                                PrimaryColor.copy(alpha = 0.02f),
                                PrimaryColor.copy(alpha = 0.20f)
                            )
                        )
                    )
            )

            // Línea de acento lateral
            Box(
                modifier = Modifier
                    .fillMaxHeight()
                    .width(4.dp)
                    .background(PrimaryColor, RoundedCornerShape(topStart = 16.dp, bottomStart = 16.dp))
            )

            Row(
                modifier = Modifier.padding(20.dp).fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = mensual.mesLabel.uppercase(),
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = FontWeight.ExtraBold,
                        color = MaterialTheme.colorScheme.secondary,
                        letterSpacing = 1.sp
                    )
                    Text(
                        text = "${mensual.cantidadRegistros} registros - ${(porcentaje * 100).toInt()}%",
                        fontSize = 12.sp,
                        color = Color.Gray
                    )
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Surface(
                        color = PrimaryColor.copy(alpha = 0.1f),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text(
                            text = "${formatWeight(mensual.totalKilos)} kg",
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                            fontWeight = FontWeight.Black,
                            color = PrimaryColor,
                            fontSize = 16.sp
                        )
                    }
                    Icon(
                        imageVector = if (isExpanded) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                        contentDescription = null,
                        tint = Color.Gray,
                        modifier = Modifier.padding(start = 8.dp)
                    )
                }
            }
        }
    }
}

@Composable
private fun RegistroProduccionItem(
    ratio: Ratio,
    loteNombre: String?
) {
    val date = Instant.fromEpochMilliseconds(ratio.ratioDate)
        .toLocalDateTime(TimeZone.currentSystemDefault()).date

    val dateStr = "${date.dayOfMonth.toString().padStart(2, '0')}/${date.monthNumber.toString().padStart(2, '0')}/${date.year}"

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        shape = RoundedCornerShape(8.dp),
        elevation = CardDefaults.cardElevation(1.dp)
        // Eliminado .clickable para que no colapse al tocar aquí
    ) {
        Row(
            modifier = Modifier.padding(12.dp).fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = dateStr,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.DarkGray
                )

                val displayLote = when {
                    !loteNombre.isNullOrBlank() -> loteNombre
                    ratio.ratioLoteId.isNotBlank() -> "Desconocido"
                    else -> "Desconocido"
                }

                Text(
                    text = "Lote: $displayLote",
                    fontSize = 12.sp,
                    color = Color.Gray
                )
            }
            Text(
                text = "${formatWeight(ratio.ratioTotalWeight.toDoubleOrNull() ?: 0.0)} kg",
                fontWeight = FontWeight.ExtraBold,
                fontSize = 14.sp,
                color = MaterialTheme.colorScheme.secondary
            )
        }
    }
}