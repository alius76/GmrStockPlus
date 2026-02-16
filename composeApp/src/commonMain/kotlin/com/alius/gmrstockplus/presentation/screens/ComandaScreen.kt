package com.alius.gmrstockplus.presentation.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ListAlt
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.navigator.LocalNavigator
import cafe.adriel.voyager.navigator.currentOrThrow
import com.alius.gmrstockplus.data.getClientRepository
import com.alius.gmrstockplus.data.getComandaRepository
import com.alius.gmrstockplus.data.getMaterialRepository
import com.alius.gmrstockplus.domain.model.AsignacionLote
import com.alius.gmrstockplus.domain.model.Cliente
import com.alius.gmrstockplus.domain.model.Comanda
import com.alius.gmrstockplus.domain.model.Material
import com.alius.gmrstockplus.ui.components.AddComandaDialog
import com.alius.gmrstockplus.ui.components.ClientesSelectedDialogContent
import com.alius.gmrstockplus.ui.components.ComandaCard
import com.alius.gmrstockplus.ui.components.ConfirmDeleteComandaDialog
import com.alius.gmrstockplus.ui.components.ConfirmReassignDateDialog
import com.alius.gmrstockplus.ui.components.EditRemarkDialog
import com.alius.gmrstockplus.ui.components.InlineCalendarSelector
import com.alius.gmrstockplus.ui.components.MaterialSelectedDialogContent
import com.alius.gmrstockplus.ui.components.UniversalDatePickerDialog
import com.alius.gmrstockplus.ui.components.PlanningAssignmentBottomSheet
import com.alius.gmrstockplus.ui.components.SelectMaterialAssignmentDialog
import com.alius.gmrstockplus.ui.theme.BackgroundColor
import com.alius.gmrstockplus.ui.theme.PrimaryColor
import kotlinx.coroutines.launch
import kotlinx.datetime.*

