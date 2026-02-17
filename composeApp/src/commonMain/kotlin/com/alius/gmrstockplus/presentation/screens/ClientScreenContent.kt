package com.alius.gmrstockplus.presentation.screens

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.alius.gmrstockplus.data.getVentaRepository
import com.alius.gmrstockplus.domain.model.ClientGroupSell
import com.alius.gmrstockplus.domain.model.User
import com.alius.gmrstockplus.domain.model.Venta
import com.alius.gmrstockplus.ui.components.ClientGroupSellCard
import com.alius.gmrstockplus.ui.components.GroupClientBottomSheetContent
import kotlinx.coroutines.launch
import kotlinx.datetime.Clock
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import androidx.compose.ui.Alignment
import androidx.compose.ui.graphics.Color

@OptIn(ExperimentalFoundationApi::class, ExperimentalMaterial3Api::class)
@Composable
fun ClientScreenContent(user: User, plantId: String) { // 🔑 Cambiado: databaseUrl -> plantId

    // 1. Instanciamos el repositorio usando el plantId
    val ventaRepository = remember(plantId) { getVentaRepository(plantId) }

    var ventas by remember { mutableStateOf<List<Venta>>(emptyList()) }
    var selectedClientGroup by remember { mutableStateOf<ClientGroupSell?>(null) }
    var selectedClientVentas by remember { mutableStateOf<List<Venta>>(emptyList()) }
    var loading by remember { mutableStateOf(true) }

    val coroutineScope = rememberCoroutineScope()
    val bottomSheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    // 2. Cargamos los datos cuando cambie la planta
    LaunchedEffect(plantId) {
        loading = true
        ventas = ventaRepository.mostrarVentasDelMes()
        loading = false
    }

    val monthNamesEs = listOf(
        "Enero", "Febrero", "Marzo", "Abril", "Mayo", "Junio",
        "Julio", "Agosto", "Septiembre", "Octubre", "Noviembre", "Diciembre"
    )

    val currentMonth = remember {
        val now = Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault())
        monthNamesEs[now.monthNumber - 1]
    }

    val grouped: List<Pair<ClientGroupSell, List<Venta>>> = ventas
        .groupBy { it.ventaCliente }
        .map { (clienteNombre, ventasCliente) ->

            val totalKilos = ventasCliente.sumOf { venta ->
                venta.ventaPesoTotal?.toIntOrNull()
                    ?: venta.ventaBigbags.sumOf { it.ventaBbWeight.toIntOrNull() ?: 0 }
            }

            val totalBigBags = ventasCliente.sumOf { it.ventaBigbags.size }

            ClientGroupSell(
                cliente = com.alius.gmrstockplus.domain.model.Cliente(cliNombre = clienteNombre),
                totalVentasMes = ventasCliente.size,
                totalKilosVendidos = totalKilos,
                totalBigBags = totalBigBags
            ) to ventasCliente
        }
        .sortedByDescending { it.first.totalKilosVendidos }

    Box(modifier = Modifier.fillMaxSize()) {
        if (loading) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(
                    color = com.alius.gmrstockplus.ui.theme.PrimaryColor
                )
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                item {
                    Column(modifier = Modifier.fillMaxWidth()) {
                        Spacer(modifier = Modifier.height(50.dp))

                        Text(
                            text = "Top clientes en $currentMonth",
                            style = MaterialTheme.typography.titleLarge.copy(
                                fontSize = 26.sp,
                                fontWeight = FontWeight.Bold
                            ),
                            color = MaterialTheme.colorScheme.secondary
                        )

                        Spacer(modifier = Modifier.height(4.dp))

                        Text(
                            text = "Número de clientes: ${grouped.size}",
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.Medium
                            ),
                            color = Color.Gray,
                            modifier = Modifier.padding(bottom = 12.dp)
                        )
                    }
                }

                items(grouped) { group ->
                    ClientGroupSellCard(group = group.first) { clickedGroup ->
                        selectedClientGroup = clickedGroup
                        selectedClientVentas =
                            grouped.find { it.first == clickedGroup }?.second ?: emptyList()

                        coroutineScope.launch {
                            bottomSheetState.show()
                        }
                    }
                }
            }

            selectedClientGroup?.let { clientGroup ->
                ModalBottomSheet(
                    onDismissRequest = {
                        coroutineScope.launch {
                            bottomSheetState.hide()
                            selectedClientGroup = null
                        }
                    },
                    sheetState = bottomSheetState,
                    modifier = Modifier.fillMaxHeight(0.75f)
                ) {
                    GroupClientBottomSheetContent(
                        cliente = clientGroup.cliente,
                        ventas = selectedClientVentas,
                        plantId = plantId, // 🔑 Cambiado: databaseUrl -> plantId
                        onDismissRequest = {
                            coroutineScope.launch {
                                bottomSheetState.hide()
                                selectedClientGroup = null
                            }
                        }
                    )
                }
            }
        }
    }
}
