package com.alius.gmrstockplus.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.alius.gmrstockplus.presentation.screens.ProduccionDiaria
import com.alius.gmrstockplus.ui.theme.PrimaryColor

@Composable
fun ProduccionTrendChart(
    datos: List<ProduccionDiaria>,
    modifier: Modifier = Modifier
) {
    val maxWeight = datos.maxOfOrNull { it.totalKilos }?.takeIf { it > 0 } ?: 1000.0

    Card(
        modifier = modifier.fillMaxWidth().height(200.dp),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(2.dp)
    ) {
        Box(modifier = Modifier.padding(16.dp)) {
            Canvas(modifier = Modifier.fillMaxSize()) {
                val width = size.width
                val height = size.height
                val spacing = width / (datos.size.coerceAtLeast(1))

                // Dibujar líneas de referencia (Eje Y)
                val lines = 4
                for (i in 0..lines) {
                    val y = height - (i * (height / lines))
                    drawLine(
                        color = Color.LightGray.copy(alpha = 0.5f),
                        start = Offset(0f, y),
                        end = Offset(width, y),
                        strokeWidth = 1.dp.toPx()
                    )
                }

                // Dibujar Barras de Producción
                datos.forEachIndexed { index, punto ->
                    // Calculamos el ancho de la barra (60% del espacio disponible)
                    val barWidth = spacing * 0.6f

                    // Posición X centrada en su sección
                    val x = index * spacing + (spacing / 2)

                    // Altura proporcional al valor máximo
                    val barHeight = (punto.totalKilos.toFloat() / maxWeight.toFloat()) * height

                    drawRoundRect(
                        color = PrimaryColor,
                        topLeft = Offset(x - (barWidth / 2), height - barHeight),
                        size = Size(barWidth, barHeight),
                        cornerRadius = CornerRadius(4.dp.toPx())
                    )
                }
            }
        }
    }
}