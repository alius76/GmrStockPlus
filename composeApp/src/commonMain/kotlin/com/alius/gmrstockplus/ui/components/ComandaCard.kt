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
    val isLoteAsignado = comanda.numberLoteComanda.isNotBlank()
    val cardBackground = if (comanda.fueVendidoComanda) ReservedColor.copy(alpha = 0.05f) else Color.White
    val borderColor = Color.LightGray.copy(alpha = 0.3f)
    val elevation = if (isSelected) 4.dp else 0.dp

    val formattedComandaNumber = comanda.numeroDeComanda.toString().padStart(6, '0')

    // Lógica de retraso basada en la fecha
    val isDelayed = remember(comanda.dateBookedComanda) {
        val dateBooked = comanda.dateBookedComanda?.toLocalDateTime(TimeZone.currentSystemDefault())?.date
        val today = Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault()).date
        dateBooked != null && dateBooked < today && !comanda.fueVendidoComanda
    }

    // Color de estado (Barra y Texto de Lote)
    val statusColor = if (isLoteAsignado) PrimaryColor else WarningColor

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
        elevation = CardDefaults.cardElevation(defaultElevation = elevation),
        colors = CardDefaults.cardColors(containerColor = cardBackground),
        border = BorderStroke(if (isSelected) 2.dp else 1.dp, borderColor)
    ) {
        // Envolvemos todo en una Row para la barra lateral
        Row(modifier = Modifier.height(IntrinsicSize.Min)) {
            // --- Barra Lateral Coherente con Planning ---
            Box(
                modifier = Modifier
                    .width(6.dp)
                    .fillMaxHeight()
                    .background(statusColor)
            )

            Column(modifier = Modifier.fillMaxWidth()) {
                // --- Parte Superior: Información ---
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalAlignment = Alignment.Top
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        // Badge de número de comanda
                        Surface(
                            color = if (comanda.fueVendidoComanda) Color.Gray else PrimaryColor,
                            shape = RoundedCornerShape(4.dp)
                        ) {
                            Text(
                                text = "#$formattedComandaNumber",
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                                color = Color.White,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        comanda.bookedClientComanda?.let { cliente ->
                            Text(
                                text = cliente.cliNombre.uppercase(),
                                style = MaterialTheme.typography.titleMedium.copy(
                                    fontWeight = FontWeight.ExtraBold,
                                    letterSpacing = 0.5.sp
                                ),
                                color = if (comanda.fueVendidoComanda) Color.Gray else MaterialTheme.colorScheme.onSurface
                            )
                        }

                        Text(
                            text = comanda.descriptionLoteComanda,
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )

                        Spacer(modifier = Modifier.height(4.dp))

                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                Icons.Default.Scale,
                                contentDescription = null,
                                modifier = Modifier.size(14.dp),
                                tint = Color.Gray
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "${formatWeight(comanda.totalWeightComanda.toDoubleOrNull() ?: 0.0)} Kg",
                                style = MaterialTheme.typography.bodySmall,
                                fontWeight = FontWeight.Bold
                            )
                        }

                        if (comanda.remarkComanda.isNotBlank()) {
                            Spacer(modifier = Modifier.height(8.dp))
                            Row(
                                modifier = Modifier
                                    .background(Color.Gray.copy(alpha = 0.1f), RoundedCornerShape(4.dp))
                                    .padding(6.dp)
                            ) {
                                Icon(Icons.Default.Notes, null, Modifier.size(12.dp), tint = Color.Gray)
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = comanda.remarkComanda,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = Color.Gray,
                                    maxLines = 2
                                )
                            }
                        }
                    }

                    // --- Indicador de Estado Lote y Etiquetas ---
                    Column(horizontalAlignment = Alignment.End) {
                        Text(
                            text = if (isLoteAsignado) comanda.numberLoteComanda else "PENDIENTE",
                            style = MaterialTheme.typography.titleSmall,
                            color = statusColor,
                            fontWeight = FontWeight.Black
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        if (comanda.fueVendidoComanda) {
                            Text(
                                "VENDIDO",
                                color = ReservedColor,
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold
                            )
                        } else if (isDelayed) {
                            // Sustituye a los iconos de checked/warning
                            Surface(
                                color = ReservedColor,
                                shape = RoundedCornerShape(4.dp)
                            ) {
                                Text(
                                    text = "RETRASO",
                                    color = Color.White,
                                    fontSize = 9.sp,
                                    fontWeight = FontWeight.Black,
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                )
                            }
                        }
                    }
                }

                // --- Parte Inferior: Acciones Animadas ---
                AnimatedVisibility(
                    visible = isSelected && !comanda.fueVendidoComanda,
                    enter = expandVertically() + fadeIn(),
                    exit = shrinkVertically() + fadeOut()
                ) {
                    Column {
                        HorizontalDivider(color = Color.LightGray.copy(alpha = 0.2f))
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(Color.Gray.copy(alpha = 0.03f))
                                .padding(8.dp),
                            horizontalArrangement = Arrangement.SpaceEvenly
                        ) {
                            ActionButton(
                                icon = Icons.Default.Inventory,
                                label = "Lote",
                                color = PrimaryColor,
                                onClick = { onAssignLote?.invoke() }
                            )

                            ActionButton(
                                icon = Icons.Default.CalendarMonth,
                                label = "Fecha",
                                color = PrimaryColor,
                                onClick = { onReassign?.invoke() }
                            )

                            ActionButton(
                                icon = Icons.Default.EditNote,
                                label = "Notas",
                                color = PrimaryColor,
                                onClick = { onEditRemark?.invoke() }
                            )

                            if (!isLoteAsignado) {
                                ActionButton(
                                    icon = Icons.Default.DeleteSweep,
                                    label = "Anular",
                                    color = ReservedColor,
                                    onClick = { onDelete?.invoke() }
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun ActionButton(
    icon: ImageVector,
    label: String,
    color: Color,
    onClick: () -> Unit
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .clip(RoundedCornerShape(8.dp))
            .clickable { onClick() }
            .padding(8.dp)
    ) {
        Icon(icon, contentDescription = label, tint = color, modifier = Modifier.size(24.dp))
        Text(label, style = MaterialTheme.typography.labelSmall, color = color, fontWeight = FontWeight.Medium)
    }
}