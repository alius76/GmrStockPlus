package com.alius.gmrstockplus.presentation.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.window.DialogProperties
import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.navigator.LocalNavigator
import cafe.adriel.voyager.navigator.currentOrThrow
import com.alius.gmrstockplus.core.utils.formatWeight
import com.alius.gmrstockplus.data.agruparPorMaterial
import com.alius.gmrstockplus.data.getClientRepository
import com.alius.gmrstockplus.data.getLoteRepository
import com.alius.gmrstockplus.domain.model.*
import com.alius.gmrstockplus.ui.components.*
import com.alius.gmrstockplus.ui.theme.BackgroundColor
import com.alius.gmrstockplus.ui.theme.PrimaryColor
import com.alius.gmrstockplus.ui.theme.TextSecondary
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
class HomeScreenContent(
    private val user: User,
    private val plantId: String,
    private val onChangeDatabase: () -> Unit,
    private val onLogoutClick: () -> Unit = {}
) : Screen {

    @Composable
    override fun Content() {
        val loteRepository = remember(plantId) { getLoteRepository(plantId) }
        val clientRepository = remember(plantId) { getClientRepository(plantId) }
        val coroutineScope = rememberCoroutineScope()
        val localNavigator = LocalNavigator.currentOrThrow

        var lotes by remember(plantId) { mutableStateOf<List<LoteModel>>(emptyList()) }
        var materialGroups by remember(plantId) { mutableStateOf<List<MaterialGroup>>(emptyList()) }
        var isLoading by remember(plantId) { mutableStateOf(true) }
        var errorMessage by remember(plantId) { mutableStateOf<String?>(null) }

        val updateLoteState: (LoteModel) -> Unit = { updatedLote ->
            lotes = lotes.map { if (it.id == updatedLote.id) updatedLote else it }
            materialGroups = agruparPorMaterial(lotes)
        }

        var showLogoutDialog by remember { mutableStateOf(false) }
        var showUnimplementedDialog by remember { mutableStateOf(false) }
        var showMaintenanceDialog by remember { mutableStateOf(false) }
        var showSearchDialog by remember { mutableStateOf(false) }

        val snackbarHostState = remember { SnackbarHostState() }
        val currentUserEmail = remember(user.email) { user.email.substringBefore("@") }

        val sheetStateGroup = rememberModalBottomSheetState(skipPartiallyExpanded = true)
        var showGroupMaterialBottomSheet by remember { mutableStateOf(false) }
        var selectedGroupForSheet by remember { mutableStateOf<MaterialGroup?>(null) }

        val sheetStateLotes = rememberModalBottomSheetState(skipPartiallyExpanded = true)
        var showLotesBottomSheet by remember { mutableStateOf(false) }

        val sheetStateSalidas = rememberModalBottomSheetState(skipPartiallyExpanded = true)
        var showSalidasBottomSheet by remember { mutableStateOf(false) }

        LaunchedEffect(plantId) {
            isLoading = true
            errorMessage = null
            try {
                val loadedLotes = loteRepository.listarLotes("")
                lotes = loadedLotes
                materialGroups = agruparPorMaterial(loadedLotes)
            } catch (e: Exception) {
                errorMessage = e.message ?: "Error al conectar con la planta $plantId"
            } finally {
                isLoading = false
            }
        }

        Scaffold(
            snackbarHost = { SnackbarHost(snackbarHostState) },
            floatingActionButton = {
                if (!isLoading && errorMessage == null) {
                    DailyScheduleFAB(onClick = {
                        showSalidasBottomSheet = true
                        coroutineScope.launch { sheetStateSalidas.show() }
                    })
                }
            }
        ) { paddingValues ->
            Box(modifier = Modifier.fillMaxSize()) {

                // --- DIÁLOGOS MODERNOS ---

                if (showMaintenanceDialog) {
                    ModernOptionDialog(
                        title = "Gestión - $plantId",
                        onDismiss = { showMaintenanceDialog = false },
                        options = listOf(
                            DialogOption("Gestión de clientes", Icons.Default.People) {
                                showMaintenanceDialog = false
                                localNavigator.push(CrudClientScreen(plantId))
                            },
                            DialogOption("Control de materiales", Icons.Default.Category) {
                                showUnimplementedDialog = true
                            },
                            DialogOption("Devoluciones", Icons.Default.AssignmentReturn) {
                                showMaintenanceDialog = false
                                localNavigator.push(DevolucionesScreen(plantId))
                            },
                            DialogOption("Gestión de comandas", Icons.Default.CalendarMonth) {
                                showMaintenanceDialog = false
                                localNavigator.push(ComandaScreen(plantId,currentUserEmail))
                            }
                        )
                    )
                }

                if (showSearchDialog) {
                    ModernOptionDialog(
                        title = "Consultas - $plantId",
                        onDismiss = { showSearchDialog = false },
                        options = listOf(
                            DialogOption("Producción", Icons.Default.Settings) {
                                localNavigator.push(ProduccionRangoScreen(plantId))
                            },
                            DialogOption("Informe de ventas", Icons.Default.EuroSymbol) {
                                localNavigator.push(VentasClienteScreen(plantId))
                            },
                            DialogOption("Lotes reprocesados", Icons.Default.Repeat) {
                                localNavigator.push(ReprocesarScreen(plantId))
                            },
                            DialogOption("Trazabilidad", Icons.Default.Timeline) {
                                localNavigator.push(TrazabilidadScreen(plantId))
                            }
                        )
                    )
                }

                // --- DIÁLOGOS ESTÁNDAR ---

                if (showUnimplementedDialog) {
                    AlertDialog(
                        onDismissRequest = { showUnimplementedDialog = false },
                        title = { Text(text = "Funcionalidad en desarrollo", color = PrimaryColor) },
                        text = { Text(text = "Esta funcionalidad aún no está implementada.") },
                        confirmButton = { TextButton(onClick = { showUnimplementedDialog = false }) { Text("Aceptar", color = PrimaryColor) } }
                    )
                }

                if (showLogoutDialog) {
                    AlertDialog(
                        onDismissRequest = { showLogoutDialog = false },
                        title = { Text(text = "¿Cerrar sesión?", color = PrimaryColor) },
                        text = { Text(text = "¿Estás seguro de que quieres cerrar la sesión?") },
                        confirmButton = { TextButton(onClick = onLogoutClick) { Text("Aceptar", color = PrimaryColor) } },
                        dismissButton = { TextButton(onClick = { showLogoutDialog = false }) { Text("Cancelar", color = PrimaryColor) } }
                    )
                }

                // --- CONTENIDO PRINCIPAL ---

                if (isLoading) {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) { CircularProgressIndicator(color = PrimaryColor) }
                } else if (errorMessage != null) {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) { Text(text = errorMessage!!, color = MaterialTheme.colorScheme.error) }
                } else {
                    Column(modifier = Modifier.align(Alignment.TopStart).padding(horizontal = 16.dp)) {
                        Row(modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            ActionButton(Modifier.weight(1f), Icons.Default.SwapHoriz, "SWAP", onChangeDatabase)
                            ActionButton(Modifier.weight(1f), Icons.Default.Tune, "Gestión", { showMaintenanceDialog = true })
                            ActionButton(Modifier.weight(1f), Icons.Default.ScreenSearchDesktop, "Consultar", { showSearchDialog = true })
                            ActionButton(Modifier.weight(1f), Icons.Default.Apartment, "Vertisol") {localNavigator.push(VertisolScreen(plantId))}
                            ActionButton(Modifier.weight(1f), Icons.Default.PowerSettingsNew, currentUserEmail, { showLogoutDialog = true })
                        }

                        LazyColumn(
                            contentPadding = PaddingValues(bottom = paddingValues.calculateBottomPadding() + 80.dp),
                            modifier = Modifier.fillMaxSize()
                        ) {
                            item {
                                Row(modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp), verticalAlignment = Alignment.CenterVertically) {
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text("Materiales en stock", style = MaterialTheme.typography.titleLarge.copy(fontSize = 26.sp, fontWeight = FontWeight.Bold), color = MaterialTheme.colorScheme.secondary)
                                        val totalKilos = materialGroups.sumOf { it.totalWeight.toDoubleOrNull() ?: 0.0 }
                                        Text("Total kilos: ${formatWeight(totalKilos)} Kg", color = TextSecondary)
                                    }
                                    IconButton(onClick = {
                                        val total = materialGroups.sumOf { it.totalWeight.toDoubleOrNull() ?: 0.0 }
                                        com.alius.gmrstockplus.core.utils.PdfGenerator.generateStockReportPdf(materialGroups, total)
                                    }) { Icon(Icons.Filled.PictureAsPdf, null, tint = PrimaryColor) }
                                    IconButton(onClick = {
                                        showLotesBottomSheet = true
                                        coroutineScope.launch { sheetStateLotes.show() }
                                    }) { Icon(Icons.Filled.Search, null, tint = PrimaryColor) }
                                }
                            }

                            items(materialGroups) { group ->
                                MaterialGroupCard(group = group) { clickedGroup ->
                                    selectedGroupForSheet = clickedGroup
                                    showGroupMaterialBottomSheet = true
                                    coroutineScope.launch { sheetStateGroup.show() }
                                }
                                Spacer(modifier = Modifier.height(8.dp))
                            }
                        }
                    }
                }

                // --- BOTTOM SHEETS ---

                if (showGroupMaterialBottomSheet && selectedGroupForSheet != null) {
                    ModalBottomSheet(
                        onDismissRequest = { showGroupMaterialBottomSheet = false },
                        sheetState = sheetStateGroup,
                        modifier = Modifier.fillMaxHeight(0.75f)
                    ) {
                        GroupMaterialBottomSheetContent(
                            loteNumbers = selectedGroupForSheet!!.loteNumbers,
                            onLoteClick = { },
                            onDismissRequest = { showGroupMaterialBottomSheet = false },
                            snackbarHostState = snackbarHostState,
                            onViewBigBags = { },
                            databaseUrl = plantId,
                            onRemarkUpdated = updateLoteState,
                            clientRepository = clientRepository,
                            currentUserEmail = currentUserEmail
                        )
                    }
                }

                if (showLotesBottomSheet) {
                    ModalBottomSheet(
                        onDismissRequest = { showLotesBottomSheet = false },
                        sheetState = sheetStateLotes,
                        modifier = Modifier.fillMaxHeight(0.75f)
                    ) {
                        LotesBottomSheetContent(
                            loteRepository = loteRepository,
                            clientRepository = clientRepository,
                            databaseUrl = plantId,
                            currentUserEmail = currentUserEmail,
                            snackbarHostState = snackbarHostState,
                            onViewBigBags = { },
                            onRemarkUpdated = updateLoteState
                        )
                    }
                }

                if (showSalidasBottomSheet) {
                    ModalBottomSheet(
                        onDismissRequest = { showSalidasBottomSheet = false },
                        sheetState = sheetStateSalidas,
                        modifier = Modifier.fillMaxHeight(0.75f)
                    ) {
                        SalidasProgramadasBottomSheet(
                            databaseUrl = plantId,
                            snackbarHostState = snackbarHostState,
                            onComandaClick = { comanda ->
                                coroutineScope.launch {
                                    sheetStateSalidas.hide()
                                    showSalidasBottomSheet = false
                                    snackbarHostState.showSnackbar("Comanda #${comanda.numeroDeComanda} seleccionada.")
                                }
                            }
                        )
                    }
                }
            }
        }
    }

    // --- COMPONENTES INTERNOS DE APOYO ---

    @Composable
    private fun ActionButton(modifier: Modifier = Modifier, icon: ImageVector, label: String, onClick: () -> Unit) {
        ElevatedCard(
            onClick = onClick,
            modifier = modifier.height(80.dp),
            shape = RoundedCornerShape(12.dp),
            colors = CardDefaults.elevatedCardColors(containerColor = Color.Transparent),
            elevation = CardDefaults.cardElevation(defaultElevation = 6.dp)
        ) {
            Box(
                modifier = Modifier.fillMaxSize().background(
                    brush = Brush.verticalGradient(colors = listOf(Color(0xFF029083), Color(0xFF00BFA5)))
                ).padding(8.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Center) {
                    Icon(icon, contentDescription = label, tint = Color.White, modifier = Modifier.size(24.dp))
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(text = label, color = Color.White, fontWeight = FontWeight.SemiBold, maxLines = 1, fontSize = 12.sp)
                }
            }
        }
    }

    @Composable
    private fun ModernOptionDialog(
        title: String,
        onDismiss: () -> Unit,
        options: List<DialogOption>
    ) {
        AlertDialog(
            onDismissRequest = onDismiss,
            properties = DialogProperties(usePlatformDefaultWidth = false),
            modifier = Modifier.padding(28.dp),
            confirmButton = {},
            dismissButton = {
                TextButton(onClick = onDismiss) {
                    Text("Cerrar", color = PrimaryColor, fontWeight = FontWeight.Bold)
                }
            },
            title = {
                Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxWidth()) {
                    Text(
                        text = title,
                        style = MaterialTheme.typography.headlineSmall.copy(
                            fontWeight = FontWeight.ExtraBold,
                            color = MaterialTheme.colorScheme.secondary
                        )
                    )
                    Divider(
                        modifier = Modifier.padding(top = 12.dp).width(60.dp),
                        thickness = 3.dp,
                        color = PrimaryColor
                    )
                }
            },
            text = {
                Column(
                    modifier = Modifier.padding(top = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    options.forEach { option ->
                        Surface(
                            onClick = option.onClick,
                            shape = RoundedCornerShape(16.dp),
                            color = Color.White,
                            tonalElevation = 2.dp,
                            border = BorderStroke(1.dp, Color(0xFFEEEEEE)),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier.padding(16.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(40.dp)
                                        .background(PrimaryColor.copy(alpha = 0.1f), RoundedCornerShape(12.dp)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(option.icon, null, tint = PrimaryColor, modifier = Modifier.size(22.dp))
                                }
                                Spacer(Modifier.width(16.dp))
                                Text(
                                    text = option.label,
                                    style = MaterialTheme.typography.bodyLarge.copy(
                                        fontWeight = FontWeight.Bold,
                                        color = Color.DarkGray
                                    )
                                )
                            }
                        }
                    }
                }
            },
            shape = RoundedCornerShape(28.dp),
            containerColor = BackgroundColor
        )
    }
}

data class DialogOption(val label: String, val icon: ImageVector, val onClick: () -> Unit)