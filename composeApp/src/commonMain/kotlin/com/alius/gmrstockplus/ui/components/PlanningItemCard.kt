package com.alius.gmrstockplus.ui.components

import androidx.compose.animation.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Inventory2
import androidx.compose.material.icons.filled.RadioButtonUnchecked
import androidx.compose.material.icons.filled.Scale
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.alius.gmrstockplus.core.utils.formatWeight
import com.alius.gmrstockplus.domain.model.Comanda
import com.alius.gmrstockplus.ui.theme.PrimaryColor
import com.alius.gmrstockplus.ui.theme.WarningColor

@Composable
fun PlanningItemCard(
    comanda: Comanda,
    modifier: Modifier = Modifier
) {
    var isExpanded by remember { mutableStateOf(false) }

    // --- Lógica de Estado Multi-material ---
    val totalMateriales = comanda.listaAsignaciones.size
    val asignacionesConLote = comanda.listaAsignaciones.count { it.numeroLote.isNotBlank() }

    val estaCompleta = totalMateriales > 0 && totalMateriales == asignacionesConLote
    val esParcial = asignacionesConLote > 0 && asignacionesConLote < totalMateriales

    val statusColor = when {
        comanda.fueVendidoComanda -> Color.Gray
        esParcial -> Color(0xFF4CAF50)
        estaCompleta -> PrimaryColor
        else -> WarningColor
    }

    Card(
        modifier = modifier
            .fillMaxWidth()
            .clickable { isExpanded = !isExpanded },
        shape = RoundedCornerShape(12.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = if (isExpanded) 4.dp else 1.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        border = BorderStroke(1.dp, Color.LightGray.copy(alpha = 0.4f))
    ) {
        Row(modifier = Modifier.height(IntrinsicSize.Min)) {
            Box(
                modifier = Modifier
                    .width(5.dp)
                    .fillMaxHeight()
                    .background(statusColor)
            )

            Column(modifier = Modifier.padding(10.dp)) {
                // Nombre del Cliente
                Text(
                    text = comanda.bookedClientComanda?.cliNombre?.uppercase() ?: "SIN CLIENTE",
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Black,
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )

                Spacer(modifier = Modifier.height(6.dp))

                // --- LISTA DE MATERIALES, LOTES Y BB ---
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    comanda.listaAsignaciones.take(4).forEach { asig ->
                        val tieneLote = asig.numeroLote.isNotBlank()

                        Column {
                            // Línea de Material
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = if (tieneLote) Icons.Default.Inventory2 else Icons.Default.RadioButtonUnchecked,
                                    contentDescription = null,
                                    modifier = Modifier.size(10.dp),
                                    tint = if (tieneLote) statusColor else Color.Gray.copy(alpha = 0.6f)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = asig.materialNombre,
                                    fontSize = 10.sp,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis,
                                    fontWeight = if (tieneLote) FontWeight.Bold else FontWeight.Normal,
                                    color = if (tieneLote) Color.Black else Color.Gray
                                )
                            }

                            // Línea de Lote y Cantidad (Solo si hay lote)
                            if (tieneLote) {
                                Text(
                                    text = "${asig.numeroLote} (${asig.cantidadBB} BB)",
                                    fontSize = 9.sp,
                                    color = statusColor,
                                    fontWeight = FontWeight.Medium,
                                    modifier = Modifier.padding(start = 14.dp)
                                )
                            }
                        }
                    }
                    if (totalMateriales > 4) {
                        Text(
                            text = "+${totalMateriales - 4} más...",
                            fontSize = 8.sp,
                            color = PrimaryColor,
                            modifier = Modifier.padding(start = 14.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                // --- FOOTER ---
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Scale, null, Modifier.size(12.dp), tint = Color.Gray)
                        Spacer(modifier = Modifier.width(3.dp))
                        Text(
                            text = "${formatWeight(comanda.totalWeightComanda.toDoubleOrNull() ?: 0.0)} Kg",
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Black,
                            color = Color.DarkGray
                        )
                    }

                    Surface(
                        color = statusColor.copy(alpha = 0.1f),
                        shape = RoundedCornerShape(4.dp)
                    ) {
                        Text(
                            text = if (estaCompleta) "LISTO" else if (esParcial) "PARCIAL" else "PEND.",
                            fontSize = 8.sp,
                            fontWeight = FontWeight.Black,
                            color = statusColor,
                            modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp)
                        )
                    }
                }

                // Observaciones expandibles
                AnimatedVisibility(visible = isExpanded && comanda.remarkComanda.isNotBlank()) {
                    Column {
                        HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp), thickness = 0.5.dp)
                        Text(
                            text = comanda.remarkComanda,
                            style = MaterialTheme.typography.bodySmall,
                            fontSize = 10.sp,
                            color = Color.DarkGray
                        )
                    }
                }
            }
        }
    }
}