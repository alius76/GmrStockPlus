package com.alius.gmrstockplus.ui.components

import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ViewList
import androidx.compose.material.icons.filled.Assessment
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Description // ⬅️ Nuevo ícono
import androidx.compose.material.icons.filled.Person      // ⬅️ Nuevo ícono
import androidx.compose.material.icons.filled.ViewList
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign // ⬅️ Importación necesaria para TextAlign.Center
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.alius.gmrstockplus.core.utils.formatInstant
import com.alius.gmrstockplus.core.utils.formatWeight // ⬅️ ¡Importación necesaria!
import com.alius.gmrstockplus.domain.model.Certificado
import com.alius.gmrstockplus.domain.model.CertificadoStatus
import com.alius.gmrstockplus.domain.model.Cliente
import com.alius.gmrstockplus.domain.model.Venta
import com.alius.gmrstockplus.ui.theme.PrimaryColor

@Composable
fun ClientCard(
    cliente: Cliente,
    venta: Venta,
    certificado: Certificado?,
    plantId: String, // 🔑 Cambiado: databaseUrl -> plantId
    modifier: Modifier = Modifier
) {
    var showBigBagsDialog by remember { mutableStateOf(false) }
    var showCertificadoDialog by remember { mutableStateOf(false) }

    val cantidadBigBags = venta.ventaBigbags.size
    val pesoTotalDouble = venta.ventaBigbags.sumOf { it.ventaBbWeight.toDoubleOrNull() ?: 0.0 }

    Card(
        modifier = modifier
            .width(300.dp)
            .wrapContentHeight(),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 6.dp),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.2f)),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(
            modifier = Modifier
                .padding(20.dp)
                .animateContentSize()
        ) {

            // --- CABECERA ---
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = venta.ventaLote,
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.ExtraBold,
                    color = PrimaryColor,
                    textAlign = TextAlign.Center,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )

                Spacer(modifier = Modifier.height(12.dp))

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceEvenly,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    // Cliente
                    Icon(Icons.Default.Person, contentDescription = "Cliente", tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f), modifier = Modifier.size(32.dp))
                    // Documento
                    Icon(Icons.Default.Description, contentDescription = "Documento de venta", tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f), modifier = Modifier.size(32.dp))
                    // Certificado
                    val certificadoIconColor = when(certificado?.status) {
                        CertificadoStatus.CORRECTO -> PrimaryColor
                        CertificadoStatus.ADVERTENCIA -> MaterialTheme.colorScheme.error
                        else -> MaterialTheme.colorScheme.onSurfaceVariant
                    }
                    IconButton(onClick = { showCertificadoDialog = true }, modifier = Modifier.size(32.dp)) {
                        Icon(Icons.Default.Assessment, contentDescription = "Certificado", tint = certificadoIconColor, modifier = Modifier.fillMaxSize())
                    }
                    // BigBags
                    IconButton(onClick = { showBigBagsDialog = true }, modifier = Modifier.size(32.dp)) {
                        Icon(Icons.AutoMirrored.Filled.ViewList, contentDescription = "Ver BigBags", tint = PrimaryColor, modifier = Modifier.fillMaxSize())
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))
            Divider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.2f), thickness = 1.dp)
            Spacer(modifier = Modifier.height(12.dp))

            // --- DETALLES ---
            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                DetailRow("Cliente", cliente.cliNombre)
                DetailRow("Material", venta.ventaMaterial ?: "Sin material")
                DetailRow("Venta", formatInstant(venta.ventaFecha))
                DetailRow("BigBags", cantidadBigBags.toString())
                DetailRow("Peso total", "${formatWeight(pesoTotalDouble)} Kg", PrimaryColor)
            }
        }
    }

    // --- DIÁLOGO DE BIGBAGS ---
    if (showBigBagsDialog) {
        AlertDialog(
            onDismissRequest = { showBigBagsDialog = false },
            confirmButton = {
                TextButton(onClick = { showBigBagsDialog = false }) { Text("Cerrar", color = PrimaryColor) }
            },
            title = { Text("Lista de BigBags", color = PrimaryColor, fontWeight = FontWeight.Bold, fontSize = 20.sp) },
            text = { VentaBigBagsDialogContent(bigBags = venta.ventaBigbags) }
        )
    }

    // --- DIÁLOGO DE CERTIFICADO ---
    if (showCertificadoDialog) {
        Dialog(onDismissRequest = { showCertificadoDialog = false }) {
            Card(
                shape = RoundedCornerShape(20.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 8.dp),
                modifier = Modifier
                    .fillMaxWidth(0.95f)
                    .padding(16.dp)
            ) {
                Column(
                    modifier = Modifier
                        .background(MaterialTheme.colorScheme.surface)
                        .padding(20.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    val (icon, estadoText, estadoColor) = if (certificado != null) {
                        when (certificado.status) {
                            CertificadoStatus.ADVERTENCIA -> Triple(Icons.Default.Warning, "Advertencia", MaterialTheme.colorScheme.error)
                            CertificadoStatus.CORRECTO -> Triple(Icons.Default.CheckCircle, "Correcto", PrimaryColor)
                            else -> Triple(Icons.Default.Description, "Sin Datos", MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    } else {
                        Triple(Icons.Default.Description, "Sin Datos", MaterialTheme.colorScheme.onSurfaceVariant)
                    }

                    Icon(icon, contentDescription = estadoText, tint = estadoColor, modifier = Modifier.size(48.dp))
                    Text(
                        "Certificado de ${venta.ventaLote}",
                        color = PrimaryColor,
                        fontWeight = FontWeight.ExtraBold,
                        fontSize = 22.sp,
                        textAlign = TextAlign.Center
                    )

                    Divider(color = MaterialTheme.colorScheme.outlineVariant)

                    if (certificado != null) {
                        certificado.parametros.forEach { parametro ->
                            Column(modifier = Modifier.fillMaxWidth()) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Text(
                                        parametro.descripcion,
                                        fontWeight = FontWeight.Bold,
                                        modifier = Modifier.weight(1f),
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        if (parametro.warning) {
                                            Icon(
                                                Icons.Default.Warning,
                                                contentDescription = "Advertencia",
                                                tint = MaterialTheme.colorScheme.error,
                                                modifier = Modifier.size(18.dp)
                                            )
                                            Spacer(modifier = Modifier.width(4.dp))
                                        }
                                        Text(
                                            parametro.valor,
                                            color = if (parametro.warning) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurface,
                                            fontSize = 14.sp
                                        )
                                    }
                                }

                                val rangoTexto = parametro.rango?.let { rango ->
                                    if (rango.valorMin != null && rango.valorMax != null) {
                                        val min = if (rango.valorMin % 1.0 == 0.0) rango.valorMin.toInt() else rango.valorMin
                                        val max = if (rango.valorMax % 1.0 == 0.0) rango.valorMax.toInt() else rango.valorMax
                                        "Rango: ($min - $max ${parametro.unidad})"
                                    } else "Rango: N/A"
                                } ?: "Rango: N/A"
                                Text(rangoTexto, color = Color.Gray, fontSize = 12.sp, modifier = Modifier.padding(top = 4.dp))
                            }
                        }
                    } else {
                        // Mensaje cuando no existe certificado
                        Text(
                            "No se encontraron datos del certificado.",
                            color = Color.Gray,
                            fontSize = 14.sp,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }

                    Spacer(modifier = Modifier.height(12.dp))
                    TextButton(onClick = { showCertificadoDialog = false }, modifier = Modifier.align(Alignment.End)) {
                        Text("Cerrar", color = PrimaryColor)
                    }
                }
            }
        }
    }
}