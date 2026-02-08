package com.alius.gmrstockplus.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ViewList
import androidx.compose.material.icons.filled.Assessment
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.alius.gmrstockplus.core.utils.formatInstant
import com.alius.gmrstockplus.core.utils.formatWeight
import com.alius.gmrstockplus.domain.model.Certificado
import com.alius.gmrstockplus.domain.model.CertificadoStatus
import com.alius.gmrstockplus.domain.model.Venta
import com.alius.gmrstockplus.ui.theme.PrimaryColor
import com.alius.gmrstockplus.ui.theme.TextSecondary

@Composable
fun VentaRow(
    venta: Venta,
    certificado: Certificado?
) {
    var showCertificadoDialog by remember { mutableStateOf(false) }
    var showBigbagsDialog by remember { mutableStateOf(false) }

    val certificadoIconColor = when (certificado?.status) {
        CertificadoStatus.CORRECTO -> PrimaryColor
        CertificadoStatus.ADVERTENCIA -> MaterialTheme.colorScheme.error
        else -> Color.Gray.copy(alpha = 0.5f)
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp),
        shape = RoundedCornerShape(12.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        border = BorderStroke(1.dp, Color.LightGray.copy(alpha = 0.3f))
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = formatInstant(venta.ventaFecha ?: kotlinx.datetime.Instant.DISTANT_PAST),
                    fontSize = 12.sp,
                    color = TextSecondary
                )
                Text(
                    text = venta.ventaMaterial.ifEmpty { "Sin material" },
                    fontWeight = FontWeight.Bold,
                    fontSize = 15.sp,
                    color = MaterialTheme.colorScheme.secondary
                )
                Text(
                    text = "Lote: ${venta.ventaLote}",
                    fontSize = 13.sp,
                    color = Color.Gray
                )
            }

            Text(
                text = "${formatWeight(venta.ventaPesoTotal?.toDoubleOrNull() ?: 0.0)} Kg",
                fontWeight = FontWeight.Black,
                color = PrimaryColor,
                fontSize = 16.sp,
                modifier = Modifier.padding(horizontal = 8.dp)
            )

            IconButton(onClick = { showCertificadoDialog = true }) {
                Icon(Icons.Default.Assessment, "Certificado", tint = certificadoIconColor, modifier = Modifier.size(28.dp))
            }

            IconButton(onClick = { showBigbagsDialog = true }) {
                Icon(Icons.AutoMirrored.Filled.ViewList, "BigBags", tint = PrimaryColor, modifier = Modifier.size(28.dp))
            }
        }
    }

    // Diálogo Certificado
    if (showCertificadoDialog) {
        Dialog(onDismissRequest = { showCertificadoDialog = false }) {
            Card(shape = RoundedCornerShape(20.dp)) {
                CertificadoDialogContent(venta.ventaLote, certificado) { showCertificadoDialog = false }
            }
        }
    }

    // Diálogo BigBags
    if (showBigbagsDialog) {
        AlertDialog(
            onDismissRequest = { showBigbagsDialog = false },
            confirmButton = {
                TextButton(onClick = { showBigbagsDialog = false }) { Text("Cerrar", color = PrimaryColor) }
            },
            title = { Text("BigBags Entregados", color = PrimaryColor, fontWeight = FontWeight.Bold) },
            text = { VentaBigBagsDialogContent(bigBags = venta.ventaBigbags) }
        )
    }
}

