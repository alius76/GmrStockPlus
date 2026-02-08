package com.alius.gmrstockplus.ui.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.LocalShipping
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.alius.gmrstockplus.domain.model.Venta
import com.alius.gmrstockplus.core.utils.formatInstant
import com.alius.gmrstockplus.core.utils.formatWeight // ⬅️ ¡IMPORTACIÓN AÑADIDA!
import com.alius.gmrstockplus.ui.theme.BadgeTextColor
import com.alius.gmrstockplus.ui.theme.PrimaryColor
import com.alius.gmrstockplus.ui.theme.SecondaryColor


@Composable
fun VentaItemSmall(venta: Venta, modifier: Modifier = Modifier) {
    var showDialog by remember { mutableStateOf(false) }

    // Conversión segura del String a Number para el formateo ⬅️ ¡AÑADIDO!
    val totalWeightNumber = venta.ventaPesoTotal?.toDoubleOrNull() ?: 0.0

    // Animación de zoom al presionar
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
            .pointerInput(Unit) {
                detectTapGestures(
                    onPress = {
                        pressed = true
                        try {
                            awaitRelease()
                        } finally {
                            pressed = false
                        }
                    },
                    onTap = { showDialog = true }
                )
            },
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 6.dp),
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
                // Cliente
                Text(
                    text = venta.ventaCliente,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    fontSize = 20.sp,
                    color = Color.White,
                    maxLines = 1
                )

                // Icono central
                Icon(
                    imageVector = Icons.Default.LocalShipping,
                    contentDescription = "Truck Icon",
                    tint = Color.White,
                    modifier = Modifier.size(48.dp)
                )

                // Bloque de información: lote, material, fecha, peso
                Column(
                    verticalArrangement = Arrangement.spacedBy(4.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "Lote: ${venta.ventaLote}",
                        color = Color(0xCCFFFFFF),
                        fontWeight = FontWeight.Bold,
                        maxLines = 1
                    )
                    Text(
                        text = "Material: ${venta.ventaMaterial}",
                        color = Color(0xAAFFFFFF),
                        maxLines = 1
                    )
                    Text(
                        text = "Fecha: ${formatInstant(venta.ventaFecha)}",
                        color = Color(0xAAFFFFFF)
                    )

                    // ⬇️ APLICACIÓN DEL FORMATO DE PESO ⬇️
                    val pesoFormateado = formatWeight(totalWeightNumber)
                    val pesoTexto = if (totalWeightNumber > 0) {
                        "Peso: $pesoFormateado Kg"
                    } else {
                        "Peso: No disponible"
                    }

                    Text(
                        text = pesoTexto,
                        color = Color(0xAAFFFFFF)
                    )
                }

                // Badge BigBags
                Box(
                    modifier = Modifier
                        .background(SecondaryColor, RoundedCornerShape(12.dp))
                        .padding(horizontal = 10.dp, vertical = 5.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "BigBags ${venta.ventaBigbags.size}",
                        fontWeight = FontWeight.SemiBold,
                        color = BadgeTextColor
                    )
                }
            }
        }
    }

    // Diálogo de BigBags
    if (showDialog) {
        AlertDialog(
            onDismissRequest = { showDialog = false },
            confirmButton = {
                TextButton(onClick = { showDialog = false }) {
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
                VentaBigBagsDialogContent(venta.ventaBigbags)
            }
        )
    }
}