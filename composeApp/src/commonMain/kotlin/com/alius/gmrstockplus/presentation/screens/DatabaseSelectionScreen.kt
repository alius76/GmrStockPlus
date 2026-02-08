package com.alius.gmrstockplus.presentation.screens

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Factory
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import cafe.adriel.voyager.core.screen.Screen
import com.alius.gmrstockplus.core.utils.formatWeight
import com.alius.gmrstockplus.data.getRatioRepository
import com.alius.gmrstockplus.ui.theme.DarkGrayColor
import com.alius.gmrstockplus.ui.theme.PrimaryColor
import com.alius.gmrstockplus.ui.theme.TextSecondary
import io.github.aakira.napier.Napier
import kotlinx.coroutines.launch
import kotlinx.datetime.Clock
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime

class DatabaseSelectionScreen(
    private val onDatabaseSelected: (String) -> Unit
) : Screen {

    @Composable
    override fun Content() {
        var progressDB1 by remember { mutableStateOf(0f) }
        var progressDB2 by remember { mutableStateOf(0f) }
        var kilosDB1 by remember { mutableStateOf(0.0) }
        var kilosDB2 by remember { mutableStateOf(0.0) }

        val scope = rememberCoroutineScope()

        // Definición de objetivos diferenciados
        val OBJETIVO_P07 = 1_000_000.0
        val OBJETIVO_P08 = 600_000.0

        // Obtener el nombre del mes actual en español
        val nombreMes = remember {
            val ahora = Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault())
            when (ahora.monthNumber) {
                1 -> "Enero" ; 2 -> "Febrero" ; 3 -> "Marzo" ; 4 -> "Abril"
                5 -> "Mayo" ; 6 -> "Junio" ; 7 -> "Julio" ; 8 -> "Agosto"
                9 -> "Septiembre" ; 10 -> "Octubre" ; 11 -> "Noviembre" ; 12 -> "Diciembre"
                else -> ""
            }
        }

        LaunchedEffect(Unit) {
            // Carga P07 (Objetivo 1M)
            scope.launch {
                try {
                    val repo = getRatioRepository("P07")
                    val ratios = repo.listarRatiosDelMes()
                    kilosDB1 = ratios.sumOf { it.ratioTotalWeight.replace(",", ".").toDoubleOrNull() ?: 0.0 }
                    progressDB1 = (kilosDB1 / OBJETIVO_P07).toFloat().coerceIn(0f, 1f)
                } catch (e: Exception) {
                    Napier.e("❌ P07 Error: ${e.message}")
                }
            }

            // Carga P08 (Objetivo 600k)
            scope.launch {
                try {
                    val repo = getRatioRepository("P08")
                    val ratios = repo.listarRatiosDelMes()
                    kilosDB2 = ratios.sumOf { it.ratioTotalWeight.replace(",", ".").toDoubleOrNull() ?: 0.0 }
                    progressDB2 = (kilosDB2 / OBJETIVO_P08).toFloat().coerceIn(0f, 1f)
                } catch (e: Exception) {
                    Napier.e("❌ P08 Error: ${e.message}")
                }
            }
        }

        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color(0xFFF8F9FA)),
            contentAlignment = Alignment.TopCenter
        ) {
            Column(
                modifier = Modifier
                    .padding(horizontal = 20.dp)
                    .padding(top = 60.dp, bottom = 20.dp)
                    .widthIn(max = 900.dp)
                    .fillMaxHeight(),
                verticalArrangement = Arrangement.Top,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Cabecera estilizada
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Surface(
                        color = PrimaryColor.copy(alpha = 0.1f),
                        shape = RoundedCornerShape(16.dp),
                        modifier = Modifier.padding(bottom = 16.dp)
                    ) {
                        Text(
                            text = "CONTROL DE PLANTAS",
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = PrimaryColor
                        )
                    }
                    Text(
                        text = "GMR STOCK +",
                        fontSize = 38.sp,
                        fontWeight = FontWeight.Black,
                        color = DarkGrayColor,
                        letterSpacing = (-1).sp
                    )
                    Text(
                        text = "Seleccione planta de producción",
                        fontSize = 16.sp,
                        color = TextSecondary
                    )
                }

                Spacer(modifier = Modifier.height(50.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(20.dp)
                ) {
                    DatabaseCardDetailed(
                        label = "P07",
                        kilos = kilosDB1,
                        progress = progressDB1,
                        subLabel = "Producción $nombreMes",
                        onClick = { onDatabaseSelected("P07") },
                        modifier = Modifier.weight(1f)
                    )
                    DatabaseCardDetailed(
                        label = "P08",
                        kilos = kilosDB2,
                        progress = progressDB2,
                        subLabel = "Producción $nombreMes",
                        onClick = { onDatabaseSelected("P08") },
                        modifier = Modifier.weight(1f)
                    )
                }

                Spacer(modifier = Modifier.weight(1f))

                Text(
                    text = "GMR Stock System © 2026",
                    fontSize = 12.sp,
                    color = TextSecondary.copy(alpha = 0.5f),
                    modifier = Modifier.padding(bottom = 10.dp)
                )
            }
        }
    }

    @Composable
    fun DatabaseCardDetailed(
        label: String,
        kilos: Double,
        progress: Float,
        subLabel: String,
        onClick: () -> Unit,
        modifier: Modifier = Modifier
    ) {
        val animatedProgress by animateFloatAsState(
            targetValue = progress,
            animationSpec = tween(durationMillis = 1200)
        )

        ElevatedCard(
            onClick = onClick,
            modifier = modifier
                .height(280.dp)
                .shadow(20.dp, RoundedCornerShape(32.dp), ambientColor = PrimaryColor),
            shape = RoundedCornerShape(32.dp),
            colors = CardDefaults.elevatedCardColors(containerColor = Color.White)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(24.dp),
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.Top
                ) {
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = PrimaryColor.copy(alpha = 0.1f)
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Factory,
                            contentDescription = null,
                            tint = PrimaryColor,
                            modifier = Modifier.padding(8.dp).size(28.dp)
                        )
                    }

                    // Porcentaje calculado según el objetivo de cada planta
                    Text(
                        text = "${(progress * 100).toInt()}%",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = PrimaryColor
                    )
                }

                Column {
                    Text(
                        text = label,
                        fontSize = 44.sp,
                        fontWeight = FontWeight.Black,
                        color = DarkGrayColor,
                        lineHeight = 44.sp
                    )
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.TrendingUp, null, modifier = Modifier.size(16.dp), tint = PrimaryColor)
                        Spacer(Modifier.width(6.dp))
                        Text(
                            text = subLabel,
                            fontSize = 15.sp,
                            color = TextSecondary,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                Column {
                    Text(
                        text = "${formatWeight(kilos)} Kg",
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Black,
                        color = PrimaryColor
                    )
                }

                LinearProgressIndicator(
                    progress = { animatedProgress },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(14.dp)
                        .clip(RoundedCornerShape(20.dp)),
                    color = PrimaryColor,
                    trackColor = PrimaryColor.copy(alpha = 0.1f)
                )
            }
        }
    }
}