@OptIn(ExperimentalMaterial3Api::class)
class ComandaScreen(
    private val plantId: String,
    private val currentUserEmail: String
) : Screen {

    @Composable
    override fun Content() {
        val navigator = LocalNavigator.currentOrThrow
        val scope = rememberCoroutineScope()
        val snackbarHostState = remember { SnackbarHostState() }

        var isProcessing by remember { mutableStateOf(false) }

        // --- Repositorios ---
        val clientRepository = remember(plantId) { getClientRepository(plantId) }
        val materialRepository = remember(plantId) { getMaterialRepository(plantId) }
        val comandaRepository = remember(plantId) { getComandaRepository(plantId) }

        // --- Estados principales ---
        var comandasDelDia by remember { mutableStateOf(listOf<Comanda>()) }
        var fechaSeleccionada by remember {
            mutableStateOf(Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault()).date)
        }
        var todasLasComandas by remember { mutableStateOf(listOf<Comanda>()) }

        // --- Estados para Añadir Comanda ---
        var clients by remember { mutableStateOf<List<Cliente>>(emptyList()) }
        var selectedCliente by remember { mutableStateOf<Cliente?>(null) }
        var showClientesDialog by remember { mutableStateOf(false) }

        var materials by remember { mutableStateOf<List<Material>>(emptyList()) }
        var showMaterialDialog by remember { mutableStateOf(false) }

        var showAgregarDialog by remember { mutableStateOf(false) }
        var totalWeightComanda by remember { mutableStateOf("") }
        var remarkComanda by remember { mutableStateOf("") }

        var errorCliente by remember { mutableStateOf(false) }
        var errorDescripcion by remember { mutableStateOf(false) }
        var errorPeso by remember { mutableStateOf(false) }

        var selectedComanda by remember { mutableStateOf<Comanda?>(null) }

        // --- Estados para Reasignar Fecha ---
        var showDatePicker by remember { mutableStateOf(false) }
        var comandaToUpdateDate by remember { mutableStateOf<Comanda?>(null) }

        // --- Estados para ASIGNACIÓN DE LOTE ---
        var showAssignmentSheet by remember { mutableStateOf(false) }
        var showMaterialSelectionDialog by remember { mutableStateOf(false) }
        var comandaForLote by remember { mutableStateOf<Comanda?>(null) }
        val assignmentSheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

        // --- ESTADOS PARA MULTI-MATERIAL ---
        var selectedMaterialsList by remember { mutableStateOf(listOf<Material>()) }

        // --- Diálogos y Modificaciones ---
        var showConfirmDeleteDialog by remember { mutableStateOf(false) }
        var showConfirmReassignDateDialog by remember { mutableStateOf(false) }
        var showEditRemarkDialog by remember { mutableStateOf(false) }
        var comandaToModify by remember { mutableStateOf<Comanda?>(null) }
        var newRemark by remember { mutableStateOf("") }
        var newDateSelected by remember { mutableStateOf<LocalDate?>(null) }

        fun refrescarDatos() {
            scope.launch {
                try {
                    val listaDia = comandaRepository.listarComandas(fechaSeleccionada.toString())
                    val listaGlobal = comandaRepository.listarTodasComandas()
                    comandasDelDia = listaDia
                    todasLasComandas = listaGlobal
                } catch (e: Exception) {
                    println("Error al refrescar datos: ${e.message}")
                }
            }
        }

        LaunchedEffect(fechaSeleccionada) { refrescarDatos() }

        LaunchedEffect(plantId) {
            clients = try {
                clientRepository.getAllClientsOrderedByName().filter { it.cliNombre != "NO OK" }
            } catch (e: Exception) {
                emptyList()
            }

            materials = try {
                materialRepository.getAllMaterialsOrderedByName()
            } catch (e: Exception) {
                emptyList()
            }
        }

        fun resetFormStates() {
            selectedCliente = null
            selectedMaterialsList = emptyList()
            totalWeightComanda = ""
            remarkComanda = ""
            errorCliente = false
            errorDescripcion = false
            errorPeso = false
        }

        fun guardarComanda() {
            val instantToSave = fechaSeleccionada.atStartOfDayIn(TimeZone.UTC)
            val asignacionesIniciales = selectedMaterialsList.map {
                AsignacionLote(
                    materialNombre = it.materialNombre,
                    cantidadBB = 0,
                    numeroLote = "",
                    idLote = "",
                    userAsignacion = ""
                )
            }

            val nuevaComanda = Comanda(
                idComanda = "",
                bookedClientComanda = selectedCliente,
                descriptionLoteComanda = selectedMaterialsList.firstOrNull()?.materialNombre ?: "",
                numberLoteComanda = "",
                dateBookedComanda = instantToSave,
                totalWeightComanda = totalWeightComanda,
                remarkComanda = remarkComanda,
                userEmailComanda = currentUserEmail,
                listaAsignaciones = asignacionesIniciales
            )
            scope.launch {
                val exito = comandaRepository.addComanda(nuevaComanda)
                if (exito) {
                    refrescarDatos()
                    resetFormStates()
                }
            }
        }

        fun actualizarObservaciones(comanda: Comanda, text: String) {
            scope.launch {
                comanda.idComanda.takeIf { it.isNotEmpty() }?.let { id ->
                    val exito = comandaRepository.updateComandaRemark(id, text)
                    if (exito) refrescarDatos()
                }
            }
        }

        fun ejecutarEliminar(comanda: Comanda) {
            // 1. Verificación de seguridad:
            // No permitimos borrar si hay algún lote que ya salió de la planta (fueVendido)
            val tieneVentasVinculadas = comanda.listaAsignaciones.any { it.fueVendido }

            if (tieneVentasVinculadas) {
                scope.launch {
                    snackbarHostState.showSnackbar(
                        message = "No se puede anular: existen lotes ya confirmados en báscula.",
                        duration = SnackbarDuration.Long
                    )
                }
                showConfirmDeleteDialog = false
                comandaToModify = null
                return
            }

            // 2. Proceso de eliminación si la comanda está "limpia"
            scope.launch {
                try {
                    isProcessing = true
                    val exito = comandaRepository.deleteComanda(comanda.idComanda)
                    if (exito) {
                        snackbarHostState.showSnackbar("Comanda anulada con éxito")
                        refrescarDatos()
                    } else {
                        snackbarHostState.showSnackbar("No se pudo eliminar la comanda")
                    }
                } catch (e: Exception) {
                    println("❌ Error al eliminar: ${e.message}")
                    snackbarHostState.showSnackbar("Error de conexión al servidor")
                } finally {
                    // Garantizamos que la UI se limpie siempre
                    isProcessing = false
                    showConfirmDeleteDialog = false
                    comandaToModify = null
                }
            }
        }

        fun ejecutarReasignacionFinal() {
            val comandaToReassign = comandaToModify
            val targetDate = newDateSelected
            if (comandaToReassign != null && targetDate != null) {
                scope.launch {
                    val newInstant = targetDate.atStartOfDayIn(TimeZone.UTC)
                    val exito =
                        comandaRepository.updateComandaDate(comandaToReassign.idComanda, newInstant)
                    if (exito) {
                        fechaSeleccionada = targetDate
                        refrescarDatos()
                    }
                    comandaToModify = null
                    newDateSelected = null
                    showConfirmReassignDateDialog = false
                }
            }
        }

        // --- UI Principal ---
        Box(modifier = Modifier.fillMaxSize().background(BackgroundColor)) {
            Column(modifier = Modifier.fillMaxWidth()) {
                // Header (Exactamente igual a tu original)
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(BackgroundColor)
                        .padding(horizontal = 16.dp, vertical = 12.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        IconButton(onClick = { navigator.pop() }) {
                            Icon(
                                Icons.Default.ArrowBack,
                                contentDescription = "Atrás",
                                tint = PrimaryColor
                            )
                        }
                        Column(modifier = Modifier.weight(1f).padding(start = 8.dp)) {
                            Text(
                                "Gestión de comandas",
                                fontSize = 26.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.secondary
                            )
                            Text(
                                "Seleccione fecha",
                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Medium),
                                color = Color.Gray,
                                modifier = Modifier.padding(top = 2.dp)
                            )
                        }
                        IconButton(onClick = {
                            navigator.push(
                                ComandasPlanningScreen(
                                    plantId,
                                    currentUserEmail
                                )
                            )
                        }) {
                            Icon(
                                Icons.Default.ListAlt,
                                contentDescription = "Planning",
                                tint = PrimaryColor,
                                modifier = Modifier.size(32.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    InlineCalendarSelector(
                        selectedDate = fechaSeleccionada,
                        allComandas = todasLasComandas,
                        onDateSelected = { fechaSeleccionada = it },
                        primaryColor = PrimaryColor
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        val conteo =
                            if (comandasDelDia.isNotEmpty()) " (${comandasDelDia.size})" else ""
                        Text(
                            "Comandas$conteo",
                            fontSize = 22.sp,
                            fontWeight = FontWeight.Bold,
                            color = PrimaryColor
                        )
                        IconButton(onClick = { resetFormStates(); showAgregarDialog = true }) {
                            Icon(
                                Icons.Default.Add,
                                contentDescription = "Agregar",
                                tint = PrimaryColor,
                                modifier = Modifier.size(32.dp)
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(16.dp)
                ) {
                    items(comandasDelDia) { comanda ->
                        ComandaCard(
                            comanda = comanda,
                            isSelected = selectedComanda == comanda,
                            onClick = {
                                selectedComanda = if (selectedComanda == comanda) null else comanda
                            },
                            onDelete = {
                                comandaToModify = comanda
                                showConfirmDeleteDialog = true
                                selectedComanda = null
                            },
                            onReassign = {
                                comandaToModify = comanda
                                comandaToUpdateDate = comanda
                                selectedComanda = null
                                showDatePicker = true
                            },
                            onEditRemark = {
                                comandaToModify = comanda
                                newRemark = comanda.remarkComanda
                                showEditRemarkDialog = true
                                selectedComanda = null
                            },
                            onAssignLote = {
                                comandaForLote = comanda
                                // Si hay varios materiales, preguntamos cuál
                                val mats =
                                    comanda.listaAsignaciones.map { it.materialNombre }.distinct()
                                if (mats.size > 1) {
                                    showMaterialSelectionDialog = true
                                } else {
                                    val mat = mats.firstOrNull() ?: comanda.descriptionLoteComanda
                                    comandaForLote = comanda.copy(descriptionLoteComanda = mat)
                                    showAssignmentSheet = true
                                    scope.launch { assignmentSheetState.show() }
                                }
                                selectedComanda = null
                            }
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                    }
                }
            }

            SnackbarHost(
                hostState = snackbarHostState,
                modifier = Modifier.align(Alignment.BottomCenter)
            )

            // --- Dialog Selección Material para ASIGNAR (Multi-material) ---
            if (showMaterialSelectionDialog && comandaForLote != null) {
                SelectMaterialAssignmentDialog(
                    comanda = comandaForLote!!,
                    onDismiss = { showMaterialSelectionDialog = false },
                    onMaterialSelected = { mat ->
                        comandaForLote = comandaForLote!!.copy(descriptionLoteComanda = mat)
                        showMaterialSelectionDialog = false
                        showAssignmentSheet = true
                        scope.launch { assignmentSheetState.show() }
                    }
                )
            }

            if (showAgregarDialog) {
                AddComandaDialog(
                    selectedCliente = selectedCliente,
                    selectedMaterialsList = selectedMaterialsList,
                    totalWeight = totalWeightComanda,
                    remark = remarkComanda,
                    errorCliente = errorCliente,
                    errorDescripcion = errorDescripcion,
                    errorPeso = errorPeso,
                    onDismiss = { showAgregarDialog = false; resetFormStates() },
                    onSelectCliente = { showClientesDialog = true },
                    onAddMaterial = { showMaterialDialog = true },
                    onRemoveMaterial = { selectedMaterialsList = selectedMaterialsList - it },
                    onWeightChange = { totalWeightComanda = it.filter { c -> c.isDigit() } },
                    onRemarkChange = { remarkComanda = it },
                    onSave = {
                        val valid =
                            selectedCliente != null && selectedMaterialsList.isNotEmpty() && totalWeightComanda.isNotEmpty()
                        if (valid) {
                            guardarComanda()
                            showAgregarDialog = false
                        } else {
                            errorCliente = selectedCliente == null
                            errorDescripcion = selectedMaterialsList.isEmpty()
                            errorPeso = totalWeightComanda.isEmpty()
                        }
                    }
                )
            }

            // --- Diálogos de Selección ---
            if (showClientesDialog) {
                ClientesSelectedDialogContent(
                    clients = clients,
                    currentSelectedClient = selectedCliente,
                    showAllOption = false,
                    onDismiss = { showClientesDialog = false },
                    onConfirm = {
                        selectedCliente = it; errorCliente = false; showClientesDialog = false
                    }
                )
            }

            if (showMaterialDialog) {
                MaterialSelectedDialogContent(
                    materials = materials, currentSelectedMaterial = null,
                    onDismiss = { showMaterialDialog = false },
                    onConfirm = {
                        if (!selectedMaterialsList.contains(it)) selectedMaterialsList =
                            selectedMaterialsList + it
                        errorDescripcion = false
                        showMaterialDialog = false
                    }
                )
            }

            if (showDatePicker) {
                UniversalDatePickerDialog(
                    initialDate = comandaToUpdateDate?.dateBookedComanda?.toLocalDateTime(TimeZone.currentSystemDefault())?.date
                        ?: fechaSeleccionada,
                    onDateSelected = {
                        showDatePicker = false
                        if (comandaToUpdateDate != null) {
                            newDateSelected = it
                            comandaToModify = comandaToUpdateDate
                            showConfirmReassignDateDialog = true
                        } else {
                            fechaSeleccionada = it
                        }
                        comandaToUpdateDate = null
                    },
                    onDismiss = { showDatePicker = false },
                    primaryColor = PrimaryColor
                )
            }

            if (showConfirmDeleteDialog && comandaToModify != null) {
                ConfirmDeleteComandaDialog(
                    clienteNombre = comandaToModify!!.bookedClientComanda?.cliNombre ?: "",
                    isProcessing = isProcessing,
                    onDismiss = { showConfirmDeleteDialog = false; comandaToModify = null },
                    onConfirm = { ejecutarEliminar(comandaToModify!!) }
                )
            }

            if (showConfirmReassignDateDialog && comandaToModify != null && newDateSelected != null) {
                val oldDate =
                    comandaToModify!!.dateBookedComanda?.toLocalDateTime(TimeZone.currentSystemDefault())?.date
                        ?: fechaSeleccionada
                ConfirmReassignDateDialog(
                    clienteNombre = comandaToModify!!.bookedClientComanda?.cliNombre ?: "",
                    oldDate = "${oldDate.dayOfMonth}/${oldDate.monthNumber}",
                    newDate = "${newDateSelected!!.dayOfMonth}/${newDateSelected!!.monthNumber}",
                    onDismiss = { showConfirmReassignDateDialog = false; comandaToModify = null },
                    onConfirm = { ejecutarReasignacionFinal() }
                )
            }

            if (showEditRemarkDialog && comandaToModify != null) {
                EditRemarkDialog(
                    remark = newRemark,
                    onRemarkChange = { newRemark = it },
                    onDismiss = { showEditRemarkDialog = false; comandaToModify = null },
                    onSave = {
                        actualizarObservaciones(
                            comandaToModify!!,
                            newRemark
                        ); showEditRemarkDialog = false
                    }
                )
            }

            // El BottomSheet se puede quedar igual por su complejidad de estado (sheetState)
            if (showAssignmentSheet && comandaForLote != null) {
                ModalBottomSheet(
                    onDismissRequest = { showAssignmentSheet = false; comandaForLote = null },
                    sheetState = assignmentSheetState,
                    containerColor = Color.White
                ) {
                    PlanningAssignmentBottomSheet(
                        selectedComanda = comandaForLote!!,
                        plantId = plantId,
                        currentUserEmail = currentUserEmail,
                        clientRepository = clientRepository,
                        onLoteAssignmentSuccess = {
                            refrescarDatos()
                            scope.launch { assignmentSheetState.hide() }.invokeOnCompletion {
                                showAssignmentSheet = false
                                comandaForLote = null
                            }
                        },
                        snackbarHostState = snackbarHostState
                    )
                }
            }
        }
    }
}