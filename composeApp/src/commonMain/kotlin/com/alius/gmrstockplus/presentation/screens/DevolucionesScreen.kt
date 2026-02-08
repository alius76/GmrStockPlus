package com.alius.gmrstockplus.presentation.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.navigator.LocalNavigator
import cafe.adriel.voyager.navigator.currentOrThrow
import com.alius.gmrstockplus.core.utils.formatWeight
import com.alius.gmrstockplus.data.getDevolucionRepository
import com.alius.gmrstockplus.data.getDevolucionesRepository
import com.alius.gmrstockplus.data.getVentaRepository
import com.alius.gmrstockplus.data.getHistorialRepository
import com.alius.gmrstockplus.domain.model.BigBags
import com.alius.gmrstockplus.domain.model.Devolucion
import com.alius.gmrstockplus.domain.model.DevolucionBigbag
import com.alius.gmrstockplus.domain.model.LoteModel
import com.alius.gmrstockplus.ui.components.BigBagSeleccionableItem
import com.alius.gmrstockplus.ui.theme.BackgroundColor
import com.alius.gmrstockplus.ui.theme.PrimaryColor
import kotlinx.coroutines.launch
import kotlinx.datetime.Clock
import kotlinx.datetime.Instant


@OptIn(ExperimentalMaterial3Api::class)
class DevolucionesScreen(private val plantId: String) : Screen {

    @Composable
    override fun Content() {
        val navigator = LocalNavigator.currentOrThrow
        val coroutineScope = rememberCoroutineScope()

        val devolucionRepository = remember(plantId) { getDevolucionRepository(plantId) }
        val devolucionesRepository = remember(plantId) { getDevolucionesRepository(plantId) }
        val ventaRepository = remember(plantId) { getVentaRepository(plantId) }
        val historialRepository = remember(plantId) { getHistorialRepository(plantId) }

        // --- Estados ---
        var numeroLote by remember { mutableStateOf("") }
        var clientes by remember { mutableStateOf<List<String>>(emptyList()) }
        var clienteSeleccionado by remember { mutableStateOf<String?>(null) }
        var bigBagsFiltrados by remember { mutableStateOf<List<BigBags>>(emptyList()) }

        var loteBuscado by remember { mutableStateOf<LoteModel?>(null) }
        var loteArchivadoTemporal by remember { mutableStateOf<LoteModel?>(null) }

        var isLoading by remember { mutableStateOf(false) }
        var isFirstLoad by remember { mutableStateOf(true) } // Estado para controlar la lupa inicial
        var errorMessage by remember { mutableStateOf<String?>(null) }
        var ultimoClientePorBb by remember { mutableStateOf<Map<String, String>>(emptyMap()) }
        var bigBagsSeleccionados by remember { mutableStateOf<Set<BigBags>>(emptySet()) }
        var showConfirmMultipleDialog by remember { mutableStateOf(false) }

        Box(modifier = Modifier.fillMaxSize().background(BackgroundColor)) {

            // --- HEADER FIJO ---
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(BackgroundColor.copy(alpha = 0.95f))
                    .padding(horizontal = 16.dp, vertical = 12.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    IconButton(onClick = { navigator.pop() }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Volver", tint = PrimaryColor)
                    }

                    Column(modifier = Modifier.padding(start = 8.dp).weight(1f)) {
                        Text(
                            text = "Devoluciones",
                            fontSize = 26.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.secondary
                        )
                        Text(
                            text = "Seleccione lote para devolver",
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Medium),
                            color = Color.Gray,
                            modifier = Modifier.padding(top = 2.dp)
                        )
                    }

                    IconButton(onClick = { navigator.push(ListaDevolucionesScreen(plantId)) }) {
                        Icon(
                            Icons.Default.List,
                            contentDescription = "Historial de devoluciones",
                            tint = PrimaryColor,
                            modifier = Modifier.size(56.dp)
                        )
                    }
                }
            }

