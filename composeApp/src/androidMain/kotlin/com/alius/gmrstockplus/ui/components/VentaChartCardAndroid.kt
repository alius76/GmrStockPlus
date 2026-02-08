package com.alius.gmrstockplus.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.unit.dp
import com.alius.gmrstockplus.ui.theme.PrimaryColor
import com.alius.gmrstockplus.core.utils.formatWeight
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.text.ExperimentalTextApi
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.drawText
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.unit.sp

@OptIn(ExperimentalTextApi::class)
@Composable
actual fun VentaChartCard(
    modifier: Modifier,
    ventaDataList: List<VentaData>,
    isAnnual: Boolean
) {
    val data = ventaDataList
    if (data.isEmpty()) return

    val maxWeight = if (isAnnual) 1_000_000f else 160_000f
    val yLabels = if (isAnnual)
        listOf(0f, 200_000f, 400_000f, 600_000f, 800_000f, 1_000_000f)
    else
        listOf(0f, 20_000f, 40_000f, 60_000f, 80_000f, 100_000f, 120_000f, 140_000f, 160_000f)

    val yLabelStrings = yLabels.map {
        if (it >= 1_000_000f) "${(it / 1_000_000).toInt()}M"
        else "${(it / 1000).toInt()}K"
    }

    var selectedIndex by remember(data) { mutableStateOf<Int?>(null) }

    // ⭐️ NUEVO: Inicializar TextMeasurer y TextStyle para compatibilidad
    val textMeasurer = rememberTextMeasurer()
    val textStyle = TextStyle(color = Color.Black, fontSize = 12.sp)

    Card(
        modifier = modifier,
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(4.dp)
    ) {
        Canvas(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
                .pointerInput(data) {
                    detectTapGestures { tapOffset ->
                        val leftPadding = 60f
                        val chartWidth = size.width - leftPadding
                        val stepX =
                            if (data.size > 1) chartWidth / (data.size - 1) else chartWidth
                        val idx =
                            ((tapOffset.x - leftPadding + stepX / 2) / stepX).toInt()
                        selectedIndex = idx.coerceIn(0, data.lastIndex)
                    }
                }
        ) {
            val leftPadding = 60f
            val bottomPadding = 30.dp.toPx()
            val chartWidth = size.width - leftPadding
            val chartHeight = size.height - bottomPadding
            val stepX =
                if (data.size > 1) chartWidth / (data.size - 1) else chartWidth
            val scaleY = chartHeight / maxWeight

            // --- Eje Y (Usando drawText) ---
            yLabels.forEachIndexed { index, value ->
                val y = chartHeight - value * scaleY
                drawLine(
                    color = Color(0xFFE0E0E0),
                    start = Offset(leftPadding, y),
                    end = Offset(leftPadding + chartWidth, y)
                )

                // ⭐️ Reemplazamos nativeCanvas.drawText con drawText
                val measured = textMeasurer.measure(yLabelStrings[index], textStyle)
                drawText(
                    textMeasurer = textMeasurer,
                    text = yLabelStrings[index],
                    topLeft = Offset(
                        leftPadding - measured.size.width - 4.dp.toPx(),
                        y - measured.size.height / 2 // Centrado vertical
                    ),
                    style = textStyle
                )
            }

            // --- Área y Línea Principal (Sin cambios estructurales) ---
            val areaPath = Path().apply {
                moveTo(leftPadding, chartHeight - data.first().totalWeight * scaleY)
                data.forEachIndexed { index, d ->
                    val x = leftPadding + index * stepX
                    val y = chartHeight - d.totalWeight * scaleY
                    lineTo(x, y)
                }
                lineTo(leftPadding + chartWidth, chartHeight)
                lineTo(leftPadding, chartHeight)
                close()
            }
            drawPath(
                areaPath,
                brush = Brush.verticalGradient(
                    listOf(
                        PrimaryColor.copy(alpha = 0.3f),
                        Color.Transparent
                    )
                )
            )

            for (i in 0 until data.size - 1) {
                val x1 = leftPadding + i * stepX
                val y1 = chartHeight - data[i].totalWeight * scaleY
                val x2 = leftPadding + (i + 1) * stepX
                val y2 = chartHeight - data[i + 1].totalWeight * scaleY
                drawLine(
                    brush = Brush.linearGradient(
                        listOf(
                            PrimaryColor,
                            PrimaryColor.copy(alpha = 0.7f)
                        )
                    ),
                    start = Offset(x1, y1),
                    end = Offset(x2, y2),
                    strokeWidth = 4f
                )
            }

            // --- Puntos (Sin cambios) ---
            data.forEachIndexed { index, d ->
                val x = leftPadding + index * stepX
                val y = chartHeight - d.totalWeight * scaleY
                drawCircle(color = PrimaryColor, radius = 6f, center = Offset(x, y))
            }

            // --- Tooltip (Usando drawText) ---
            selectedIndex?.let { idx ->
                val safeIdx = idx.coerceIn(0, data.lastIndex)
                val xPoint = leftPadding + safeIdx * stepX

                drawLine(
                    color = Color.Gray,
                    start = Offset(xPoint, 0f),
                    end = Offset(xPoint, chartHeight),
                    strokeWidth = 2f,
                    pathEffect = PathEffect.dashPathEffect(floatArrayOf(10f, 10f), 0f)
                )

                val tooltipTitle = if (isAnnual) "Mes ${data[safeIdx].day}" else "Día ${data[safeIdx].day}"
                val tooltipValue = "${formatWeight(data[safeIdx].totalWeight)} Kg"

                val titleStyle = textStyle.copy(color = PrimaryColor, fontSize = 13.sp)
                val valueStyle = textStyle.copy(color = PrimaryColor, fontSize = 13.sp)

                // ⭐️ Usamos TextMeasurer para medir y dibujar
                val line1 = textMeasurer.measure(tooltipTitle, titleStyle)
                val line2 = textMeasurer.measure(tooltipValue, valueStyle)

                val tooltipWidth = maxOf(line1.size.width, line2.size.width) + 20.dp.toPx()
                val tooltipHeight = line1.size.height + line2.size.height + 16.dp.toPx()

                val tooltipX = leftPadding + (chartWidth - tooltipWidth) / 2f
                val tooltipY = chartHeight / 3f

                drawRoundRect(
                    color = Color.White,
                    topLeft = Offset(tooltipX, tooltipY),
                    size = androidx.compose.ui.geometry.Size(tooltipWidth, tooltipHeight),
                    cornerRadius = CornerRadius(10.dp.toPx())
                )

                drawRoundRect(
                    color = Color.LightGray,
                    topLeft = Offset(tooltipX, tooltipY),
                    size = androidx.compose.ui.geometry.Size(tooltipWidth, tooltipHeight),
                    cornerRadius = CornerRadius(10.dp.toPx()),
                    style = Stroke(width = 1.dp.toPx())
                )

                // ⭐️ Usamos drawText
                drawText(
                    textMeasurer,
                    tooltipTitle,
                    topLeft = Offset(
                        tooltipX + (tooltipWidth - line1.size.width) / 2,
                        tooltipY + 4.dp.toPx()
                    ),
                    style = titleStyle
                )
                drawText(
                    textMeasurer,
                    tooltipValue,
                    topLeft = Offset(
                        tooltipX + (tooltipWidth - line2.size.width) / 2,
                        tooltipY + line1.size.height + 6.dp.toPx()
                    ),
                    style = valueStyle
                )
            }

            // --- Eje X (Usando drawText) ---
            val firstLabel =
                if (isAnnual) "Mes ${data.first().day}" else "Día ${data.first().day}"
            val lastLabel =
                if (isAnnual) "Mes ${data.last().day}" else "Día ${data.last().day}"

            // ⭐️ Usamos la coordenada que funciona para RatioProductionCard
            val xLabelY = chartHeight + 10.dp.toPx()

            drawText(
                textMeasurer,
                firstLabel,
                Offset(leftPadding, xLabelY),
                textStyle
            )

            // ⭐️ Usamos TextMeasurer para medir el ancho del último label
            val lastMeasured = textMeasurer.measure(lastLabel, textStyle)
            drawText(
                textMeasurer,
                lastLabel,
                Offset(
                    leftPadding + chartWidth - lastMeasured.size.width,
                    xLabelY
                ),
                textStyle
            )
        }
    }
}