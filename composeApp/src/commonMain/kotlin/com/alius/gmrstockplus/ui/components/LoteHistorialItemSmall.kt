package com.alius.gmrstockplus.ui.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.ShoppingBag
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.alius.gmrstockplus.core.utils.formatInstant
import com.alius.gmrstockplus.core.utils.formatWeight
import com.alius.gmrstockplus.domain.model.LoteModel
import com.alius.gmrstockplus.ui.theme.PrimaryColor


@Composable
fun LoteHistorialItemSmall(lote: LoteModel, modifier: Modifier = Modifier) {
    var showBigBagsDialog by remember { mutableStateOf(false) }

    // Conversión segura del String a Number para el formateo
    val totalWeightNumber = lote.totalWeight.toDoubleOrNull() ?: 0.0 // ⬅️ Conversión añadida

    var pressed by remember { mutableStateOf(false) }
    val scale by animateFloatAsState(
        targetValue = if (pressed) 0.97f else 1f,
        animationSpec = spring()
    )

    Card(
        modifier = modifier
            .width(220.dp)
            .height(260.dp)
            .padding(6.dp)
            .graphicsLayer { scaleX = scale; scaleY = scale }
            .clickable { showBigBagsDialog = true },
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 6.dp),
        colors = CardDefaults.cardColors(containerColor = Color.Transparent)
    ) {
        // Fondo degradado diferente para historial (gris/negro/rojo suave)
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    brush = Brush.verticalGradient(
                        colors = listOf(Color(0xFF888888), Color(0xFF555555))
                    )
                )
                .padding(16.dp)
        ) {
            Column(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.SpaceBetween,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Número del lote
                Text(
                    text = lote.number,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    fontSize = 20.sp,
                    color = Color.White,
                    maxLines = 1
                )

                // Icono central de archivo/historial
                Icon(
                    imageVector = Icons.Outlined.ShoppingBag,
                    contentDescription = "Lote Vendido",
                    tint = Color.White,
                    modifier = Modifier.size(48.dp)
                )

                // Información del lote
                Column(
                    verticalArrangement = Arrangement.spacedBy(4.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = lote.description,
                        color = Color(0xCCFFFFFF),
                        fontWeight = FontWeight.Bold,
                        maxLines = 1
                    )
                    Text(
                        text = "Fecha: ${formatInstant(lote.date)}",
                        color = Color(0xAAFFFFFF)
                    )
                    Text(
                        text = lote.location,
                        color = Color(0xAAFFFFFF),
                        maxLines = 1
                    )
                    Text(
                        // ⬅️ ¡APLICACIÓN DE LA FUNCIÓN FORMATWEIGHT!
                        text = "Peso: ${formatWeight(totalWeightNumber)} Kg",
                        color = Color(0xAAFFFFFF)
                    )
                }

                // Badge Estado Historial
                Box(
                    modifier = Modifier
                        .background(Color(0xFFDD5555), RoundedCornerShape(12.dp))
                        .padding(horizontal = 10.dp, vertical = 5.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "VENDIDO",
                        fontWeight = FontWeight.SemiBold,
                        color = Color.White
                    )
                }
            }
        }
    }

    // Diálogo de BigBags
    if (showBigBagsDialog) {
        AlertDialog(
            onDismissRequest = { showBigBagsDialog = false },
            confirmButton = {
                TextButton(onClick = { showBigBagsDialog = false }) {
                    Text("Cerrar", color = PrimaryColor)
                }
            },
            title = {
                Box(
                    modifier = Modifier.fillMaxWidth(),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "Lista de BigBags",
                        color = PrimaryColor,
                        fontWeight = FontWeight.Bold,
                        fontSize = 20.sp
                    )
                }
            },
            text = {
                BigBagsDialogContent(bigBags = lote.bigBag)
            }
        )
    }
}