            // --- CUERPO DE LA PANTALLA ---
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(top = 100.dp) // Espacio para el header fijo
            ) {
                // --- SECCIÓN DE BÚSQUEDA (Siempre visible arriba) ---
                Column(modifier = Modifier.padding(horizontal = 16.dp)) {
                    OutlinedTextField(
                        value = numeroLote,
                        onValueChange = { input ->
                            if (input.all { it.isDigit() } || input.isEmpty()) {
                                numeroLote = input
                                if (input.isEmpty()) {
                                    isFirstLoad = true
                                    errorMessage = null
                                }
                                clientes = emptyList()
                                clienteSeleccionado = null
                                bigBagsFiltrados = emptyList()
                                loteBuscado = null
                                loteArchivadoTemporal = null
                                bigBagsSeleccionados = emptySet()
                            }
                        },
                        label = { Text("Número de lote") },
                        singleLine = true,
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth(),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        colors = TextFieldDefaults.outlinedTextFieldColors(
                            focusedBorderColor = PrimaryColor,
                            focusedLabelColor = PrimaryColor
                        )
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    Button(
                        onClick = {
                            coroutineScope.launch {
                                isLoading = true
                                isFirstLoad = false
                                errorMessage = null
                                clientes = emptyList()
                                clienteSeleccionado = null
                                bigBagsFiltrados = emptyList()
                                loteBuscado = null
                                loteArchivadoTemporal = null
                                bigBagsSeleccionados = emptySet()
                                ultimoClientePorBb = emptyMap()

                                try {
                                    var loteActivo = devolucionesRepository.getLoteByNumber(numeroLote)
                                    if (loteActivo == null) {
                                        val loteArchivado = historialRepository.getLoteHistorialByNumber(numeroLote)
                                        if (loteArchivado != null) {
                                            loteArchivadoTemporal = loteArchivado
                                            loteBuscado = loteArchivado
                                        } else {
                                            errorMessage = "No se encontró el lote."
                                            return@launch
                                        }
                                    } else {
                                        loteBuscado = loteActivo
                                    }

                                    val lote = loteBuscado!!
                                    val ventas = ventaRepository.obtenerVentasPorLote(numeroLote)

                                    if (ventas.isEmpty()) {
                                        errorMessage = "No se encontraron ventas para este lote."
                                        loteBuscado = null
                                        loteArchivadoTemporal = null
                                        return@launch
                                    }

                                    if (lote.bigBag.all { it.bbStatus != "o" }) {
                                        errorMessage = "Todos los BigBags del lote están en stock. No hay devoluciones posibles."
                                        clientes = emptyList()
                                        loteBuscado = null
                                        loteArchivadoTemporal = null
                                        return@launch
                                    }

                                    clientes = ventas.map { it.ventaCliente }.distinct()

                                    val bbUltimoClienteMap = mutableMapOf<String, String>()
                                    ventas.sortedBy { it.ventaFecha ?: Instant.DISTANT_PAST }
                                        .forEach { venta ->
                                            venta.ventaBigbags.forEach { bb ->
                                                bbUltimoClienteMap[bb.ventaBbNumber] = venta.ventaCliente
                                            }
                                        }
                                    ultimoClientePorBb = bbUltimoClienteMap

                                } catch (e: Exception) {
                                    errorMessage = "❌ Error en el proceso de búsqueda: ${e.message}"
                                    loteBuscado = null
                                    loteArchivadoTemporal = null
                                } finally {
                                    isLoading = false
                                }
                            }
                        },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = PrimaryColor),
                        enabled = numeroLote.isNotBlank() && !isLoading
                    ) {
                        Text("Buscar", color = MaterialTheme.colorScheme.onPrimary, fontWeight = FontWeight.SemiBold)
                    }
                }

                // --- SECCIÓN DE CONTENIDO (Centrada o Lista) ---
                Box(modifier = Modifier.fillMaxSize()) {
                    if (isLoading) {
                        CircularProgressIndicator(Modifier.align(Alignment.Center), color = PrimaryColor)
                    } else if (isFirstLoad) {
                        EmptyStateDevoluciones(isNoAction = true)
                    } else if (errorMessage != null) {
                        EmptyStateDevoluciones(isNoAction = false, customMessage = errorMessage)
                    } else {
                        // RESULTADOS
                        LazyColumn(
                            modifier = Modifier.fillMaxSize(),
                            contentPadding = PaddingValues(16.dp),
                            verticalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            if (clientes.isNotEmpty()) {
                                item {
                                    Text("Seleccione cliente:", modifier = Modifier.padding(start = 16.dp))
                                    ClienteDialog(
                                        clientes = clientes,
                                        clienteSeleccionado = clienteSeleccionado,
                                        onClienteSeleccionado = { cliente ->
                                            clienteSeleccionado = cliente
                                            bigBagsSeleccionados = emptySet()
                                            coroutineScope.launch {
                                                isLoading = true
                                                errorMessage = null
                                                try {
                                                    val bigbags = loteBuscado?.bigBag ?: emptyList()
                                                    bigBagsFiltrados = bigbags.filter { bb ->
                                                        bb.bbStatus == "o" && ultimoClientePorBb[bb.bbNumber] == cliente
                                                    }
                                                    if (bigBagsFiltrados.isEmpty()) {
                                                        errorMessage = "No hay BigBags disponibles para devolver para este cliente."
                                                    }
                                                } catch (e: Exception) {
                                                    errorMessage = "❌ Error al cargar BigBags: ${e.message}"
                                                } finally {
                                                    isLoading = false
                                                }
                                            }
                                        }
                                    )
                                }
                            }

                            if (clienteSeleccionado != null && bigBagsFiltrados.isNotEmpty() && loteBuscado != null) {
                                items(bigBagsFiltrados, key = { it.bbNumber }) { bigBag ->
                                    BigBagSeleccionableItem(
                                        bigBag = bigBag,
                                        isSelected = bigBagsSeleccionados.contains(bigBag),
                                        onToggleSelect = {
                                            bigBagsSeleccionados = if (bigBagsSeleccionados.contains(bigBag)) {
                                                bigBagsSeleccionados - bigBag
                                            } else {
                                                bigBagsSeleccionados + bigBag
                                            }
                                        }
                                    )
                                }

                                item {
                                    Button(
                                        onClick = { showConfirmMultipleDialog = true },
                                        enabled = bigBagsSeleccionados.isNotEmpty(),
                                        modifier = Modifier.fillMaxWidth().padding(vertical = 12.dp),
                                        shape = RoundedCornerShape(12.dp),
                                        colors = ButtonDefaults.buttonColors(containerColor = PrimaryColor)
                                    ) {
                                        Text("Devolver ${bigBagsSeleccionados.size} BigBag(s)", color = MaterialTheme.colorScheme.onPrimary, fontWeight = FontWeight.Bold)
                                    }
                                }
                            }
                        }
                    }
                }
            }

            // --- Diálogo Confirmación Devolución ---
            if (showConfirmMultipleDialog && loteBuscado != null && clienteSeleccionado != null) {
                val selectedBigBags = bigBagsSeleccionados.toList()
                val totalWeightNumber = selectedBigBags.sumOf { it.bbWeight.toDoubleOrNull() ?: 0.0 }
                val formattedWeight = formatWeight(totalWeightNumber)

                AlertDialog(
                    onDismissRequest = { showConfirmMultipleDialog = false },
                    title = { Text("Confirmar Devolución", fontWeight = FontWeight.Bold, color = PrimaryColor) },
                    text = {
                        Column {
                            Text("Cliente: $clienteSeleccionado", fontWeight = FontWeight.SemiBold)
                            Text("Lote: ${loteBuscado!!.number}")
                            Text("Material: ${loteBuscado!!.description}")
                            Spacer(Modifier.height(8.dp))
                            Text("Cantidad de BigBags: ${selectedBigBags.size}", fontWeight = FontWeight.SemiBold)
                            Text("Peso total a devolver: $formattedWeight Kg")
                        }
                    },
                    confirmButton = {
                        TextButton(
                            onClick = {
                                showConfirmMultipleDialog = false
                                coroutineScope.launch {
                                    isLoading = true
                                    errorMessage = null
                                    var loteActualizadoEnStock: LoteModel? = null
                                    try {
                                        val loteParaActualizar = loteBuscado!!
                                        var loteArchivado = loteArchivadoTemporal

                                        if (loteArchivado != null) {
                                            loteArchivado = loteArchivado.copy(status = "s")
                                            val newStockId = historialRepository.agregarYLigaroLote(loteArchivado)
                                            if (newStockId != null) {
                                                loteActualizadoEnStock = loteParaActualizar.copy(id = newStockId, status = "s")
                                                val successDelete = historialRepository.eliminarLoteHistorial(loteArchivado.id)
                                                if (!successDelete) {
                                                    errorMessage = "⚠️ Lote copiado a Stock, pero falló la eliminación del registro de Historial."
                                                }
                                            } else {
                                                errorMessage = "❌ Error al copiar lote de Historial a Stock."
                                                isLoading = false
                                                return@launch
                                            }
                                        } else {
                                            loteActualizadoEnStock = loteParaActualizar
                                        }

                                        val loteActivo = loteActualizadoEnStock!!
                                        val devolucionBigbagsList = selectedBigBags.map { DevolucionBigbag(it.bbNumber, it.bbWeight) }
                                        val devolucion = Devolucion(
                                            devolucionCliente = clienteSeleccionado!!,
                                            devolucionLote = loteActivo.number,
                                            devolucionMaterial = loteActivo.description,
                                            devolucionFecha = Clock.System.now(), // SE RESPETA INSTANT
                                            devolucionPesoTotal = totalWeightNumber.toLong().toString(),
                                            devolucionBigbags = devolucionBigbagsList
                                        )
                                        val firestoreSuccess = devolucionRepository.agregarDevolucion(devolucion)

                                        if (firestoreSuccess) {
                                            val successUpdate = devolucionesRepository.devolverBigBags(
                                                loteNumber = loteActivo.number,
                                                bigBagNumbers = selectedBigBags.map { it.bbNumber }
                                            )

                                            if (successUpdate) {
                                                val updatedLote = devolucionesRepository.getLoteByNumber(loteActivo.number)
                                                if (updatedLote != null) {
                                                    loteBuscado = updatedLote
                                                    bigBagsFiltrados = updatedLote.bigBag.filter { bb ->
                                                        bb.bbStatus == "o" && ultimoClientePorBb[bb.bbNumber] == clienteSeleccionado
                                                    }
                                                    bigBagsSeleccionados = emptySet()
                                                    errorMessage = "✅ Devolución registrada correctamente."
                                                }
                                            }
                                        }
                                    } catch (e: Exception) {
                                        errorMessage = "❌ Error: ${e.message}"
                                    } finally {
                                        isLoading = false
                                        loteArchivadoTemporal = null
                                    }
                                }
                            },
                            colors = ButtonDefaults.textButtonColors(contentColor = PrimaryColor)
                        ) {
                            Text("Confirmar", fontWeight = FontWeight.SemiBold)
                        }
                    },
                    dismissButton = {
                        TextButton(onClick = { showConfirmMultipleDialog = false }) {
                            Text("Cancelar", fontWeight = FontWeight.SemiBold, color = PrimaryColor)
                        }
                    }
                )
            }
        }
    }
}

