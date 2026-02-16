package com.alius.gmrstockplus.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Assessment
import androidx.compose.material.icons.filled.Group
import androidx.compose.material.icons.automirrored.filled.ViewList
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.alius.gmrstockplus.core.utils.formatWeight
import com.alius.gmrstockplus.domain.model.BigBags
import com.alius.gmrstockplus.domain.model.Certificado
import com.alius.gmrstockplus.domain.model.CertificadoStatus
import com.alius.gmrstockplus.domain.model.Comanda
import com.alius.gmrstockplus.domain.model.LoteModel
import com.alius.gmrstockplus.domain.model.OccupancyInfo
import com.alius.gmrstockplus.ui.theme.PrimaryColor
import com.alius.gmrstockplus.ui.theme.ReservedColor
import kotlinx.coroutines.launch

@Composable
fun AssignmentBadge(text: String, color: Color, alpha: Float = 0.8f) {
    Surface(
        color = color.copy(alpha = alpha),
        shape = RoundedCornerShape(8.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Text(
            text = text,
            fontWeight = FontWeight.SemiBold,
            color = Color.White,
            fontSize = 14.sp,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(vertical = 8.dp)
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PlanningLoteCard(
    lote: LoteModel,
    comanda: Comanda,
    certificado: Certificado?,
    occupancyList: List<OccupancyInfo>,
    currentUserEmail: String,
    snackbarHostState: SnackbarHostState,
    onAssignLote: (LoteModel, Boolean, Int) -> Unit,
    onViewBigBags: (List<BigBags>) -> Unit,
    modifier: Modifier = Modifier
) {
    var showBigBagsDialog by remember { mutableStateOf(false) }
    var showCertificadoDialog by remember { mutableStateOf(false) }
    var showOccupancyDialog by remember { mutableStateOf(false) }

    // ESTADOS PARA LOS DIÁLOGOS DE CONFIRMACIÓN
    var showConfirmAssignmentDialog by remember { mutableStateOf(false) }
    var showConfirmAnulationDialog by remember { mutableStateOf(false) }

    val totalWeightNumber = lote.totalWeight.toDoubleOrNull() ?: 0.0
    val comandaClientName = comanda.bookedClientComanda?.cliNombre
    val loteBookedClientName = lote.booked?.cliNombre

    // --- CONTROL DE VENTA GLOBAL ---
    val isComandaVendida = comanda.fueVendidoComanda

    // --- CÁLCULOS DE STOCK ---
    val totalBB = lote.count.toIntOrNull() ?: 0
    val totalAsignado = occupancyList.sumOf { it.cantidad }
    val disponibles = (totalBB - totalAsignado).coerceAtLeast(0)

    // --- Lógica Central de Estado por Línea ---
    // Buscamos si este lote específico está en la lista de asignaciones de la comanda
    val asignacionEnComanda = remember(comanda.listaAsignaciones, lote.number) {
        comanda.listaAsignaciones.find { it.numeroLote == lote.number }
    }

    val isAssignedToThisComanda = asignacionEnComanda != null
    val isLoteYaVendidoEnComanda = asignacionEnComanda?.fueVendido ?: false

    val isReservedByOther = loteBookedClientName != null && loteBookedClientName != comandaClientName

    // Lógica de Estado de Certificado
    val certColor = when (certificado?.status) {
        CertificadoStatus.ADVERTENCIA -> MaterialTheme.colorScheme.error
        CertificadoStatus.CORRECTO -> PrimaryColor
        else -> MaterialTheme.colorScheme.onSurfaceVariant
    }

    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 6.dp),
        border = BorderStroke(
            width = 2.dp,
            color = when {
                isLoteYaVendidoEnComanda -> Color(0xFF4CAF50).copy(alpha = 0.8f) // Verde si ya se vendió
                isAssignedToThisComanda -> PrimaryColor.copy(alpha = 0.8f)
                isReservedByOther -> ReservedColor.copy(alpha = 0.6f)
                else -> MaterialTheme.colorScheme.outline.copy(alpha = 0.2f)
            }
        ),
        colors = CardDefaults.cardColors(
            containerColor = if (isReservedByOther) ReservedColor.copy(alpha = 0.05f) else MaterialTheme.colorScheme.surface
        )
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            // --- 1. CABECERA ---
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = lote.number,
                    style = MaterialTheme.typography.headlineMedium.copy(fontSize = 24.sp),
                    fontWeight = FontWeight.ExtraBold,
                    color = PrimaryColor,
                    maxLines = 1,
                    modifier = Modifier.weight(1f)
                )
                Row(horizontalArrangement = Arrangement.End) {
                    IconButton(onClick = { showOccupancyDialog = true }, modifier = Modifier.size(32.dp)) {
                        Icon(
                            Icons.Default.Group,
                            contentDescription = "Ocupación",
                            tint = if (occupancyList.isNotEmpty()) PrimaryColor else Color.Gray
                        )
                    }
                    IconButton(onClick = { showCertificadoDialog = true }, modifier = Modifier.size(32.dp)) {
                        Icon(Icons.Default.Assessment, contentDescription = "Certificado", tint = certColor)
                    }
                    IconButton(onClick = { showBigBagsDialog = true }, modifier = Modifier.size(32.dp)) {
                        Icon(Icons.AutoMirrored.Filled.ViewList, contentDescription = "BigBags", tint = PrimaryColor)
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))
            HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.2f), thickness = 1.dp)
            Spacer(modifier = Modifier.height(12.dp))

            // --- 2. DETALLES BÁSICOS Y STOCK ---
            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                DetailRow("Ubicación", lote.location)

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text("Stock Disponible", style = MaterialTheme.typography.bodyMedium, color = Color.Gray)
                    Text("$disponibles / $totalBB BB", fontWeight = FontWeight.Bold, color = if(disponibles > 0) PrimaryColor else Color.Red)
                }

                DetailRow("Peso total", "${formatWeight(totalWeightNumber)} Kg", PrimaryColor)
            }

            Spacer(modifier = Modifier.height(12.dp))

            // --- 4. ESTADO DE RESERVA / ASIGNACIÓN ---
            when {
                isComandaVendida -> {
                    AssignmentBadge(text = "COMANDA VENDIDA", color = Color.Gray, alpha = 0.5f)
                }
                isLoteYaVendidoEnComanda -> {
                    AssignmentBadge(text = "LOTE ENTREGADO", color = Color(0xFF4CAF50), alpha = 0.8f)
                }
                isAssignedToThisComanda -> {
                    AssignmentBadge(text = "ASIGNADO A ESTA COMANDA", color = PrimaryColor, alpha = 0.6f)
                }
                isReservedByOther -> {
                    AssignmentBadge(text = "RESERVADO por ${loteBookedClientName!!}", color = ReservedColor)
                }
                else -> {
                    val badgeText = if (disponibles > 0) "LOTE DISPONIBLE" else "LOTE AGOTADO"
                    val badgeColor = if (disponibles > 0) PrimaryColor else Color.Gray
                    AssignmentBadge(text = badgeText, color = badgeColor, alpha = 0.2f)
                }
            }

            // --- 5. BOTÓN DE ACCIÓN CON BLOQUEO POR VENTA ---
            Spacer(modifier = Modifier.height(16.dp))
            Button(
                onClick = {
                    if (isAssignedToThisComanda) {
                        showConfirmAnulationDialog = true
                    } else if (!isReservedByOther && disponibles > 0) {
                        showConfirmAssignmentDialog = true
                    }
                },
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(
                    containerColor = when {
                        isComandaVendida || isLoteYaVendidoEnComanda -> Color.LightGray.copy(alpha = 0.4f)
                        isAssignedToThisComanda -> MaterialTheme.colorScheme.error
                        isReservedByOther || disponibles <= 0 -> MaterialTheme.colorScheme.onSurface.copy(alpha = 0.12f)
                        else -> PrimaryColor
                    },
                    contentColor = if (isComandaVendida || isLoteYaVendidoEnComanda) Color.DarkGray else Color.White
                ),
                // Bloqueo total si la comanda general está vendida O si este lote específico ya se entregó
                enabled = !isComandaVendida && !isLoteYaVendidoEnComanda && (isAssignedToThisComanda || (!isReservedByOther && disponibles > 0))
            ) {
                Text(
                    text = when {
                        isComandaVendida -> "LECTURA: VENTA FINALIZADA"
                        isLoteYaVendidoEnComanda -> "CARGA COMPLETADA"
                        isAssignedToThisComanda -> "Anular asignación"
                        disponibles <= 0 -> "SIN STOCK DISPONIBLE"
                        isReservedByOther -> "LOTE BLOQUEADO"
                        else -> "Asignar este lote"
                    },
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }

    // --- DIÁLOGOS ---
    if (showOccupancyDialog) {
        OccupancyDetailsDialog(
            loteNumber = lote.number,
            occupancyList = occupancyList,
            onDismiss = { showOccupancyDialog = false }
        )
    }

    if (showBigBagsDialog) {
        AlertDialog(
            onDismissRequest = { showBigBagsDialog = false },
            confirmButton = {
                TextButton(onClick = { showBigBagsDialog = false }) { Text("Cerrar", color = PrimaryColor) }
            },
            title = {
                Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                    Text("Lista de BigBags", color = PrimaryColor, fontWeight = FontWeight.Bold, fontSize = 20.sp)
                }
            },
            text = { BigBagsDialogContent(bigBags = lote.bigBag) }
        )
    }

    if (showCertificadoDialog) {
        Dialog(onDismissRequest = { showCertificadoDialog = false }) {
            Card(
                shape = RoundedCornerShape(20.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 8.dp),
                modifier = Modifier.fillMaxWidth(0.95f)
            ) {
                CertificadoDialogContent(
                    loteNumber = lote.number,
                    certificado = certificado,
                    onClose = { showCertificadoDialog = false }
                )
            }
        }
    }

    if (showConfirmAssignmentDialog) {
        ConfirmAssignmentDialog(
            loteNumber = lote.number,
            comandaNumber = comanda.numeroDeComanda.toString(),
            disponibles = disponibles,
            totalBB = totalBB,
            totalWeightNumber = totalWeightNumber,
            totalWeightComanda = comanda.totalWeightComanda,
            currentUserEmail = currentUserEmail,
            onConfirm = { cantidad ->
                onAssignLote(lote, false, cantidad)
                showConfirmAssignmentDialog = false
            },
            onDismiss = { showConfirmAssignmentDialog = false }
        )
    }

    if (showConfirmAnulationDialog) {
        ConfirmAnulationDialog(
            loteNumber = lote.number,
            comandaNumber = comanda.numeroDeComanda.toString(),
            currentUserEmail = currentUserEmail,
            onConfirm = {
                onAssignLote(lote, false, 0)
                showConfirmAnulationDialog = false
            },
            onDismiss = { showConfirmAnulationDialog = false }
        )
    }
}