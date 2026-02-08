package com.alius.gmrstockplus.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
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
import com.alius.gmrstockplus.domain.model.Certificado
import com.alius.gmrstockplus.presentation.screens.VentasMes
import com.alius.gmrstockplus.ui.theme.PrimaryColor

@Composable
fun ExpandableMesCard(
    mesData: VentasMes,
    porcentaje: Float,
    certificadosMap: Map<String, Certificado?>
) {
    // Estado para controlar si el mes está expandido o contraído
    var expanded by remember { mutableStateOf(false) }

    Column(modifier = Modifier.fillMaxWidth()) {
        // Cabecera con el estilo de Producción
        ProduccionMensualRowOriginalStyle(
            mesLabel = mesData.mesLabel,
            totalKilos = mesData.totalKilos,
            cantidadRegistros = mesData.listaVentas.size,
            porcentaje = porcentaje,
            isExpanded = expanded,
            onClick = { expanded = !expanded }
        )

        // Contenido expandible (Las filas de ventas)
        AnimatedVisibility(
            visible = expanded,
            enter = expandVertically() + fadeIn(),
            exit = shrinkVertically() + fadeOut()
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 8.dp)
                    // Fondo sutil para agrupar visualmente las ventas del mes
                    .background(
                        Color.Black.copy(alpha = 0.03f),
                        RoundedCornerShape(bottomStart = 16.dp, bottomEnd = 16.dp)
                    )
                    .padding(8.dp),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                mesData.listaVentas.forEach { venta ->
                    VentaRow(
                        venta = venta,
                        certificado = certificadosMap[venta.ventaLote]
                    )
                }
            }
        }
    }
}

@Composable
private fun ProduccionMensualRowOriginalStyle(
    mesLabel: String,
    totalKilos: Double,
    cantidadRegistros: Int,
    porcentaje: Float,
    isExpanded: Boolean,
    onClick: () -> Unit
) {
    // Animación de la barra de progreso (igual que en tu pantalla de producción)
    var targetProgress by remember { mutableStateOf(0f) }
    val animatedProgress by animateFloatAsState(
        targetValue = targetProgress,
        animationSpec = tween(900, easing = androidx.compose.animation.core.FastOutSlowInEasing),
        label = "progress"
    )
    LaunchedEffect(porcentaje) { targetProgress = porcentaje.coerceIn(0f, 1f) }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        colors = CardDefaults.cardColors(containerColor = Color.White),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Box(modifier = Modifier.fillMaxWidth().height(IntrinsicSize.Min)) {
            // Barra de progreso de fondo con degradado
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

            // Contenido de la fila
            Row(
                modifier = Modifier
                    .padding(horizontal = 20.dp, vertical = 18.dp)
                    .fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = mesLabel.uppercase(),
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = FontWeight.ExtraBold,
                        color = MaterialTheme.colorScheme.secondary,
                        letterSpacing = 1.sp
                    )
                    Text(
                        text = "$cantidadRegistros registros - ${(porcentaje * 100).toInt()}%",
                        fontSize = 12.sp,
                        color = Color.Gray.copy(alpha = 0.8f)
                    )
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Surface(
                        color = PrimaryColor.copy(alpha = 0.1f),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text(
                            text = "${formatWeight(totalKilos)} kg",
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                            fontWeight = FontWeight.Black,
                            color = PrimaryColor,
                            fontSize = 16.sp
                        )
                    }

                    // Icono indicador de expansión
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