// --- COMPONENTES AUXILIARES ---

@Composable
fun EmptyStateDevoluciones(isNoAction: Boolean, customMessage: String? = null) {
    Column(
        modifier = Modifier.fillMaxSize().padding(bottom = 60.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        // 1. Lógica para el icono GRANDE
        val icon = if (isNoAction) Icons.Default.Search else {
            when {
                customMessage?.contains("❌") == true -> Icons.Default.Error
                customMessage?.contains("✅") == true -> Icons.Default.CheckCircle
                else -> Icons.Default.Info
            }
        }

        // 2. Limpiamos el texto para quitarle los emojis repetidos de forma segura
        val cleanText = customMessage
            ?.replace("✅", "")
            ?.replace("❌", "")
            ?.replace("⚠️", "")
            ?.trim() ?: ""

        // Corregido: Variable asignada correctamente
        val finalDisplay = if (isNoAction) "Ingrese un número de lote para devolver" else cleanText

        Icon(
            imageVector = icon,
            contentDescription = null,
            modifier = Modifier.size(100.dp).alpha(0.2f),
            tint = if (customMessage?.contains("❌") == true) Color.Red else Color.Gray
        )

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = finalDisplay,
            style = MaterialTheme.typography.bodyLarge,
            color = Color.Gray,
            fontWeight = FontWeight.Medium,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(horizontal = 32.dp)
        )
    }
}

