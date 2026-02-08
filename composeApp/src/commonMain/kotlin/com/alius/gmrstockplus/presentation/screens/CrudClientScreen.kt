package com.alius.gmrstockplus.presentation.screens

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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.navigator.LocalNavigator
import cafe.adriel.voyager.navigator.currentOrThrow
import com.alius.gmrstockplus.data.getClientRepository
import com.alius.gmrstockplus.domain.model.Cliente
import com.alius.gmrstockplus.ui.theme.ReservedColor
import com.alius.gmrstockplus.ui.theme.PrimaryColor
import com.alius.gmrstockplus.ui.theme.BackgroundColor
import com.alius.gmrstockplus.ui.theme.TextSecondary
import kotlinx.coroutines.launch

class CrudClientScreen(
    private val plantId: String // 👈 Cambiado: de databaseUrl a plantId
) : Screen {

    @OptIn(ExperimentalMaterial3Api::class)
    @Composable
    override fun Content() {
        val navigator = LocalNavigator.currentOrThrow
        val coroutineScope = rememberCoroutineScope()

        // Repositorio instanciado con el identificador de planta
        val clientRepo = remember(plantId) { getClientRepository(plantId) }

        var clients by remember { mutableStateOf<List<Pair<String, Cliente>>>(emptyList()) }
        var showEditCreateDialog by remember { mutableStateOf(false) }
        var showDeleteConfirmDialog by remember { mutableStateOf(false) }
        var clientToDelete by remember { mutableStateOf<Pair<String, Cliente>?>(null) }
        var loading by remember { mutableStateOf(true) }

        var editingClient by remember { mutableStateOf<Pair<String, Cliente>?>(null) }
        var nameField by remember { mutableStateOf(TextFieldValue("")) }
        var obsField by remember { mutableStateOf(TextFieldValue("")) }

        val focusedTextFieldColors = TextFieldDefaults.outlinedTextFieldColors(
            focusedBorderColor = PrimaryColor,
            focusedLabelColor = PrimaryColor
        )

        val roundedShape12 = RoundedCornerShape(12.dp)

        fun refreshClients() {
            coroutineScope.launch {
                loading = true
                clients = clientRepo.getAllClientsWithIds()
                loading = false
            }
        }

        LaunchedEffect(plantId) { refreshClients() } // 👈 Observamos el cambio de planta

        // --- BOX PANTALLA COMPLETA ---
        Box(modifier = Modifier.fillMaxSize().background(BackgroundColor)) {

            // --- HEADER FIJO: Flecha + Título + Subtítulo ---
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(BackgroundColor)
                    .padding(horizontal = 16.dp, vertical = 12.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(onClick = { navigator.pop() }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Atrás", tint = PrimaryColor)
                    }
                    Column(
                        modifier = Modifier.padding(start = 8.dp),
                        verticalArrangement = Arrangement.Center
                    ) {
                        Text(
                            text = "Gestión de clientes",
                            fontSize = 26.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.secondary
                        )
                        Text(
                            text = "Clientes Registrados",
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Medium),
                            color = Color.Gray,
                            modifier = Modifier.padding(top = 2.dp)
                        )
                    }
                }
            }

            val topPadding = 100.dp

            if (loading) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(top = topPadding),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(color = PrimaryColor)
                }
            } else if (clients.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(top = topPadding),
                    contentAlignment = Alignment.TopCenter
                ) {
                    Text("No hay clientes registrados.", color = TextSecondary)
                }
            } else {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 16.dp, vertical = 8.dp)
                        .padding(top = topPadding),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(clients) { (documentId, cliente) ->
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = roundedShape12,
                            colors = CardDefaults.cardColors(containerColor = Color.White),
                            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(16.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        cliente.cliNombre,
                                        style = MaterialTheme.typography.titleMedium.copy(fontSize = 18.sp),
                                        fontWeight = FontWeight.Bold,
                                        color = PrimaryColor,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                    if (cliente.cliObservaciones.isNotEmpty()) {
                                        Spacer(modifier = Modifier.height(4.dp))
                                        Text(
                                            "Obs: ${cliente.cliObservaciones}",
                                            style = MaterialTheme.typography.bodyMedium,
                                            color = Color.DarkGray,
                                            maxLines = 2,
                                            overflow = TextOverflow.Ellipsis
                                        )
                                    }
                                }
                                Row(
                                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    IconButton(onClick = {
                                        editingClient = documentId to cliente
                                        nameField = TextFieldValue(cliente.cliNombre)
                                        obsField = TextFieldValue(cliente.cliObservaciones)
                                        showEditCreateDialog = true
                                    }) {
                                        Icon(Icons.Default.Edit, contentDescription = "Editar", tint = PrimaryColor)
                                    }
                                    IconButton(onClick = {
                                        clientToDelete = documentId to cliente
                                        showDeleteConfirmDialog = true
                                    }) {
                                        Icon(Icons.Default.Delete, contentDescription = "Eliminar", tint = ReservedColor)
                                    }
                                }
                            }
                        }
                    }
                    item { Spacer(Modifier.height(80.dp)) }
                }
            }

            // --- Floating Action Button ---
            FloatingActionButton(
                onClick = {
                    editingClient = null
                    nameField = TextFieldValue("")
                    obsField = TextFieldValue("")
                    showEditCreateDialog = true
                },
                containerColor = PrimaryColor,
                shape = RoundedCornerShape(50),
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(24.dp)
            ) {
                Icon(Icons.Default.Add, contentDescription = "Nuevo cliente", tint = Color.White)
            }

            // --- Dialogos de Crear/Editar ---
            if (showEditCreateDialog) {
                AlertDialog(
                    onDismissRequest = { showEditCreateDialog = false },
                    title = {
                        Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                            Text(
                                if (editingClient == null) "Nuevo cliente" else "Editar cliente",
                                fontWeight = FontWeight.Bold,
                                color = PrimaryColor
                            )
                        }
                    },
                    text = {
                        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                            OutlinedTextField(
                                value = nameField,
                                onValueChange = { nameField = it },
                                label = { Text("Nombre del cliente") },
                                singleLine = true,
                                shape = roundedShape12,
                                colors = focusedTextFieldColors
                            )
                            OutlinedTextField(
                                value = obsField,
                                onValueChange = { obsField = it },
                                label = { Text("Observaciones") },
                                maxLines = 3,
                                shape = roundedShape12,
                                colors = focusedTextFieldColors
                            )
                        }
                    },
                    confirmButton = {
                        TextButton(onClick = {
                            coroutineScope.launch {
                                val nuevo = Cliente(
                                    cliNombre = nameField.text.trim(),
                                    cliObservaciones = obsField.text.trim()
                                )
                                if (editingClient == null) {
                                    clientRepo.addClient(nuevo)
                                } else {
                                    clientRepo.updateClient(editingClient!!.first, nuevo)
                                }
                                showEditCreateDialog = false
                                refreshClients()
                            }
                        }) { Text("Guardar", fontWeight = FontWeight.SemiBold, color = PrimaryColor) }
                    },
                    dismissButton = {
                        TextButton(onClick = { showEditCreateDialog = false }) {
                            Text("Cancelar", fontWeight = FontWeight.SemiBold, color = PrimaryColor)
                        }
                    }
                )
            }

            // --- Dialogo de Confirmación de Eliminación ---
            if (showDeleteConfirmDialog && clientToDelete != null) {
                val (documentId, cliente) = clientToDelete!!
                AlertDialog(
                    onDismissRequest = { showDeleteConfirmDialog = false },
                    icon = { Icon(Icons.Default.Warning, contentDescription = "Advertencia", tint = ReservedColor) },
                    title = {
                        Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                            Text("Confirmar eliminación", fontWeight = FontWeight.Bold, color = PrimaryColor)
                        }
                    },
                    text = {
                        Text("¿Está seguro de que desea eliminar al cliente ${cliente.cliNombre}?")
                    },
                    confirmButton = {
                        TextButton(onClick = {
                            coroutineScope.launch {
                                clientRepo.deleteClient(documentId)
                                showDeleteConfirmDialog = false
                                clientToDelete = null
                                refreshClients()
                            }
                        }) {
                            Text("Eliminar", color = ReservedColor, fontWeight = FontWeight.SemiBold)
                        }
                    },
                    dismissButton = {
                        TextButton(onClick = {
                            showDeleteConfirmDialog = false
                            clientToDelete = null
                        }) {
                            Text("Cancelar", color = PrimaryColor, fontWeight = FontWeight.SemiBold)
                        }
                    }
                )
            }
        }
    }
}