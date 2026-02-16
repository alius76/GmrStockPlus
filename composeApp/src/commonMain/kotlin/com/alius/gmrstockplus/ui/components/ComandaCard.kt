package com.alius.gmrstockplus.ui.components

import androidx.compose.animation.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.ripple.rememberRipple
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.alius.gmrstockplus.core.utils.formatWeight
import com.alius.gmrstockplus.domain.model.Comanda
import com.alius.gmrstockplus.ui.theme.PrimaryColor
import com.alius.gmrstockplus.ui.theme.ReservedColor
import com.alius.gmrstockplus.ui.theme.WarningColor
import kotlinx.datetime.*

@Composable
fun ComandaCard(
    comanda: Comanda,
    isSelected: Boolean = false,
    onClick: (Comanda) -> Unit = {},
    onDelete: (() -> Unit)? = null,
    onReassign: (() -> Unit)? = null,
    onEditRemark: (() -> Unit)? = null,
    onAssignLote: (() -> Unit)? = null
) {
    // 1. Identificamos si hay lotes físicos vinculados
    val tieneAsignacionesReales = remember(comanda.listaAsignaciones) {
        comanda.listaAsignaciones.any { it.numeroLote.isNotBlank() }
    }

    // 2. Identificamos si TODAS las asignaciones reales ya han sido vendidas
    // (Para el feedback visual de la card antes de que se cierre la comanda)
    val totalAsignacionesConLote = comanda.listaAsignaciones.count { it.numeroLote.isNotBlank() }
    val asignacionesVendidas = comanda.listaAsignaciones.count { it.fueVendido }
    val esVentaParcial = asignacionesVendidas > 0 && asignacionesVendidas < totalAsignacionesConLote

    val isLoteAsignado = tieneAsignacionesReales || comanda.numberLoteComanda.isNotBlank()
    val cardBackground = if (comanda.fueVendidoComanda) ReservedColor.copy(alpha = 0.05f) else Color.White

    val statusColor = when {
        comanda.fueVendidoComanda -> Color.Gray
        esVentaParcial -> Color(0xFF4CAF50) // Un verde intermedio para indicar progreso
        isLoteAsignado -> PrimaryColor
        else -> WarningColor
    }

    val isDelayed = remember(comanda.dateBookedComanda) {
        val dateBooked = comanda.dateBookedComanda?.toLocalDateTime(TimeZone.currentSystemDefault())?.date
        val today = Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault()).date
        dateBooked != null && dateBooked < today && !comanda.fueVendidoComanda
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
            .clickable(
                onClick = { onClick(comanda) },
                indication = rememberRipple(color = PrimaryColor.copy(alpha = 0.1f)),
                interactionSource = remember { MutableInteractionSource() }
            ),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = if (isSelected) 4.dp else 0.dp),
        colors = CardDefaults.cardColors(containerColor = cardBackground),
        border = BorderStroke(if (isSelected) 2.dp else 1.dp, Color.LightGray.copy(alpha = 0.3f))
    ) {
        Row(modifier = Modifier.height(IntrinsicSize.Min)) {
            Box(modifier = Modifier.width(6.dp).fillMaxHeight().background(statusColor))

            Column(modifier = Modifier.fillMaxWidth()) {
                Row(
                    modifier = Modifier.fillMaxWidth().padding(16.dp),
                    verticalAlignment = Alignment.Top
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        // --- CABECERA ---
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Surface(
                                color = if (comanda.fueVendidoComanda) Color.Gray else PrimaryColor,
                                shape = RoundedCornerShape(4.dp)
                            ) {
                                Text(
                                    text = "#${comanda.numeroDeComanda.toString().padStart(6, '0')}",
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                                    color = Color.White,
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                            if (comanda.userEmailComanda.isNotBlank()) {
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "Generada por: ${comanda.userEmailComanda.split("@").firstOrNull()}",
                                    fontSize = 9.sp, color = Color.Gray
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        // --- CLIENTE ---
                        comanda.bookedClientComanda?.let { cliente ->
                            Text(
                                text = cliente.cliNombre.uppercase(),
                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.ExtraBold),
                                color = if (comanda.fueVendidoComanda) Color.Gray else MaterialTheme.colorScheme.onSurface
                            )
                        }

                        // --- LISTADO DE MATERIALES CON ESTADO INDIVIDUAL ---
                        if (comanda.listaAsignaciones.isNotEmpty()) {
                            comanda.listaAsignaciones.forEach { asig ->
                                // Solo mostramos el lote si existe
                                val labelLote = if (asig.numeroLote.isNotBlank()) " [${asig.numeroLote}]" else " (Pendiente)"

                                // CORRECCIÓN: El usuario de asignación solo se procesa si existe el lote
                                // Si asig.userAsignacion está vacío, userSuffix será ""
                                val userSuffix = if (asig.numeroLote.isNotBlank() && asig.userAsignacion.isNotBlank()) {
                                    " • ${asig.userAsignacion.split("@").first()}"
                                } else ""

                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(
                                        text = "• ${asig.materialNombre}$labelLote - ${asig.cantidadBB} BB$userSuffix",
                                        style = MaterialTheme.typography.bodySmall.copy(
                                            textDecoration = if (asig.fueVendido) TextDecoration.LineThrough else TextDecoration.None
                                        ),
                                        color = when {
                                            comanda.fueVendidoComanda || asig.fueVendido -> Color.Gray
                                            asig.numeroLote.isNotBlank() -> PrimaryColor
                                            else -> MaterialTheme.colorScheme.onSurfaceVariant
                                        },
                                        fontWeight = if (asig.numeroLote.isNotBlank()) FontWeight.Bold else FontWeight.Medium
                                    )
                                    if (asig.fueVendido && !comanda.fueVendidoComanda) {
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Icon(Icons.Default.CheckCircle, null, Modifier.size(12.dp), tint = Color(0xFF4CAF50))
                                    }
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        // --- INFO INFERIOR ---
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Scale, null, Modifier.size(14.dp), tint = Color.Gray)
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "${formatWeight(comanda.totalWeightComanda.toDoubleOrNull() ?: 0.0)} Kg",
                                style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.Bold
                            )
                        }

                        if (comanda.remarkComanda.isNotBlank()) {
                            Spacer(modifier = Modifier.height(6.dp))
                            Row(verticalAlignment = Alignment.Top) {
                                Icon(
                                    imageVector = Icons.Default.ChatBubbleOutline,
                                    contentDescription = null,
                                    modifier = Modifier.size(14.dp).padding(top = 2.dp),
                                    tint = Color.Gray.copy(alpha = 0.8f)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = comanda.remarkComanda,
                                    style = MaterialTheme.typography.bodySmall.copy(
                                        lineHeight = 16.sp,
                                        fontStyle = androidx.compose.ui.text.font.FontStyle.Italic
                                    ),
                                    color = Color.DarkGray
                                )
                            }
                        }
                    }

                    // --- COLUMNA DERECHA: ESTADO ---
                    Column(horizontalAlignment = Alignment.End) {
                        if (comanda.fueVendidoComanda) {
                            Surface(
                                color = Color.Gray.copy(alpha = 0.1f),
                                shape = RoundedCornerShape(4.dp),
                                border = BorderStroke(1.dp, Color.Gray)
                            ) {
                                Text(
                                    text = "VENDIDA",
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                    style = MaterialTheme.typography.labelLarge,
                                    color = Color.Gray,
                                    fontWeight = FontWeight.ExtraBold
                                )
                            }
                        } else {
                            Text(
                                text = if (esVentaParcial) "PARCIAL" else if (isLoteAsignado) "ASIGNADO" else "PENDIENTE",
                                style = MaterialTheme.typography.titleSmall,
                                color = statusColor,
                                fontWeight = FontWeight.Black
                            )
                        }

                        if (isDelayed) {
                            Spacer(modifier = Modifier.height(8.dp))
                            Surface(color = ReservedColor, shape = RoundedCornerShape(4.dp)) {
                                Text("RETRASO", color = Color.White, fontSize = 9.sp, modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp))
                            }
                        }
                    }
                }

                // --- PANEL DE ACCIONES ---
                AnimatedVisibility(visible = isSelected && !comanda.fueVendidoComanda) {
                    Column {
                        HorizontalDivider(color = Color.LightGray.copy(alpha = 0.2f))
                        Row(
                            modifier = Modifier.fillMaxWidth().background(Color.Gray.copy(alpha = 0.03f)).padding(8.dp),
                            horizontalArrangement = Arrangement.SpaceEvenly
                        ) {
                            ActionButton(
                                icon = Icons.Default.Inventory,
                                label = if (tieneAsignacionesReales) "Ver Lotes" else "Lote",
                                color = PrimaryColor,
                                onClick = { onAssignLote?.invoke() }
                            )
                            ActionButton(Icons.Default.CalendarMonth, "Fecha", PrimaryColor) { onReassign?.invoke() }
                            ActionButton(Icons.Default.EditNote, "Notas", PrimaryColor) { onEditRemark?.invoke() }

                            if (!isLoteAsignado) {
                                ActionButton(Icons.Default.DeleteSweep, "Anular", ReservedColor) { onDelete?.invoke() }
                            } else {
                                // Si hay algo vendido, bloqueamos el botón de acción para proteger la integridad
                                ActionButton(
                                    icon = if (esVentaParcial) Icons.Default.LocalShipping else Icons.Default.Lock,
                                    label = if (esVentaParcial) "Cargando..." else "Asignado",
                                    color = Color.Gray.copy(alpha = 0.5f)
                                ) { }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun ActionButton(icon: ImageVector, label: String, color: Color, onClick: () -> Unit) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.clip(RoundedCornerShape(8.dp)).clickable { onClick() }.padding(8.dp)
    ) {
        Icon(icon, label, tint = color, modifier = Modifier.size(24.dp))
        Text(label, style = MaterialTheme.typography.labelSmall, color = color, fontWeight = FontWeight.Medium)
    }
}