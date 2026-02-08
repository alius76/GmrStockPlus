package com.alius.gmrstockplus.ui.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarToday // 🆕 Icono para Fecha
import androidx.compose.material.icons.filled.ListAlt // 🆕 Icono para Lote
import androidx.compose.material.icons.filled.Scale
import androidx.compose.material.icons.filled.Widgets
import androidx.compose.material.icons.outlined.ShoppingBag
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.alius.gmrstockplus.domain.model.Venta
import com.alius.gmrstockplus.core.utils.formatInstant
import com.alius.gmrstockplus.core.utils.formatWeight
import com.alius.gmrstockplus.ui.theme.PrimaryColor
import com.alius.gmrstockplus.ui.theme.SecondaryColor
import com.alius.gmrstockplus.ui.theme.TextPrimary
import com.alius.gmrstockplus.ui.theme.TextSecondary

@Composable
fun VentaItem(venta: Venta, modifier: Modifier = Modifier) {
    var showDialog by remember { mutableStateOf(false) }

    // Conversión segura del String a Number
    val totalWeightNumber = venta.ventaPesoTotal?.toDoubleOrNull() ?: 0.0

    Card(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp, horizontal = 12.dp)
            .clickable { showDialog = true },
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 6.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            // --- 1. Título principal: Cliente ---
            Text(
                text = venta.ventaCliente,
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.ExtraBold,
                color = PrimaryColor,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )

            // --- Separador visual ---
            Spacer(modifier = Modifier.height(8.dp))
            Divider(color = TextSecondary.copy(alpha = 0.2f), thickness = 1.dp)
            Spacer(modifier = Modifier.height(12.dp))

            // --- 2. Fila de Métricas Clave (Peso y BigBags) ---
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceAround,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Métrica 1: Peso Total
                MetricItem(
                    icon = Icons.Default.Scale,
                    label = "Peso Total",
                    value = "${formatWeight(totalWeightNumber)} Kg",
                    iconColor = SecondaryColor
                )

                // Métrica 2: BigBags
                MetricItem(
                    icon = Icons.Outlined.ShoppingBag,
                    label = "BigBags",
                    value = venta.ventaBigbags.size.toString(),
                    iconColor = Color(0xFF00BFA5)
                )
            }


            Spacer(modifier = Modifier.height(20.dp))

            // --- 3. Información Detallada (Lote, Material, Fecha) ⬅️ DISEÑO MODIFICADO ---
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp),
                verticalArrangement = Arrangement.spacedBy(6.dp) // Espaciado entre detalles
            ) {

                // Lote (Ícono y Texto)
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.ListAlt, // Icono para Lote/ID
                        contentDescription = "Lote",
                        tint = PrimaryColor.copy(alpha = 0.7f),
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Lote: ${venta.ventaLote}",
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                // Material (Ícono y Texto)
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Widgets,
                        contentDescription = "Material",
                        tint = PrimaryColor.copy(alpha = 0.7f),
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Material: ${venta.ventaMaterial}",
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = TextSecondary,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                // Fecha (Ícono y Texto)
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.CalendarToday, // Icono para Fecha
                        contentDescription = "Fecha",
                        tint = PrimaryColor.copy(alpha = 0.7f),
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Fecha: ${formatInstant(venta.ventaFecha)}",
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = TextSecondary
                    )
                }
            }
        }
    }

    if (showDialog) {
        AlertDialog(
            onDismissRequest = { showDialog = false },
            confirmButton = {
                TextButton(onClick = { showDialog = false }) {
                    Text("Cerrar", color = PrimaryColor)
                }
            },
            title = {
                Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                    Text(
                        text = "Lista de BigBags",
                        color = PrimaryColor,
                        fontWeight = FontWeight.Bold,
                        fontSize = 20.sp
                    )
                }
            },
            text = {
                VentaBigBagsDialogContent(venta.ventaBigbags)
            }
        )
    }
}

