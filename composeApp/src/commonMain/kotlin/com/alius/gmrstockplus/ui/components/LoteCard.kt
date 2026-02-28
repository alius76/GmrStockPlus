package com.alius.gmrstockplus.ui.components

import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.NoteAdd
import androidx.compose.material.icons.automirrored.filled.ViewList
import androidx.compose.material.icons.filled.*
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
import com.alius.gmrstockplus.domain.model.*
import com.alius.gmrstockplus.ui.theme.PrimaryColor
import com.alius.gmrstockplus.ui.theme.ReservedColor
import com.alius.gmrstockplus.core.utils.formatInstant
import com.alius.gmrstockplus.core.utils.formatWeight
import com.alius.gmrstockplus.data.ClientRepository
import com.alius.gmrstockplus.data.getLoteRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import kotlin.time.ExperimentalTime
import com.alius.gmrstockplus.data.getComandaRepository
import com.alius.gmrstockplus.domain.model.Comanda
import androidx.compose.ui.draw.clip
import com.alius.gmrstockplus.ui.theme.TextSecondary

@OptIn(ExperimentalMaterial3Api::class, ExperimentalTime::class)
@Composable
fun LoteCard(
    lote: LoteModel,
    certificado: Certificado?,
    certificadoIconColor: Color,
    occupancyList: List<OccupancyInfo>,
    modifier: Modifier = Modifier,
    scope: CoroutineScope,
    snackbarHostState: SnackbarHostState,
    onViewBigBags: (List<BigBags>) -> Unit,
    databaseUrl: String,
    onRemarkUpdated: (LoteModel) -> Unit,
    clientRepository: ClientRepository,
    currentUserEmail: String
) {
    var showBigBagsDialog by remember { mutableStateOf(false) }
    var showCertificadoDialog by remember { mutableStateOf(false) }
    var showReservedDialog by remember { mutableStateOf(false) }
    var showRemarkDialog by remember { mutableStateOf(false) }
    var showAddRemarkDialog by remember { mutableStateOf(false) }
    var showOccupancyDialog by remember { mutableStateOf(false) }

    val totalWeightNumber = lote.totalWeight.toDoubleOrNull() ?: 0.0

    // --- CÁLCULOS DE STOCK DINÁMICO ---
    val totalBB = lote.count.toIntOrNull() ?: 0
    val totalAsignado = occupancyList.sumOf { it.cantidad }
    val disponibles = (totalBB - totalAsignado).coerceAtLeast(0)

    var currentRemarkText by remember { mutableStateOf(lote.remark) }
    var currentBookedRemark by remember { mutableStateOf(lote.bookedRemark ?: "") }

    val loteRepository = remember { getLoteRepository(databaseUrl) }
    val hasRemark = lote.remark.isNotBlank()

    // -------------------- CARD --------------------
    Card(
        modifier = modifier.wrapContentHeight(),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 6.dp),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.2f)),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(
            modifier = Modifier
                .padding(20.dp)
                .animateContentSize()
        ) {

            // 1. CABECERA CON BOTONES REORGANIZADOS
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = lote.number,
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.ExtraBold,
                    color = PrimaryColor,
                    textAlign = TextAlign.Center,
                    maxLines = 1
                )

                Spacer(modifier = Modifier.height(12.dp))

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceEvenly,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    // 1. Reservado
                    IconButton(
                        onClick = {
                            currentBookedRemark = lote.bookedRemark ?: ""
                            showReservedDialog = true
                        },
                        modifier = Modifier.size(32.dp)
                    ) {
                        Icon(
                            Icons.Default.Lock,
                            contentDescription = "Ver reservado",
                            tint = PrimaryColor,
                            modifier = Modifier.fillMaxSize()
                        )
                    }

                    // 2. Asignaciones (INTERCAMBIADO A POSICIÓN 2)
                    IconButton(
                        onClick = { showOccupancyDialog = true },
                        modifier = Modifier.size(32.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.DatasetLinked,
                            contentDescription = "Ver asignaciones",
                            tint = if (occupancyList.isNotEmpty()) PrimaryColor else Color.Gray,
                            modifier = Modifier.fillMaxSize()
                        )
                    }

                    // 3. Observación (INTERCAMBIADO A POSICIÓN 3)
                    IconButton(
                        onClick = {
                            currentRemarkText = lote.remark
                            if (hasRemark) showRemarkDialog = true
                            else showAddRemarkDialog = true
                        },
                        modifier = Modifier.size(32.dp)
                    ) {
                        Icon(
                            imageVector = if (hasRemark) Icons.Default.Description else Icons.AutoMirrored.Filled.NoteAdd,
                            contentDescription = if (hasRemark) "Ver/Editar observación" else "Añadir observación",
                            tint = if (hasRemark) PrimaryColor else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
                            modifier = Modifier.fillMaxSize()
                        )
                    }

                    // 4. Certificado
                    IconButton(
                        onClick = { showCertificadoDialog = true },
                        modifier = Modifier.size(32.dp)
                    ) {
                        Icon(
                            Icons.Default.Assessment,
                            contentDescription = "Ver certificado",
                            tint = certificadoIconColor,
                            modifier = Modifier.fillMaxSize()
                        )
                    }

                    // 5. BigBags
                    IconButton(
                        onClick = { showBigBagsDialog = true },
                        modifier = Modifier.size(32.dp)
                    ) {
                        Icon(
                            Icons.AutoMirrored.Filled.ViewList,
                            contentDescription = "Ver BigBags",
                            tint = PrimaryColor,
                            modifier = Modifier.fillMaxSize()
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))
            HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.2f), thickness = 1.dp)
            Spacer(modifier = Modifier.height(12.dp))

            // 2. BLOQUE DE DETALLES REORDENADO
            Box(modifier = Modifier.fillMaxWidth()) {
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {

                    // --- STOCK DISPONIBLE (AHORA PRIMERO) ---
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("Stock Disponible", style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.SemiBold, color = Color.Gray)
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = "$disponibles / $totalBB",
                                fontWeight = FontWeight.ExtraBold,
                                color = if(disponibles > 0) PrimaryColor else ReservedColor,
                                fontSize = 16.sp
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Icon(
                                imageVector = Icons.Default.Inventory,
                                contentDescription = null,
                                tint = if(disponibles > 0) PrimaryColor else ReservedColor,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }

                    // --- RESTO DE CAMPOS ---
                    DetailRow("Material", lote.description)
                    DetailRow("Fecha", formatInstant(lote.date))
                    DetailRow("Ubicación", lote.location)
                    DetailRow("Peso total", "${formatWeight(totalWeightNumber)} Kg", PrimaryColor)
                }

                // Indicador visual de reserva
                if (lote.booked != null && lote.booked.cliNombre.isNotBlank()) {
                    Surface(
                        color = ReservedColor,
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .align(Alignment.BottomEnd)
                            .padding(start = 8.dp)
                            .width(90.dp)
                            .height(48.dp)
                    ) {
                        Column(
                            modifier = Modifier.fillMaxSize().padding(horizontal = 6.dp, vertical = 2.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center
                        ) {
                            Text("RESERVADO", fontWeight = FontWeight.Bold, color = Color.White, fontSize = 12.sp, maxLines = 1)
                            Text(lote.booked.cliNombre, fontWeight = FontWeight.Bold, color = Color.White, fontSize = 12.sp, maxLines = 1, overflow = TextOverflow.Ellipsis)
                        }
                    }
                }
            }
        }
    }

    // --- InfoCard auxiliar ---
    @Composable
    fun InfoCard(label: String, value: String) {
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
            ),
            shape = RoundedCornerShape(12.dp)
        ) {
            Column(modifier = Modifier.padding(12.dp)) {
                Text(label, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                Text(value, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f))
            }
        }
    }


    // --- Diálogo Observación General ---
    if (showRemarkDialog) {
        val isChanged = currentRemarkText.trim() != lote.remark.trim()
        AlertDialog(
            onDismissRequest = { showRemarkDialog = false },
            title = {
                Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("Observación del Lote", fontWeight = FontWeight.Bold, color = PrimaryColor, textAlign = TextAlign.Center)
                        Text(lote.number, fontWeight = FontWeight.ExtraBold, color = PrimaryColor, textAlign = TextAlign.Center)
                    }
                }
            },
            text = {
                OutlinedTextField(
                    value = currentRemarkText,
                    onValueChange = { currentRemarkText = it },
                    label = { Text("Editar observación") },
                    modifier = Modifier.fillMaxWidth().height(150.dp),
                    singleLine = false,
                    shape = RoundedCornerShape(12.dp),
                    colors = TextFieldDefaults.outlinedTextFieldColors(
                        focusedBorderColor = PrimaryColor,
                        focusedLabelColor = PrimaryColor,
                        cursorColor = PrimaryColor,
                    )
                )
            },
            confirmButton = {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    TextButton(
                        onClick = {
                            showRemarkDialog = false
                            scope.launch {
                                val success = loteRepository.updateLoteRemark(lote.id, "")
                                if (success) onRemarkUpdated(lote.copy(remark = ""))
                                snackbarHostState.showSnackbar(if (success) "Observación eliminada" else "Error al eliminar la observación")
                            }
                        },
                        enabled = lote.remark.isNotBlank()
                    ) { Text("Eliminar", color = MaterialTheme.colorScheme.error) }

                    Row(horizontalArrangement = Arrangement.End) {
                        TextButton(onClick = { showRemarkDialog = false }) { Text("Cerrar", color = PrimaryColor) }
                        Spacer(Modifier.width(8.dp))
                        TextButton(
                            onClick = {
                                showRemarkDialog = false
                                val remarkToSave = currentRemarkText.trim()
                                scope.launch {
                                    val success = loteRepository.updateLoteRemark(lote.id, remarkToSave)
                                    if (success) onRemarkUpdated(lote.copy(remark = remarkToSave))
                                    snackbarHostState.showSnackbar(if (success) "Observación actualizada" else "Error al actualizar la observación")
                                }
                            },
                            enabled = isChanged && currentRemarkText.isNotBlank()
                        ) { Text("Guardar", color = PrimaryColor) }
                    }
                }
            }
        )
    }

    // --- Diálogo Añadir Observación ---
    if (showAddRemarkDialog) {
        AlertDialog(
            onDismissRequest = { showAddRemarkDialog = false },
            title = {
                Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("Observación del Lote", fontWeight = FontWeight.Bold, color = PrimaryColor, textAlign = TextAlign.Center)
                        Text(lote.number, fontWeight = FontWeight.Bold, color = PrimaryColor, textAlign = TextAlign.Center)
                    }
                }
            },
            text = {
                OutlinedTextField(
                    value = currentRemarkText,
                    onValueChange = { currentRemarkText = it },
                    label = { Text("Escribe tu observación") },
                    modifier = Modifier.fillMaxWidth().height(150.dp),
                    singleLine = false,
                    shape = RoundedCornerShape(12.dp),
                    colors = TextFieldDefaults.outlinedTextFieldColors(
                        focusedBorderColor = PrimaryColor,
                        focusedLabelColor = PrimaryColor,
                        cursorColor = PrimaryColor,
                    )
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        showAddRemarkDialog = false
                        val remarkToSave = currentRemarkText.trim()
                        scope.launch {
                            val success = loteRepository.updateLoteRemark(lote.id, remarkToSave)
                            if (success) onRemarkUpdated(lote.copy(remark = remarkToSave))
                            snackbarHostState.showSnackbar(if (success) "Observación guardada" else "Error al guardar la observación")
                        }
                    },
                    enabled = currentRemarkText.isNotBlank()
                ) { Text("Guardar", color = PrimaryColor) }
            },
            dismissButton = {
                TextButton(onClick = { showAddRemarkDialog = false }) { Text("Cancelar", color = PrimaryColor) }
            }
        )
    }

    // 1. Añade el diálogo de Ocupación reutilizando tu componente
    if (showOccupancyDialog) {
        OccupancyDetailsDialog(
            loteNumber = lote.number,
            occupancyList = occupancyList,
            onDismiss = { showOccupancyDialog = false }
        )
    }

    // --- Diálogo BigBags ---
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

    // --- Diálogo Certificado ---
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


    // --- Diálogo de Reservas Profesional con Freno de Seguridad y Bloqueo de Interacción ---
    if (showReservedDialog) {
        var showConfirmNoOkDialog by remember { mutableStateOf(false) }
        var selectedCliente by remember { mutableStateOf(lote.booked) }
        var showClientesDialog by remember { mutableStateOf(false) }
        val userToSave = currentUserEmail
        var clientesList by remember { mutableStateOf<List<Cliente>?>(null) }

        val comandaRepository = remember { getComandaRepository(databaseUrl) }
        val noOkCliente = Cliente(cliNombre = "NO OK")
        val errorColor = MaterialTheme.colorScheme.error
        val cardShape = RoundedCornerShape(12.dp)

        // --- LÓGICA DE ESTADOS Y BLOQUEO DE BOTÓN ---
        val isNoOkInDatabase = lote.booked?.cliNombre == "NO OK"
        val isNoOkSelected = selectedCliente?.cliNombre == "NO OK"
        val isLoteReservedOrBlocked = lote.booked != null

        // REGLA: El botón de bloqueo solo es clickable si el lote NO está bloqueado ya en la DB.
        // Si ya está bloqueado, obligamos a usar "Anular" para rehabilitarlo.
        val isBloqueoClickable = !isNoOkInDatabase

        var currentBookedRemark by remember { mutableStateOf(lote.bookedRemark?.trim() ?: "") }

        val otrosClientesAsignados = occupancyList
            .map { it.cliente }
            .filter { it != selectedCliente?.cliNombre }
            .distinct()

        val hayConflicto = otrosClientesAsignados.isNotEmpty() && !isNoOkSelected
        val remarkChanged = currentBookedRemark.trim() != (lote.bookedRemark?.trim() ?: "")

        // --- FUNCIÓN CENTRALIZADA DE GUARDADO ---
        val ejecutarGuardadoLote = {
            scope.launch {
                if (isNoOkSelected) {
                    occupancyList.forEach { occ ->
                        comandaRepository.getComandaByNumber(occ.numeroComanda)?.let { cmd ->
                            comandaRepository.quitarAsignacionLote(cmd.idComanda, lote.number)
                        }
                    }
                }

                val remarkToSave = currentBookedRemark.trim().ifBlank { null }
                val success = loteRepository.updateLoteBooked(
                    lote.id,
                    selectedCliente,
                    lote.dateBooked ?: kotlinx.datetime.Clock.System.now(),
                    userToSave,
                    remarkToSave
                )

                if (success) {
                    onRemarkUpdated(lote.copy(
                        booked = selectedCliente,
                        bookedByUser = userToSave,
                        bookedRemark = remarkToSave
                    ))
                    snackbarHostState.showSnackbar(
                        if (isNoOkSelected) "Lote BLOQUEADO y asignaciones liberadas"
                        else "Reserva guardada correctamente"
                    )
                }
            }
        }

        LaunchedEffect(Unit) {
            val allClients = clientRepository.getAllClientsOrderedByName()
            clientesList = allClients.filter { it.cliNombre != "NO OK" }
        }

        AlertDialog(
            onDismissRequest = { showReservedDialog = false },
            modifier = Modifier.width(360.dp).wrapContentHeight(),
            title = {
                Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = "Reserva del lote",
                            style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                        )
                        Text(
                            text = lote.number,
                            style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                            color = PrimaryColor
                        )
                    }
                }
            },
            text = {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    lote.bookedByUser?.takeIf { it.isNotBlank() }?.let {
                        InfoCard(label = "Gestionado por", value = it)
                    }

                    if (occupancyList.isNotEmpty()) {
                        Surface(
                            color = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.4f),
                            shape = cardShape,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(modifier = Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.Info, contentDescription = null, tint = PrimaryColor, modifier = Modifier.size(20.dp))
                                Spacer(Modifier.width(8.dp))
                                Text(
                                    "Lote presente en ${occupancyList.size} asignación(es).",
                                    fontSize = 13.sp,
                                    color = MaterialTheme.colorScheme.onSecondaryContainer
                                )
                            }
                        }
                    }

                    // --- 1. SELECCIÓN DE CLIENTE ---
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(56.dp)
                            .border(
                                width = 1.dp,
                                color = if (hayConflicto) errorColor
                                else if (selectedCliente != null && !isNoOkSelected) PrimaryColor
                                else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f),
                                shape = cardShape
                            )
                            .clip(cardShape)
                            // Si está bloqueado por calidad, no permitimos cambiar de cliente
                            .clickable(enabled = !isLoteReservedOrBlocked) { showClientesDialog = true }
                            .padding(horizontal = 16.dp),
                        contentAlignment = Alignment.CenterStart
                    ) {
                        Text(
                            text = if (isNoOkSelected) "BLOQUEO CALIDAD" else (selectedCliente?.cliNombre ?: "Seleccione cliente"),
                            color = if (isNoOkSelected) errorColor else if (selectedCliente != null) PrimaryColor else TextSecondary
                        )
                    }

                    if (hayConflicto) {
                        Text(
                            "Ya asignado a: ${otrosClientesAsignados.joinToString()}. Libere las asignaciones antes de reservar.",
                            color = errorColor,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            lineHeight = 14.sp
                        )
                    }

                    // --- 2. OBSERVACIONES ---
                    OutlinedTextField(
                        value = currentBookedRemark,
                        onValueChange = { currentBookedRemark = it },
                        label = { Text("Observaciones de reserva") },
                        modifier = Modifier.fillMaxWidth().height(120.dp),
                        singleLine = false,
                        shape = cardShape,
                        colors = TextFieldDefaults.outlinedTextFieldColors(
                            focusedBorderColor = PrimaryColor,
                            focusedLabelColor = PrimaryColor,
                            cursorColor = PrimaryColor
                        )
                    )

                    // --- 3. BLOQUEO INTERNO (LÓGICA DE INTERACTIVIDAD) ---
                    val bloqueoBgColor = if (isNoOkSelected) errorColor.copy(alpha = 0.15f) else errorColor.copy(alpha = 0.05f)
                    val bloqueoStrokeColor = if (isNoOkSelected) errorColor else errorColor.copy(alpha = 0.3f)
                    val bloqueoStrokeWidth = if (isNoOkSelected) 2.dp else 1.dp

                    Surface(
                        shape = cardShape,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(54.dp)
                            .border(bloqueoStrokeWidth, bloqueoStrokeColor, cardShape)
                            .clip(cardShape)
                            .background(bloqueoBgColor)
                            .then(
                                // APLICA TU REGLA: Solo clickable si no estaba ya bloqueado en la DB
                                if (isBloqueoClickable) Modifier.clickable {
                                    selectedCliente = if (isNoOkSelected) null else noOkCliente
                                } else Modifier
                            ),
                        shadowElevation = if (isNoOkSelected) 4.dp else 0.dp
                    ) {
                        Row(
                            modifier = Modifier.fillMaxSize().padding(horizontal = 16.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Center
                        ) {
                            Icon(
                                imageVector = if (isNoOkSelected) Icons.Default.Lock else Icons.Default.LockOpen,
                                contentDescription = null,
                                tint = bloqueoStrokeColor,
                                modifier = Modifier.size(24.dp)
                            )
                            Spacer(modifier = Modifier.width(12.dp))
                            Text(
                                text = if (isNoOkSelected) "BLOQUEO ACTIVO" else "BLOQUEO INTERNO",
                                color = bloqueoStrokeColor,
                                fontWeight = FontWeight.ExtraBold,
                                fontSize = 16.sp
                            )
                        }
                    }

                    // Mensaje de ayuda si está bloqueado
                    if (!isBloqueoClickable && isNoOkInDatabase) {
                        Text(
                            text = "Para quitar el bloqueo activo use el botón 'Anular'",
                            style = MaterialTheme.typography.labelSmall,
                            color = errorColor,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }
            },
            confirmButton = {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    if (isLoteReservedOrBlocked) {
                        TextButton(onClick = {
                            showReservedDialog = false
                            scope.launch {
                                val success = loteRepository.updateLoteBooked(lote.id, null, null, null, null)
                                if (success) {
                                    onRemarkUpdated(lote.copy(booked = null, dateBooked = null, bookedByUser = null, bookedRemark = null))
                                    snackbarHostState.showSnackbar("Estado de lote reseteado")
                                }
                            }
                        }) { Text("Anular", color = errorColor) }
                    } else Spacer(modifier = Modifier.width(1.dp))

                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        TextButton(onClick = { showReservedDialog = false }) {
                            Text("Cancelar", color = PrimaryColor)
                        }

                        TextButton(
                            onClick = {
                                if (selectedCliente == null) return@TextButton

                                // Freno de seguridad: solo si el bloqueo es NUEVO y hay ocupación
                                if (isNoOkSelected && occupancyList.isNotEmpty() && !isNoOkInDatabase) {
                                    showConfirmNoOkDialog = true
                                } else {
                                    showReservedDialog = false
                                    ejecutarGuardadoLote()
                                }
                            },
                            enabled = selectedCliente != null && !hayConflicto &&
                                    (!isLoteReservedOrBlocked || remarkChanged || (isNoOkSelected && !isNoOkInDatabase))
                        ) {
                            val isEnabled = selectedCliente != null && !hayConflicto &&
                                    (!isLoteReservedOrBlocked || remarkChanged || (isNoOkSelected && !isNoOkInDatabase))
                            Text(
                                "Guardar",
                                color = if (isEnabled) PrimaryColor else TextSecondary,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }
        )

        // --- DIÁLOGO DE ADVERTENCIA
        if (showConfirmNoOkDialog) {
            AlertDialog(
                onDismissRequest = { showConfirmNoOkDialog = false },
                confirmButton = {
                    TextButton(onClick = {
                        showConfirmNoOkDialog = false
                        showReservedDialog = false
                        ejecutarGuardadoLote()
                    }) {
                        Text(
                            text = "Confirmar",
                            color = MaterialTheme.colorScheme.error,
                            fontWeight = FontWeight.Bold
                        )
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showConfirmNoOkDialog = false }) {
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
                        Text(
                            text = "Bloqueo de calidad",
                            style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold)
                        )
                    }
                },
                text = {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text("El lote se marcará como NO OK. Esto desvinculará automáticamente todas sus asignaciones actuales.")

                        Surface(
                            color = MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.2f),
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(modifier = Modifier.padding(12.dp)) {
                                Text("Lote: ${lote.number}", fontWeight = FontWeight.Bold)
                                Text("Asignaciones afectadas: ${occupancyList.size}", color = MaterialTheme.colorScheme.error, fontWeight = FontWeight.SemiBold)
                                Spacer(Modifier.height(4.dp))
                                Text(
                                    text = "Resultado: El stock quedará bloqueado y se eliminará de las comandas vinculadas para evitar su carga.",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }

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
                                text = "Solicitado por: $currentUserEmail",
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

        if (showClientesDialog) {
            ClientesSelectedDialogContent(
                clients = clientesList ?: emptyList(),
                currentSelectedClient = selectedCliente,
                showAllOption = false,
                onDismiss = { showClientesDialog = false },
                onConfirm = { cliente ->
                    selectedCliente = cliente
                    showClientesDialog = false
                }
            )
        }
    }

        // --- NUEVO: Diálogo de Asignaciones (Occupancy) ---
        if (showOccupancyDialog) {
            OccupancyDetailsDialog(
                loteNumber = lote.number,
                occupancyList = occupancyList,
                onDismiss = { showOccupancyDialog = false }
            )
        }
    }




