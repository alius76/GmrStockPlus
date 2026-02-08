package com.alius.gmrstockplus.ui.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import com.alius.gmrstockplus.domain.model.Process
import com.alius.gmrstockplus.core.utils.formatInstant import kotlinx.datetime.Clock
import kotlin.time.Duration.Companion.hours


@Composable
fun ProcessItem(
    proceso: Process,
    modifier: Modifier = Modifier,
    onClick: (() -> Unit)? = null
) {
    // Animación de zoom al presionar
    var pressed by remember { mutableStateOf(false) }
    val scale by animateFloatAsState(
        targetValue = if (pressed) 0.97f else 1f,
        animationSpec = spring()
    )

    // Declara las variables con 'var' para poder reasignarles un valor
    var progress by remember { mutableStateOf(0f) }
    var estimatedTime by remember { mutableStateOf("N/A") }

    // --- LÓGICA DE CÁLCULO DE TIEMPO REAL ---
    LaunchedEffect(proceso) { // Usa LaunchedEffect para ejecutar el cálculo una vez que el 'proceso' cambie
        proceso.date?.let { startDate ->
            val now = Clock.System.now()
            val totalDurationHours = 2.hours
            val endTime = startDate.plus(totalDurationHours)

            // Calcula el progreso
            val totalTimeMillis = totalDurationHours.inWholeMilliseconds.toFloat()
            val elapsedTimeMillis = (now - startDate).inWholeMilliseconds.toFloat()

            progress = (elapsedTimeMillis / totalTimeMillis).coerceIn(0f, 1f)

            // Calcula el tiempo restante y lo formatea
            val remainingTime = endTime - now
            estimatedTime = when {
                remainingTime.isNegative() -> "Finalizado"
                else -> {
                    val hours = remainingTime.inWholeHours
                    val minutes = remainingTime.inWholeMinutes % 60
                    "${hours}h ${minutes}m"
                }
            }
        } ?: run {
            // En caso de que la fecha sea nula, mostramos valores por defecto
            progress = 0f
            estimatedTime = "N/A"
        }
    }


    Card(
        modifier = modifier
            .width(220.dp)
            .height(260.dp)
            .padding(6.dp)
            .graphicsLayer { scaleX = scale; scaleY = scale }
            .pointerInput(Unit) {
                detectTapGestures(
                    onPress = {
                        pressed = true
                        tryAwaitRelease()
                        pressed = false
                        onClick?.invoke()
                    }
                )
            },
        elevation = CardDefaults.cardElevation(defaultElevation = 6.dp),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.Transparent)
    ) {
        // Fondo degradado
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    brush = Brush.verticalGradient(
                        colors = listOf(Color(0xFF029083), Color(0xFF00BFA5))
                    )
                )
                .padding(16.dp)
        ) {
            Column(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.SpaceBetween,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Número de lote y tipo de material
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = proceso.number,
                        fontSize = 22.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = proceso.description,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Medium,
                        color = Color(0xCCFFFFFF)
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Fecha: ${formatInstant(proceso.date)}",
                        fontSize = 12.sp,
                        color = Color(0xAAFFFFFF)
                    )
                }

                // Icono central
                Icon(
                    imageVector = Icons.Default.Settings,
                    contentDescription = "Proceso",
                    tint = Color.White,
                    modifier = Modifier.size(48.dp)
                )

                // Progress bar
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    LinearProgressIndicator(
                        progress = progress,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(6.dp)
                            .clip(RoundedCornerShape(8.dp)),
                        color = Color.Yellow,
                        trackColor = Color(0x33FFFFFF)
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Progreso: ${(progress * 100).toInt()}% - Est. $estimatedTime",
                        fontSize = 12.sp,
                        color = Color(0xCCFFFFFF)
                    )
                }
            }
        }
    }
}