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
    occupancyList: List<OccupancyInfo>, // 👈 Agregado para calcular stock disponible
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
    var showOccupancyDialog by remember { mutableStateOf(false) } // 👈 Nuevo estado

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

            // 1. CABECERA REESTRUCTURADA (Ahora con 5 botones)
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
                    // Reservado
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

                    // Observación
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

                    // Asignaciones (NUEVO)
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

                    // Certificado
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

                    // BigBags
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

            // 2. BLOQUE DE DETALLES
            Box(modifier = Modifier.fillMaxWidth()) {
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    DetailRow("Material", lote.description)
                    DetailRow("Fecha", formatInstant(lote.date))
                    DetailRow("Ubicación", lote.location)

                    // Fila de Stock con icono Inventory (NUEVA LÓGICA)
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("Stock Disponible", style = MaterialTheme.typography.bodyMedium, color = Color.Gray)
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = "$disponibles / $totalBB",
                                fontWeight = FontWeight.Bold,
                                color = if(disponibles > 0) PrimaryColor else Color.Red
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Icon(
                                imageVector = Icons.Default.Inventory,
                                contentDescription = null,
                                tint = if(disponibles > 0) PrimaryColor else Color.Red,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    }

                    DetailRow("Peso total", "${formatWeight(totalWeightNumber)} Kg", PrimaryColor)
                }

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
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(horizontal = 6.dp, vertical = 2.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center
                        ) {
                            Text(
                                text = "RESERVADO",
                                fontWeight = FontWeight.Bold,
                                color = Color.White,
                                fontSize = 12.sp,
                                maxLines = 1,
                                overflow = TextOverflow.Clip
                            )
                            Text(
                                text = lote.booked.cliNombre,
                                fontWeight = FontWeight.Bold,
                                color = Color.White,
                                fontSize = 12.sp,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                            )
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
// --- Diálogo de reservas con selección de cliente (Estructura Blindada y Lógica de Guardado Completa) ---
    if (showReservedDialog) {
        var selectedCliente by remember { mutableStateOf(lote.booked) }
        var showClientesDialog by remember { mutableStateOf(false) }
        val userToSave = currentUserEmail
        var clientesList by remember { mutableStateOf<List<Cliente>?>(null) }

        var linkedComanda by remember { mutableStateOf<Comanda?>(null) }
        var isComandaLoading by remember { mutableStateOf(false) }

        // Estado para las observaciones
        var currentBookedRemark by remember { mutableStateOf(lote.bookedRemark?.trim() ?: "") }

        // 🛡️ Lógica para detectar si el comentario ha cambiado
        val remarkChanged = currentBookedRemark.trim() != (lote.bookedRemark?.trim() ?: "")

        val comandaRepository = remember { getComandaRepository(databaseUrl) }

        val noOkCliente = Cliente(cliNombre = "NO OK")
        val errorColor = MaterialTheme.colorScheme.error
        val cardShape = RoundedCornerShape(12.dp)

        val isNoOkSelected = selectedCliente?.cliNombre == "NO OK"
        val isLoteReservedOrBlocked = lote.booked != null
        val isBloqueoClickable = !isLoteReservedOrBlocked || isNoOkSelected

        // 1. Cargar Clientes
        LaunchedEffect(Unit) {
            val allClients = clientRepository.getAllClientsOrderedByName()
            clientesList = allClients.filter { it.cliNombre != "NO OK" }
        }

        // 2. Cargar Comanda vinculada
        LaunchedEffect(lote.number) {
            if (isLoteReservedOrBlocked) {
                isComandaLoading = true
                try {
                    val comanda = comandaRepository.getComandaByLoteNumber(lote.number)

                    linkedComanda = if (comanda != null && !comanda.fueVendidoComanda) {
                        comanda
                    } else {
                        null
                    }

                } catch (e: Exception) {
                    linkedComanda = null
                } finally {
                    isComandaLoading = false
                }
            }
        }


        AlertDialog(
            onDismissRequest = { showReservedDialog = false },
            modifier = Modifier
                .width(360.dp) // Ancho Fijo
                .height(650.dp), // Altura Fija Total
            title = {
                Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("Reserva del lote", fontWeight = FontWeight.Bold, color = PrimaryColor)
                        Text(lote.number, fontWeight = FontWeight.Bold, color = PrimaryColor)
                    }
                }
            },
            text = {
                Column(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    lote.bookedByUser?.takeIf { it.isNotBlank() }?.let {
                        InfoCard(label = "Reservado por", value = it)
                    }

                    // --- 1. SELECCIÓN DE CLIENTE ---
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(56.dp)
                            .border(
                                width = 1.dp,
                                color = if (selectedCliente != null && !isNoOkSelected) PrimaryColor
                                else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f),
                                shape = cardShape
                            )
                            .clip(cardShape)
                            .clickable(enabled = !isLoteReservedOrBlocked) { showClientesDialog = true }
                            .padding(horizontal = 16.dp),
                        contentAlignment = Alignment.CenterStart
                    ) {
                        Text(
                            text = if (isNoOkSelected) "BLOQUEO ACTIVO" else (selectedCliente?.cliNombre ?: "Seleccione cliente"),
                            color = if (isNoOkSelected) errorColor else if (selectedCliente != null) PrimaryColor else TextSecondary
                        )
                    }

                    // --- 2. SECCIÓN DE COMANDA ASOCIADA (Altura Fija) ---
                    Text("Comanda vinculada", fontWeight = FontWeight.Bold)

                    Box(modifier = Modifier
                        .fillMaxWidth()
                        .height(150.dp)
                        .border(1.dp, MaterialTheme.colorScheme.outlineVariant, cardShape)
                        .padding(4.dp)) {

                        if (isComandaLoading) {
                            CircularProgressIndicator(modifier = Modifier.align(Alignment.Center).size(30.dp), color = PrimaryColor)
                        } else if (linkedComanda != null) {
                            ComandaLoteCard(comanda = linkedComanda!!, isSelected = true, onClick = { })
                        } else {
                            Text(
                                "Sin comanda asociada.",
                                modifier = Modifier.align(Alignment.Center).padding(16.dp),
                                color = TextSecondary,
                                textAlign = TextAlign.Center,
                                fontSize = 13.sp
                            )
                        }
                    }

                    // --- 3. OBSERVACIONES (Altura Fija Blindada) ---
                    OutlinedTextField(
                        value = currentBookedRemark,
                        onValueChange = { currentBookedRemark = it },
                        label = { Text("Observaciones de reserva") },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(130.dp),
                        singleLine = false,
                        shape = cardShape,
                        colors = TextFieldDefaults.outlinedTextFieldColors(
                            focusedBorderColor = PrimaryColor,
                            focusedLabelColor = PrimaryColor,
                            cursorColor = PrimaryColor
                        )
                    )

                    // --- 4. BLOQUEO INTERNO (Efectos Reforzados) ---
                    val isBlocked = isNoOkSelected
                    val bloqueoBgColor = if (isBlocked) errorColor.copy(alpha = 0.15f) else errorColor.copy(alpha = 0.05f)
                    val bloqueoStrokeColor = if (isBlocked) errorColor else errorColor.copy(alpha = 0.3f)
                    val bloqueoStrokeWidth = if (isBlocked) 2.dp else 1.dp

                    Surface(
                        shape = cardShape,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(54.dp)
                            .border(bloqueoStrokeWidth, bloqueoStrokeColor, cardShape)
                            .clip(cardShape)
                            .background(bloqueoBgColor)
                            .then(
                                if (isBloqueoClickable) Modifier.clickable {
                                    selectedCliente = if (isBlocked) null else noOkCliente
                                }
                                else Modifier
                            ),
                        shadowElevation = if (isBlocked) 4.dp else 0.dp
                    ) {
                        Row(
                            modifier = Modifier.fillMaxSize().padding(horizontal = 16.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Center
                        ) {
                            Icon(
                                imageVector = if (isBlocked) Icons.Default.Lock else Icons.Default.LockOpen,
                                contentDescription = null,
                                tint = bloqueoStrokeColor,
                                modifier = Modifier.size(24.dp)
                            )
                            Spacer(modifier = Modifier.width(12.dp))
                            Text(
                                text = if (isBlocked) "BLOQUEO ACTIVADO" else "BLOQUEO INTERNO",
                                color = bloqueoStrokeColor,
                                fontWeight = FontWeight.ExtraBold,
                                fontSize = 16.sp
                            )
                        }
                    }
                }
            },
            confirmButton = {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // BOTÓN ANULAR
                    if (isLoteReservedOrBlocked) {
                        TextButton(onClick = {
                            showReservedDialog = false
                            scope.launch {
                                var success = true
                                linkedComanda?.let {
                                    val comandaCleanSuccess = comandaRepository.updateComandaLoteNumber(it.idComanda, "")
                                    if (!comandaCleanSuccess) success = false
                                }
                                val loteCleanSuccess = loteRepository.updateLoteBooked(lote.id, null, null, null, null)
                                if (!loteCleanSuccess) success = false

                                if (success) {
                                    onRemarkUpdated(lote.copy(booked = null, dateBooked = null, bookedByUser = null, bookedRemark = null))
                                    snackbarHostState.showSnackbar("Reserva anulada")
                                }
                            }
                        }) { Text("Anular", color = errorColor) }
                    } else Spacer(modifier = Modifier.width(1.dp))

                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        TextButton(onClick = { showReservedDialog = false }) { Text("Cancelar", color = PrimaryColor) }

                        TextButton(
                            onClick = {
                                if (selectedCliente == null) return@TextButton
                                showReservedDialog = false
                                val remarkToSave = currentBookedRemark.trim().ifBlank { null }
                                scope.launch {
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
                                        snackbarHostState.showSnackbar("Cambios guardados")
                                    }
                                }
                            },
                            // 🔥 Habilitado si hay un cliente seleccionado Y (es nuevo bloqueo/reserva O el texto ha cambiado)
                            enabled = selectedCliente != null && (!isLoteReservedOrBlocked || remarkChanged)
                        ) {
                            Text(
                                "Guardar",
                                color = if (selectedCliente != null && (!isLoteReservedOrBlocked || remarkChanged)) PrimaryColor else TextSecondary,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }
        )

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

        // --- NUEVO: Diálogo de Asignaciones (Occupancy) ---
        if (showOccupancyDialog) {
            OccupancyDetailsDialog(
                loteNumber = lote.number,
                occupancyList = occupancyList,
                onDismiss = { showOccupancyDialog = false }
            )
        }
    }


}

