package com.alius.gmrstockplus.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.alius.gmrstockplus.core.utils.formatWeight
import com.alius.gmrstockplus.ui.theme.PrimaryColor

@Composable
fun ConfirmAssignmentDialog(
    loteNumber: String,
    comandaNumber: String,
    disponibles: Int,
    totalBB: Int,
    totalWeightNumber: Double,
    totalWeightComanda: String,
    currentUserEmail: String,
    onConfirm: (Int) -> Unit,
    onDismiss: () -> Unit
) {
    var cantidadSeleccionada by remember { mutableIntStateOf(disponibles) }

    AlertDialog(
        onDismissRequest = onDismiss,
        confirmButton = {
            TextButton(
                enabled = cantidadSeleccionada > 0,
                onClick = { onConfirm(cantidadSeleccionada) }
            ) {
                Text("Confirmar", color = PrimaryColor, fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancelar", color = PrimaryColor)
            }
        },
        title = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "Asignación del lote",
                    style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                    textAlign = TextAlign.Center
                )
                Text(
                    text = loteNumber,
                    style = MaterialTheme.typography.bodyMedium,
                    color = PrimaryColor,
                    textAlign = TextAlign.Center
                )
            }
        },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Text(
                    text = "¿Cuántos BigBags de este lote asignarás a la comanda $comandaNumber?",
                    style = MaterialTheme.typography.bodyMedium,
                    textAlign = TextAlign.Center
                )

                // Selector de cantidad (+ / -)
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    FilledIconButton(
                        onClick = { if (cantidadSeleccionada > 1) cantidadSeleccionada-- },
                        colors = IconButtonDefaults.filledIconButtonColors(
                            containerColor = MaterialTheme.colorScheme.surfaceVariant
                        )
                    ) {
                        Text("-", fontSize = 24.sp, fontWeight = FontWeight.Bold)
                    }

                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.padding(horizontal = 24.dp)
                    ) {
                        Text(
                            text = cantidadSeleccionada.toString(),
                            style = MaterialTheme.typography.headlineLarge,
                            fontWeight = FontWeight.ExtraBold,
                            color = PrimaryColor
                        )
                        Text("BigBags", style = MaterialTheme.typography.labelSmall)
                    }

                    FilledIconButton(
                        onClick = { if (cantidadSeleccionada < disponibles) cantidadSeleccionada++ },
                        colors = IconButtonDefaults.filledIconButtonColors(
                            containerColor = MaterialTheme.colorScheme.surfaceVariant
                        )
                    ) {
                        Text("+", fontSize = 24.sp, fontWeight = FontWeight.Bold)
                    }
                }

                OutlinedButton(
                    onClick = { cantidadSeleccionada = disponibles },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(8.dp),
                    border = BorderStroke(1.dp, PrimaryColor.copy(alpha = 0.5f))
                ) {
                    Text("Asignar máximo disponible ($disponibles BB)", color = PrimaryColor)
                }

                // Panel de información de peso
                Surface(
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.05f),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    val pesoAprox = if (totalBB > 0) (totalWeightNumber / totalBB) * cantidadSeleccionada else 0.0
                    Column(modifier = Modifier.padding(12.dp)) {
                        Text("Peso estimado:", style = MaterialTheme.typography.labelSmall, color = Color.Gray)
                        Text(
                            text = "${formatWeight(pesoAprox)} Kg aprox.",
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "Referencia Comanda: $totalWeightComanda Kg totales",
                            style = MaterialTheme.typography.labelSmall
                        )
                    }
                }

                Text(
                    text = "Asignado por: $currentUserEmail",
                    style = MaterialTheme.typography.labelSmall,
                    color = Color.Gray
                )
            }
        },
        shape = RoundedCornerShape(20.dp),
        containerColor = MaterialTheme.colorScheme.surface
    )
}