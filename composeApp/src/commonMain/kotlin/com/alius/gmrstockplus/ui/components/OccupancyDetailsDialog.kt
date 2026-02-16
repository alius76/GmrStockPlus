package com.alius.gmrstockplus.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.Inventory2
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.alius.gmrstockplus.domain.model.OccupancyInfo
import com.alius.gmrstockplus.ui.theme.PrimaryColor

@Composable
fun OccupancyDetailsDialog(
    loteNumber: String,
    occupancyList: List<OccupancyInfo>,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text(
                    text = "Cerrar",
                    color = PrimaryColor,
                    fontWeight = FontWeight.Bold
                )
            }
        },
        title = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "Asignaciones activas",
                    style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                    textAlign = TextAlign.Center
                )
                Text(
                    text = "Lote: $loteNumber",
                    style = MaterialTheme.typography.bodyMedium,
                    color = PrimaryColor,
                    fontWeight = FontWeight.Bold
                )
            }
        },
        text = {
            if (occupancyList.isEmpty()) {
                Box(
                    modifier = Modifier.fillMaxWidth().padding(24.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "No hay asignaciones para este lote.",
                        textAlign = TextAlign.Center,
                        color = Color.Gray,
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            } else {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    occupancyList.forEach { info ->
                        OccupancyCardItem(info)
                    }
                }
            }
        },
        shape = RoundedCornerShape(28.dp),
        containerColor = MaterialTheme.colorScheme.surface
    )
}

@Composable
private fun OccupancyCardItem(info: OccupancyInfo) {
    Card(
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        shape = RoundedCornerShape(16.dp),
        border = BorderStroke(1.dp, Color.LightGray.copy(alpha = 0.3f))
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            // --- FILA SUPERIOR: BADGE COMANDA (IZQ) Y CLIENTE (DER) ---
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Surface(
                    color = PrimaryColor,
                    shape = RoundedCornerShape(4.dp)
                ) {
                    Text(
                        text = "#${info.numeroComanda.padStart(6, '0')}",
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                        color = Color.White,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

                Text(
                    text = info.cliente.uppercase(),
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.ExtraBold,
                    color = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.weight(1f),
                    textAlign = TextAlign.End,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }

            Spacer(modifier = Modifier.height(12.dp))
            HorizontalDivider(thickness = 0.5.dp, color = Color.LightGray.copy(alpha = 0.5f))
            Spacer(modifier = Modifier.height(12.dp))

            // --- CUERPO: CANTIDAD (donde antes iba Nº Comanda) Y FECHA ---
            Row(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.weight(1f)) {
                    Text("CANTIDAD", style = MaterialTheme.typography.labelSmall, color = Color.Gray)
                    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(top = 2.dp)) {
                        Icon(Icons.Default.Inventory2, null, Modifier.size(14.dp), PrimaryColor)
                        Spacer(Modifier.width(6.dp))
                        Text(
                            text = "${info.cantidad} BB",
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Bold,
                            color = PrimaryColor
                        )
                    }
                }
                Column(modifier = Modifier.weight(1f)) {
                    Text("FECHA RESERVA", style = MaterialTheme.typography.labelSmall, color = Color.Gray)
                    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(top = 2.dp)) {
                        Icon(Icons.Default.CalendarToday, null, Modifier.size(14.dp), Color.Gray)
                        Spacer(Modifier.width(6.dp))
                        Text(info.fecha, style = MaterialTheme.typography.bodyMedium)
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // --- PIE: USUARIO ---
            Surface(
                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                shape = RoundedCornerShape(8.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(Icons.Default.Person, null, Modifier.size(14.dp), Color.Gray)
                    Spacer(Modifier.width(8.dp))
                    Text(
                        text = "Asignado por: ${info.usuario.split("@").first()}",
                        style = MaterialTheme.typography.labelSmall,
                        color = Color.DarkGray,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }
        }
    }
}