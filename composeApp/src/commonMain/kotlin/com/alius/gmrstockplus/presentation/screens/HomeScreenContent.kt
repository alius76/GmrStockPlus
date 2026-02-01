package com.alius.gmrstockplus.presentation.screens

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import cafe.adriel.voyager.core.screen.Screen
import com.alius.gmrstockplus.domain.model.User

class HomeScreenContent(
    private val user: User,
    private val plantId: String, // 🔑 Recibido de HomeTab
    private val onChangeDatabase: () -> Unit,
    private val onLogoutClick: () -> Unit
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
            Text(
                text = "Bienvenido, ${user.email}",
                style = MaterialTheme.typography.headlineSmall
            )

            Text(
                text = "Planta activa: $plantId",
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.primary
            )

            Spacer(modifier = Modifier.height(32.dp))

            // Botón para cambiar de planta (vuelve a la DatabaseSelectionScreen)
            OutlinedButton(
                onClick = onChangeDatabase,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("CAMBIAR DE PLANTA")
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Botón para cerrar sesión (vuelve al Login)
            Button(
                onClick = onLogoutClick,
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.error
                )
            ) {
                Text("CERRAR SESIÓN")
            }
        }
    }
}