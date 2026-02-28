package com.alius.gmrstockplus.ui.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.alius.gmrstockplus.domain.model.LoteModel
import com.alius.gmrstockplus.ui.theme.PrimaryColor

@Composable
fun ConfirmAnulationDialog(
    lote: LoteModel,
    comandaNumber: String,
    currentUserEmail: String,
    onConfirm: (Boolean) -> Unit, // Boolean indica si se anula la reserva (true) o solo la asignación (false)
    onDismiss: () -> Unit
) {
    // Estado interno del diálogo para la opción elegida si hay reserva
    var clearBookingOption by remember { mutableStateOf(false) }
    val tieneReserva = lote.booked != null

    AlertDialog(
        onDismissRequest = onDismiss,
        confirmButton = {
            TextButton(onClick = { onConfirm(clearBookingOption) }) {
                Text(
                    text = "Confirmar",
                    color = MaterialTheme.colorScheme.error,
                    fontWeight = FontWeight.Bold
                )
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancelar", color = PrimaryColor)
            }
        },
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.Warning,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.error
                )
                Spacer(Modifier.width(8.dp))
                Text("Anular asignación", style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold))
            }
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {

                // Tarjeta de información del lote y comanda (actúa como contexto visual)
                Surface(
                    color = MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.2f),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Text("Lote: ${lote.number}", fontWeight = FontWeight.Bold)
                        Text("Comanda: $comandaNumber")
                    }
                }

                // --- SECCIÓN CONDICIONAL DE RESERVA ---
                if (tieneReserva) {
                    Text(
                        text = "El lote está reservado para el cliente. Selecciona el tipo de anulación:",
                        style = MaterialTheme.typography.bodySmall,
                        fontWeight = FontWeight.Bold
                    )

                    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        val radioColors = RadioButtonDefaults.colors(
                            selectedColor = PrimaryColor,
                            unselectedColor = MaterialTheme.colorScheme.onSurfaceVariant
                        )

                        // Opción 1: Mantener reserva
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { clearBookingOption = false }
                                .padding(vertical = 4.dp)
                        ) {
                            RadioButton(
                                selected = !clearBookingOption,
                                onClick = { clearBookingOption = false },
                                colors = radioColors
                            )
                            Spacer(Modifier.width(8.dp))
                            Text("Mantener reserva al cliente", style = MaterialTheme.typography.bodyMedium)
                        }

                        // Opción 2: Anular reserva
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { clearBookingOption = true }
                                .padding(vertical = 4.dp)
                        ) {
                            RadioButton(
                                selected = clearBookingOption,
                                onClick = { clearBookingOption = true },
                                colors = radioColors
                            )
                            Spacer(Modifier.width(8.dp))
                            Text("Anular reserva (Stock Libre)", style = MaterialTheme.typography.bodyMedium)
                        }
                    }
                } else {
                    // Si no hay reserva, solo un pequeño texto informativo del resultado
                    Text(
                        text = "Resultado: Los BigBags asignados volverán a estar disponibles para otras comandas.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                // Pie con el email del usuario que realiza la acción
                Row(
                    modifier = Modifier.fillMaxWidth().padding(top = 4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.Description,
                        contentDescription = null,
                        modifier = Modifier.size(14.dp),
                        tint = Color.Gray
                    )
                    Spacer(Modifier.width(4.dp))
                    Text(
                        text = "Acción por: $currentUserEmail",
                        style = MaterialTheme.typography.bodySmall,
                        color = Color.Gray
                    )
                }
            }
        },
        shape = RoundedCornerShape(16.dp),
        containerColor = MaterialTheme.colorScheme.surfaceContainerHigh
    )
}