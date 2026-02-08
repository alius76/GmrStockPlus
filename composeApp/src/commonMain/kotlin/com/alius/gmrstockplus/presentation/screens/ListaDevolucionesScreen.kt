package com.alius.gmrstockplus.presentation.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.navigator.LocalNavigator
import cafe.adriel.voyager.navigator.currentOrThrow
import com.alius.gmrstockplus.data.getDevolucionRepository
import com.alius.gmrstockplus.domain.model.Devolucion
import com.alius.gmrstockplus.ui.components.DevolucionCard
import com.alius.gmrstockplus.ui.theme.BackgroundColor
import com.alius.gmrstockplus.ui.theme.PrimaryColor
import kotlinx.coroutines.launch

// 1. Sustituimos databaseUrl por plantId
class ListaDevolucionesScreen(private val plantId: String) : Screen {

    @Composable
    override fun Content() {
        val navigator = LocalNavigator.currentOrThrow
        val coroutineScope = rememberCoroutineScope()

        // 2. Inicializamos el repositorio usando la "llave" de la planta
        val devolucionRepository = remember(plantId) { getDevolucionRepository(plantId) }

        var devoluciones by remember { mutableStateOf<List<Devolucion>>(emptyList()) }
        var isLoading by remember { mutableStateOf(true) }
        var errorMessage by remember { mutableStateOf<String?>(null) }

        // 🚀 Carga de datos al inicio
        LaunchedEffect(plantId) { // Reaccionamos si cambia la planta
            isLoading = true
            errorMessage = null
            try {
                // El SDK gestiona la conexión interna según el plantId inyectado en el repo
                val fetchedDevoluciones = devolucionRepository.obtenerTodasLasDevoluciones()

                // Gracias al mapeo manual del repo, devolucionFecha es un Instant real y ordena bien
                devoluciones = fetchedDevoluciones.sortedByDescending { it.devolucionFecha }
            } catch (e: Exception) {
                errorMessage = "Error al cargar las devoluciones: ${e.message}"
            } finally {
                isLoading = false
            }
        }

        Scaffold(
            modifier = Modifier.fillMaxSize().background(BackgroundColor),
            topBar = {
                // --- HEADER ---
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(BackgroundColor)
                        .padding(horizontal = 16.dp, vertical = 12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(onClick = { navigator.pop() }) {
                        Icon(
                            Icons.Default.ArrowBack,
                            contentDescription = "Volver",
                            tint = PrimaryColor
                        )
                    }
                    Column(modifier = Modifier.padding(start = 8.dp)) {
                        Text(
                            text = "Historial",
                            fontSize = 26.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.secondary
                        )
                        Text(
                            text = "Todas las devoluciones registradas",
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Medium),
                            color = Color.Gray,
                            modifier = Modifier.padding(top = 2.dp)
                        )
                    }
                }
            }
        ) { paddingValues ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .background(BackgroundColor)
            ) {
                if (isLoading) {
                    CircularProgressIndicator(
                        modifier = Modifier.align(Alignment.Center),
                        color = PrimaryColor
                    )
                } else if (errorMessage != null) {
                    Text(
                        text = errorMessage!!,
                        color = Color.Red,
                        modifier = Modifier.align(Alignment.Center).padding(24.dp)
                    )
                } else if (devoluciones.isEmpty()) {
                    Text(
                        text = "No se encontraron devoluciones.",
                        color = Color.Gray,
                        modifier = Modifier.align(Alignment.Center).padding(24.dp)
                    )
                } else {
                    // --- Lista de Devoluciones ---
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(top = 8.dp, bottom = 16.dp)
                    ) {
                        items(
                            items = devoluciones,
                            // Usamos una combinación única para la clave
                            key = { "${it.devolucionLote}_${it.devolucionFecha}" }
                        ) { devolucion ->
                            DevolucionCard(devolucion = devolucion)
                        }
                    }
                }
            }
        }
    }
}