package com.alius.gmrstockplus.presentation.screens

import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Factory
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import cafe.adriel.voyager.core.screen.Screen
import com.alius.gmrstockplus.data.getRatioRepository
import com.alius.gmrstockplus.ui.theme.DarkGrayColor
import com.alius.gmrstockplus.ui.theme.PrimaryColor
import com.alius.gmrstockplus.ui.theme.TextSecondary
import kotlinx.coroutines.launch

class DatabaseSelectionScreen(
    private val onDatabaseSelected: (String) -> Unit
) : Screen {

    @Composable
    override fun Content() {
        var progressDB1 by remember { mutableStateOf(0f) }
        var progressDB2 by remember { mutableStateOf(0f) }
        val scope = rememberCoroutineScope()

        // 1. CARGA DE DATOS REALES (Filtro Mensual / Objetivo 1.5M Kg)
        LaunchedEffect(Unit) {
            val objetivo = 1_500_000.0

            // Carga Planta P07
            scope.launch {
                try {
                    val repo = getRatioRepository("P07")
                    val ratios = repo.listarRatiosDelMes()
                    val acumulado = ratios.sumOf { it.ratioTotalWeight.toDoubleOrNull() ?: 0.0 }
                    progressDB1 = (acumulado / objetivo).toFloat().coerceAtMost(1.0f)
                } catch (e: Exception) {
                    progressDB1 = 0.0f
                }
            }

            // Carga Planta P08
            scope.launch {
                try {
                    val repo = getRatioRepository("P08")
                    val ratios = repo.listarRatiosDelMes()
                    val acumulado = ratios.sumOf { it.ratioTotalWeight.toDoubleOrNull() ?: 0.0 }
                    progressDB2 = (acumulado / objetivo).toFloat().coerceAtMost(1.0f)
                } catch (e: Exception) {
                    progressDB2 = 0.0f
                }
            }
        }

        // Box principal para centrar el contenido en pantallas grandes (Desktop/Tablets)
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background),
            contentAlignment = Alignment.TopCenter // 👈 Clave para Desktop: Centra el contenido horizontalmente
        ) {
            Column(
                modifier = Modifier
                    .padding(horizontal = 24.dp)
                    .padding(top = 80.dp, bottom = 40.dp)
                    .widthIn(max = 800.dp) // 👈 LIMITADOR: Evita que se estire demasiado en PC
                    .fillMaxHeight(),
                verticalArrangement = Arrangement.Top,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Cabecera de la App
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = "GMR Stock +",
                        fontSize = 42.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = PrimaryColor
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Gestión de stock en tiempo real",
                        fontSize = 18.sp,
                        color = TextSecondary
                    )
                }

                Spacer(modifier = Modifier.height(60.dp))

                // Título de Selección
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = "Seleccione planta",
                        fontSize = 26.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = DarkGrayColor
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Conexión directa a producción",
                        fontSize = 18.sp,
                        color = TextSecondary
                    )
                }

                Spacer(modifier = Modifier.height(40.dp))

                // Fila de Tarjetas (Se adaptan al widthIn de la columna)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    DatabaseCardWithProcessStyle(
                        label = "P07",
                        progress = progressDB1,
                        onClick = { onDatabaseSelected("P07") },
                        modifier = Modifier.weight(1f)
                    )
                    DatabaseCardWithProcessStyle(
                        label = "P08",
                        progress = progressDB2,
                        onClick = { onDatabaseSelected("P08") },
                        modifier = Modifier.weight(1f)
                    )
                }

                Spacer(modifier = Modifier.weight(1f))

                // Footer Informativo
                Text(
                    text = "v1.0.0 build 1 | GMR Stock Team",
                    fontSize = 12.sp,
                    color = TextSecondary.copy(alpha = 0.7f)
                )
            }
        }
    }

    @Composable
    fun DatabaseCardWithProcessStyle(
        label: String,
        progress: Float,
        onClick: () -> Unit,
        modifier: Modifier = Modifier
    ) {
        // Animación de llenado progresivo
        val animatedProgress by animateFloatAsState(
            targetValue = progress,
            animationSpec = tween(durationMillis = 1500) // 1.5s para suavidad
        )

        ElevatedCard(
            onClick = onClick,
            modifier = modifier
                .height(220.dp) // Un poco más alto para mejorar el aspecto en PC
                .shadow(8.dp, RoundedCornerShape(20.dp)),
            shape = RoundedCornerShape(20.dp)
        ) {
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
                    modifier = Modifier.fillMaxSize().animateContentSize(),
                    verticalArrangement = Arrangement.SpaceBetween,
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Icon(
                        imageVector = Icons.Filled.Factory,
                        contentDescription = null,
                        tint = Color.White.copy(alpha = 0.9f),
                        modifier = Modifier.size(72.dp)
                    )

                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = label,
                            fontSize = 28.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                        Text(
                            text = "Producción mensual",
                            fontSize = 14.sp,
                            color = Color.White.copy(alpha = 0.8f)
                        )
                    }

                    // Barra de progreso animada
                    LinearProgressIndicator(
                        progress = { animatedProgress },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(10.dp)
                            .clip(RoundedCornerShape(12.dp)),
                        color = Color.Yellow,
                        trackColor = Color(0x33FFFFFF)
                    )
                }
            }
        }
    }
}