@Composable
fun ClienteDialog(
    clientes: List<String>,
    clienteSeleccionado: String?,
    onClienteSeleccionado: (String) -> Unit
) {
    var showClientesDialog by remember { mutableStateOf(false) }
    var tempSelected by remember { mutableStateOf(clienteSeleccionado) }
    var dialogClientes by remember { mutableStateOf(clientes) } // captura el estado actual de la lista

    // Botón para abrir diálogo
    OutlinedButton(
        onClick = {
            dialogClientes = clientes // captura la lista actual
            showClientesDialog = true
        },
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        colors = ButtonDefaults.outlinedButtonColors(contentColor = PrimaryColor),
        border = BorderStroke(1.dp, PrimaryColor)
    ) {
        Text(
            text = clienteSeleccionado ?: "Mostrar lista de clientes",
            color = if (clienteSeleccionado != null) Color.Black else Color.Gray
        )
    }

    if (showClientesDialog) {
        Dialog(
            onDismissRequest = {
                showClientesDialog = false
                tempSelected = clienteSeleccionado
            },
            properties = DialogProperties(usePlatformDefaultWidth = false) // fuerza el centrado inicial
        ) {
            Card(
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier
                    .fillMaxWidth(0.85f)
                    .heightIn(max = 500.dp), // altura máxima
                elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
            ) {
                Column(
                    modifier = Modifier
                        .background(MaterialTheme.colorScheme.surface)
                        .padding(16.dp)
                        .wrapContentHeight() // evita recomposiciones que cambien la altura
                ) {
                    Text(
                        text = "Seleccione un cliente",
                        fontWeight = FontWeight.Bold,
                        fontSize = 20.sp,
                        color = PrimaryColor,
                        modifier = Modifier.fillMaxWidth(),
                        textAlign = androidx.compose.ui.text.style.TextAlign.Center
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    Box(
                        modifier = Modifier
                            .weight(1f, fill = false)
                            .fillMaxWidth()
                    ) {
                        LazyColumn {
                            items(dialogClientes) { cliente ->
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clickable { tempSelected = cliente }
                                        .padding(12.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    RadioButton(
                                        selected = tempSelected == cliente,
                                        onClick = { tempSelected = cliente },
                                        colors = RadioButtonDefaults.colors(selectedColor = PrimaryColor)
                                    )
                                    Spacer(Modifier.width(12.dp))
                                    Text(cliente)
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.End
                    ) {
                        TextButton(onClick = {
                            showClientesDialog = false
                            tempSelected = clienteSeleccionado
                        }) { Text("Cancelar", color = PrimaryColor) }

                        Spacer(modifier = Modifier.width(12.dp))

                        TextButton(
                            onClick = {
                                showClientesDialog = false
                                tempSelected?.let { onClienteSeleccionado(it) }
                            },
                            enabled = tempSelected != null
                        ) {
                            Text(
                                "Aceptar",
                                color = if (tempSelected != null) PrimaryColor
                                else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f)
                            )
                        }
                    }
                }
            }
        }
    }
}