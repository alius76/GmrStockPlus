package com.alius.gmrstockplus.presentation.screens

import androidx.compose.foundation.layout.*
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Storage
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.alius.gmrstockplus.data.getLoteRepository
import com.alius.gmrstockplus.domain.model.User

@Composable
fun BatchScreenContent(user: User, plantId: String) {
    // Probamos a instanciar el repositorio con el parámetro recibido
    val repository = remember(plantId) { getLoteRepository(plantId) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            imageVector = Icons.Default.Storage,
            contentDescription = null,
            modifier = Modifier.size(64.dp),
            tint = MaterialTheme.colors.primary
        )

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = "Prueba de Parámetros",
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold
        )

        Spacer(modifier = Modifier.height(24.dp))

        // Card para mostrar los datos de depuración
        Card(
            elevation = 4.dp,
            backgroundColor = Color(0xFFF5F5F5),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(text = "👤 Usuario: ${user.email}", fontWeight = FontWeight.Medium)
                Divider(modifier = Modifier.padding(vertical = 8.dp))
                Text(text = "🏭 Planta ID: $plantId", color = Color.Blue, fontWeight = FontWeight.Bold)
                Divider(modifier = Modifier.padding(vertical = 8.dp))
                Text(
                    text = "🛠 Repo: ${repository::class.simpleName}",
                    style = MaterialTheme.typography.caption
                )
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        Text(
            text = "Si ves el ID '$plantId' arriba, la navegación es correcta.",
            style = MaterialTheme.typography.body2,
            color = Color.Gray
        )
    }
}

