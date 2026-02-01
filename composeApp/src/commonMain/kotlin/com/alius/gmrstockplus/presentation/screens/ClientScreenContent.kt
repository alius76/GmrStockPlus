package com.alius.gmrstockplus.presentation.screens

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import cafe.adriel.voyager.core.screen.Screen
import com.alius.gmrstockplus.domain.model.User

class ClientScreenContent(
    private val user: User,
    private val plantId: String // 🔑 Recibido de ClientTab
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
                imageVector = androidx.compose.material.icons.Icons.Default.BarChart,
                contentDescription = null,
                modifier = Modifier.size(64.dp),
                tint = MaterialTheme.colorScheme.primary
            )

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = "Ranking de Clientes",
                style = MaterialTheme.typography.headlineSmall
            )

            Text(
                text = "Consultando datos de: $plantId",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.secondary
            )

            Spacer(modifier = Modifier.height(24.dp))

            // Placeholder para la futura lista de ranking
            Card(
                modifier = Modifier.fillMaxWidth().padding(8.dp)
            ) {
                Box(
                    modifier = Modifier.padding(24.dp).fillMaxWidth(),
                    contentAlignment = Alignment.Center
                ) {
                    Text("Aquí se mostrará el Ranking de la $plantId")
                }
            }
        }
    }
}