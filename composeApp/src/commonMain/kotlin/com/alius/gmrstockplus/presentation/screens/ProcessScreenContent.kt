package com.alius.gmrstockplus.presentation.screens

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.filled.Autorenew
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import cafe.adriel.voyager.core.screen.Screen
import com.alius.gmrstockplus.domain.model.User

class ProcessScreenContent(
    private val user: User,
    private val plantId: String // 🔑 Recibido de ProcessTab
) : Screen {

    @Composable
    override fun Content() {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Icon(
                imageVector = androidx.compose.material.icons.Icons.Default.Autorenew,
                contentDescription = null,
                modifier = Modifier.size(64.dp),
                tint = MaterialTheme.colorScheme.primary
            )

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = "Procesos en Curso (WIP)",
                style = MaterialTheme.typography.headlineSmall
            )

            Text(
                text = "Planta: $plantId",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.secondary
            )

            Spacer(modifier = Modifier.height(24.dp))

            // Espacio para la futura lista de procesos/maquinaria
            LinearProgressIndicator(
                modifier = Modifier.fillMaxWidth().padding(horizontal = 32.dp),
                color = MaterialTheme.colorScheme.primary
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "Cargando estado de producción...",
                style = MaterialTheme.typography.labelSmall
            )
        }
    }
}