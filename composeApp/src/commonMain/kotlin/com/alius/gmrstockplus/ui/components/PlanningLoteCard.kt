package com.alius.gmrstockplus.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Assessment
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material.icons.automirrored.filled.ViewList
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.alius.gmrstockplus.core.utils.formatWeight
import com.alius.gmrstockplus.domain.model.BigBags
import com.alius.gmrstockplus.domain.model.Certificado
import com.alius.gmrstockplus.domain.model.CertificadoStatus
import com.alius.gmrstockplus.domain.model.Comanda
import com.alius.gmrstockplus.domain.model.LoteModel
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
    snackbarHostState: SnackbarHostState,

    // Se mantiene la firma con el booleano para la anulación condicional
    onAssignLote: (LoteModel, Boolean) -> Unit,
    onViewBigBags: (List<BigBags>) -> Unit,
    modifier: Modifier = Modifier
) {
    var showBigBagsDialog by remember { mutableStateOf(false) }
    var showCertificadoDialog by remember { mutableStateOf(false) }

    // ESTADOS PARA LOS DIÁLOGOS DE CONFIRMACIÓN
    var showConfirmAssignmentDialog by remember { mutableStateOf(false) }
    var showConfirmAnulationDialog by remember { mutableStateOf(false) }

    val totalWeightNumber = lote.totalWeight.toDoubleOrNull() ?: 0.0
    val comandaClientName = comanda.bookedClientComanda?.cliNombre
    val loteBookedClientName = lote.booked?.cliNombre

    val coroutineScope = rememberCoroutineScope()

    // --- Lógica Central de Estado ---
    val isAssignedToThisComanda = lote.number == comanda.numberLoteComanda
    val isReservedByOther = loteBookedClientName != null && loteBookedClientName != comandaClientName
    val isPreReservedBySameClient = loteBookedClientName == comandaClientName && !isAssignedToThisComanda

    val isAnotherLoteAlreadyAssigned = comanda.numberLoteComanda.isNotBlank() && !isAssignedToThisComanda

    // Lógica de Estado de Certificado
    val certColor = when (certificado?.status) {
        CertificadoStatus.ADVERTENCIA -> MaterialTheme.colorScheme.error
        CertificadoStatus.CORRECTO -> PrimaryColor
        else -> MaterialTheme.colorScheme.onSurfaceVariant
    }

    // 🔥 assignedBgColor se define pero NO se usa en CardDefaults.cardColors
    val assignedBgColor = PrimaryColor.copy(alpha = 0.08f)

    Card(
        modifier = modifier
            .fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 6.dp),
        border = BorderStroke(
            width = 2.dp,
            color = when {
                // Se mantiene el borde PrimaryColor cuando está asignado
                isAssignedToThisComanda -> PrimaryColor.copy(alpha = 0.8f)
                isReservedByOther -> ReservedColor.copy(alpha = 0.6f)
                else -> MaterialTheme.colorScheme.outline.copy(alpha = 0.2f)
            }
        ),
        colors = CardDefaults.cardColors(
            // 🔥 El fondo de la Card ahora es MaterialTheme.colorScheme.surface o ReservedColor.copy(alpha = 0.05f)
            containerColor = when {
                isReservedByOther -> ReservedColor.copy(alpha = 0.05f)
                // Se elimina la condición isAssignedToThisComanda
                else -> MaterialTheme.colorScheme.surface
            }
        )
    ) {
        Column(
            modifier = Modifier.padding(20.dp)
        ) {
            // --- 1. CABECERA: Lote, Icons y Estado de Reserva ---
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                // Lote Number
                Text(
                    text = lote.number,
                    style = MaterialTheme.typography.headlineMedium.copy(fontSize = 24.sp),
                    fontWeight = FontWeight.ExtraBold,
                    color = PrimaryColor,
                    maxLines = 1,
                    modifier = Modifier.weight(1f)
                )

                Row(horizontalArrangement = Arrangement.End) {
                    // Certificado
                    IconButton(
                        onClick = { showCertificadoDialog = true },
                        modifier = Modifier.size(32.dp)
                    ) {
                        Icon(
                            Icons.Default.Assessment,
                            contentDescription = "Ver certificado",
                            tint = certColor
                        )
                    }
                    // BigBags
                    IconButton(
                        onClick = { showBigBagsDialog = true },
                        modifier = Modifier.size(32.dp)
                    ) {
                        Icon(
                            Icons.AutoMirrored.Filled.ViewList,
                            contentDescription = "Ver BigBags",
                            tint = PrimaryColor
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))
            Divider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.2f), thickness = 1.dp)
            Spacer(modifier = Modifier.height(12.dp))

            // --- 2. DETALLES BÁSICOS ---
            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                DetailRow("Ubicación", lote.location)
                DetailRow("BigBags", lote.count.toString())
                DetailRow("Peso total", "${formatWeight(totalWeightNumber)} Kg", PrimaryColor)
            }

            // --- 3. ESTADO DE RESERVA / ASIGNACIÓN ---
            Spacer(modifier = Modifier.height(12.dp))

            when {
                isAssignedToThisComanda -> {
                    // ASIGNADO: Transparencia media
                    // Se mantiene el AssignmentBadge para indicar la asignación
                    AssignmentBadge(text = "ASIGNADO A ESTA COMANDA", color = PrimaryColor, alpha = 0.6f)
                }
                isReservedByOther -> {
                    // RESERVADO POR OTRO: Color de Reserva estándar
                    AssignmentBadge(text = "RESERVADO por ${loteBookedClientName!!}", color = ReservedColor)
                    Spacer(modifier = Modifier.height(8.dp))
                }
                isPreReservedBySameClient -> {
                    // PRE-RESERVADO: Muy sutil (0.2f)
                    AssignmentBadge(text = "PRE-RESERVADO para ${loteBookedClientName!!}", color = PrimaryColor, alpha = 0.2f)
                    Spacer(modifier = Modifier.height(8.dp))
                }
                else -> {
                    // LOTE DISPONIBLE: Muy sutil (0.2f)
                    AssignmentBadge(text = "LOTE DISPONIBLE", color = PrimaryColor, alpha = 0.2f)
                }
            }

            // --- 4. BOTÓN DE ACCIÓN (Maneja la apertura del diálogo) ---
            Spacer(modifier = Modifier.height(16.dp))
            Button(
                // 🔥 onClick modificado para manejar los diálogos de confirmación
                onClick = {
                    if (isAssignedToThisComanda) {
                        showConfirmAnulationDialog = true // Pide confirmación para ANULAR
                    } else if (!isReservedByOther && !isAnotherLoteAlreadyAssigned) {
                        showConfirmAssignmentDialog = true // Pide confirmación para ASIGNAR/CONFIRMAR
                    } else if (isAnotherLoteAlreadyAssigned) {
                        // Mensaje de Snackbar si ya hay otro asignado
                        coroutineScope.launch {
                            snackbarHostState.showSnackbar(
                                "Ya tiene el lote ${comanda.numberLoteComanda} asignado. Anule ese primero.",
                                withDismissAction = true
                            )
                        }
                    }
                },
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(
                    containerColor = when {
                        isAssignedToThisComanda -> MaterialTheme.colorScheme.error
                        isReservedByOther || isAnotherLoteAlreadyAssigned -> MaterialTheme.colorScheme.onSurface.copy(alpha = 0.12f)
                        else -> PrimaryColor // PrimaryColor sólido para la acción
                    },
                    contentColor = when {
                        isAssignedToThisComanda -> Color.White
                        isReservedByOther || isAnotherLoteAlreadyAssigned -> MaterialTheme.colorScheme.onSurfaceVariant
                        else -> Color.White
                    }
                ),
                enabled = when {
                    isAssignedToThisComanda -> true
                    isReservedByOther -> false
                    isAnotherLoteAlreadyAssigned -> false
                    else -> true
                }
            ) {
                Text(
                    text = when {
                        isAssignedToThisComanda -> "Anular asignación"
                        isAnotherLoteAlreadyAssigned -> "Ya hay otro lote asignado"
                        isPreReservedBySameClient -> "Confirmar asignación"
                        isReservedByOther -> "LOTE BLOQUEADO"
                        else -> "Asignar este lote"
                    },
                    fontWeight = FontWeight.Bold,
                    color = Color.White // Forzar color blanco para el texto del botón principal/error
                )
            }
        }
    }

    // --- Diálogo BigBags (sin cambios) ---
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

    // --- Diálogo Certificado (sin cambios) ---
    if (showCertificadoDialog) {
        Dialog(onDismissRequest = { showCertificadoDialog = false }) {
            Card(
                shape = RoundedCornerShape(20.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 8.dp),
                modifier = Modifier
                    .fillMaxWidth(0.95f)
                    .padding(16.dp)
            ) {
                Column(
                    modifier = Modifier
                        .background(MaterialTheme.colorScheme.surface)
                        .padding(20.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    val (icon, estadoText, estadoColor) = if (certificado != null) {
                        when (certificado.status) {
                            CertificadoStatus.ADVERTENCIA -> Triple(Icons.Default.Warning, "Advertencia", MaterialTheme.colorScheme.error)
                            CertificadoStatus.CORRECTO -> Triple(Icons.Default.CheckCircle, "Correcto", PrimaryColor)
                            else -> Triple(Icons.Default.Description, "Sin Datos", MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    } else {
                        Triple(Icons.Default.Description, "Sin Datos", MaterialTheme.colorScheme.onSurfaceVariant)
                    }

                    Icon(icon, contentDescription = estadoText, tint = estadoColor, modifier = Modifier.size(48.dp))
                    Text(
                        "Certificado de ${lote.number}",
                        color = PrimaryColor,
                        fontWeight = FontWeight.ExtraBold,
                        fontSize = 22.sp,
                        textAlign = TextAlign.Center
                    )

                    Divider(color = MaterialTheme.colorScheme.outlineVariant)

                    Column(modifier = Modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        if (certificado != null) {
                            certificado.parametros.forEach { parametro ->
                                Column(modifier = Modifier.fillMaxWidth()) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        modifier = Modifier.fillMaxWidth()
                                    ) {
                                        Text(
                                            parametro.descripcion,
                                            fontWeight = FontWeight.Bold,
                                            modifier = Modifier.weight(1f),
                                            maxLines = 1,
                                            overflow = TextOverflow.Ellipsis
                                        )
                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.End
                                        ) {
                                            if (parametro.warning) {
                                                Icon(
                                                    Icons.Default.Warning,
                                                    contentDescription = "Advertencia",
                                                    tint = MaterialTheme.colorScheme.error,
                                                    modifier = Modifier.size(18.dp)
                                                )
                                                Spacer(modifier = Modifier.width(4.dp))
                                            }
                                            Text(
                                                parametro.valor,
                                                color = if (parametro.warning) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurface,
                                                fontSize = 14.sp
                                            )
                                        }
                                    }

                                    val rangoTexto = parametro.rango?.let { rango ->
                                        if (rango.valorMin != null && rango.valorMax != null) {
                                            val min = if (rango.valorMin % 1.0 == 0.0) rango.valorMin.toInt() else rango.valorMin
                                            val max = if (rango.valorMax % 1.0 == 0.0) rango.valorMax.toInt() else rango.valorMax
                                            "Rango: ($min - $max ${parametro.unidad})"
                                        } else "Rango: N/A"
                                    } ?: "Rango: N/A"
                                    Text(rangoTexto, color = Color.Gray, fontSize = 12.sp, modifier = Modifier.padding(top = 4.dp))
                                }
                            }
                        } else {
                            // Mensaje cuando no existe certificado
                            Text(
                                "No se encontraron datos del certificado.",
                                color = Color.Gray,
                                fontSize = 14.sp,
                                textAlign = TextAlign.Center,
                                modifier = Modifier.fillMaxWidth()
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))
                    TextButton(onClick = { showCertificadoDialog = false }, modifier = Modifier.align(Alignment.End)) {
                        Text("Cerrar", color = PrimaryColor)
                    }
                }
            }
        }
    }


    // --- DIÁLOGOS DE CONFIRMACIÓN DE ASIGNACIÓN (Levemente modificado) ---

    if (showConfirmAssignmentDialog) {
        AlertDialog(
            onDismissRequest = { showConfirmAssignmentDialog = false },
            confirmButton = {
                TextButton(
                    onClick = {
                        // Se llama con 'false' porque la asignación/confirmación NUNCA anula la reserva (Opción 1)
                        onAssignLote(lote, false)
                        showConfirmAssignmentDialog = false
                    }
                ) {
                    Text("Confirmar", color = PrimaryColor, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showConfirmAssignmentDialog = false }) {
                    Text("Cancelar", color = PrimaryColor)
                }
            },
            title = { Text("Confirmar asignación", fontWeight = FontWeight.SemiBold) },
            text = {
                Text("¿Está seguro que desea asignar el lote ${lote.number} a la comanda ${comanda.numeroDeComanda}?")
            },
            shape = RoundedCornerShape(16.dp),
            containerColor = MaterialTheme.colorScheme.surfaceContainerHigh
        )
    }

    // --- DIÁLOGO DE CONFIRMACIÓN DE ANULACIÓN (IMPLEMENTACIÓN CONDICIONAL) ---

    if (showConfirmAnulationDialog) {
        // 🔥 Estado local para la selección del Radio Button
        var selectedAnulationOption by remember { mutableStateOf(1) } // 1: Mantener Reserva, 2: Anular Reserva

        AlertDialog(
            onDismissRequest = { showConfirmAnulationDialog = false },
            confirmButton = {
                TextButton(
                    onClick = {
                        // 🔥 Ejecuta la acción de anulación: True si seleccionó la Opción 2, False si seleccionó la Opción 1
                        val shouldClearBooking = selectedAnulationOption == 2
                        onAssignLote(lote, shouldClearBooking)
                        showConfirmAnulationDialog = false
                    }
                ) {
                    Text("Anular", color = MaterialTheme.colorScheme.error, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showConfirmAnulationDialog = false }) {
                    Text("Cancelar", color = PrimaryColor)
                }
            },
            title = { Text("Anular asignación del lote:  ${lote.number}", fontWeight = FontWeight.SemiBold) },
            text = {
                Column {
                    Text("Seleccione una opción para este lote reservado a (${comandaClientName}):")
                    Spacer(modifier = Modifier.height(16.dp))

                    // Opción 1: Desasignar y Mantener Reserva
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { selectedAnulationOption = 1 }
                            .padding(vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        RadioButton(
                            selected = selectedAnulationOption == 1,
                            onClick = { selectedAnulationOption = 1 },
                            colors = RadioButtonDefaults.colors(selectedColor = PrimaryColor)
                        )
                        Spacer(Modifier.width(8.dp))
                        Text(
                            "Desasignar lote: Mantener lote RESERVADO para ${comandaClientName}.",
                            fontWeight = FontWeight.SemiBold
                        )
                    }

                    // Opción 2: Desasignar y Anular Reserva
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { selectedAnulationOption = 2 }
                            .padding(vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        RadioButton(
                            selected = selectedAnulationOption == 2,
                            onClick = { selectedAnulationOption = 2 },
                            colors = RadioButtonDefaults.colors(selectedColor = PrimaryColor)
                        )
                        Spacer(Modifier.width(8.dp))
                        Text(
                            "Desasignar lote y anular reserva: El lote pasa a DISPONIBLE.",
                            fontWeight = FontWeight.SemiBold,
                        )
                    }
                }
            },
            shape = RoundedCornerShape(16.dp),
            containerColor = MaterialTheme.colorScheme.surfaceContainerHigh
        )
    }
}