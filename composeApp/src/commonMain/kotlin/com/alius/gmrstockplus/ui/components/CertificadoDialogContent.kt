package com.alius.gmrstockplus.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Warning
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
import com.alius.gmrstockplus.domain.model.Certificado
import com.alius.gmrstockplus.domain.model.CertificadoStatus
import com.alius.gmrstockplus.domain.model.Parametro // Importamos Parametro directamente
import com.alius.gmrstockplus.ui.theme.PrimaryColor

@Composable
fun CertificadoDialogContent(
    loteNumber: String,
    certificado: Certificado?,
    onClose: () -> Unit
) {
    Column(
        modifier = Modifier
            .background(MaterialTheme.colorScheme.surface)
            .padding(20.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        val (icon, estadoText, estadoColor) = when (certificado?.status) {
            CertificadoStatus.ADVERTENCIA -> Triple(Icons.Default.Warning, "Advertencia", MaterialTheme.colorScheme.error)
            CertificadoStatus.CORRECTO -> Triple(Icons.Default.CheckCircle, "Correcto", PrimaryColor)
            else -> Triple(Icons.Default.Description, "Sin Datos", MaterialTheme.colorScheme.onSurfaceVariant)
        }

        Icon(icon, contentDescription = estadoText, tint = estadoColor, modifier = Modifier.size(48.dp))

        Text(
            text = "Certificado de $loteNumber",
            color = PrimaryColor,
            fontWeight = FontWeight.ExtraBold,
            fontSize = 20.sp,
            textAlign = TextAlign.Center
        )

        HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)

        if (certificado == null || certificado.parametros.isEmpty()) {
            Text(
                text = "No se encontraron datos analíticos para este lote.",
                color = Color.Gray,
                fontSize = 14.sp,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(vertical = 20.dp)
            )
        } else {
            certificado.parametros.forEach { parametro ->
                ParametroRow(parametro)
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        TextButton(
            onClick = onClose,
            modifier = Modifier.align(Alignment.End)
        ) {
            Text("Cerrar", color = PrimaryColor, fontWeight = FontWeight.Bold)
        }
    }
}

@Composable
private fun ParametroRow(parametro: Parametro) { // Nombre de clase corregido aquí
    Column(modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(
                text = parametro.descripcion,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.weight(1f),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Row(verticalAlignment = Alignment.CenterVertically) {
                if (parametro.warning) {
                    Icon(Icons.Default.Warning, "Aviso", tint = Color(0xFFE53935), modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                }
                Text(
                    text = parametro.valor,
                    color = if (parametro.warning) Color(0xFFE53935) else Color.Unspecified,
                    fontWeight = if (parametro.warning) FontWeight.Bold else FontWeight.Normal,
                    fontSize = 14.sp
                )
            }
        }

        val rangoText = parametro.rango?.let { r ->
            "Rango: ${r.valorMin ?: "N/A"} - ${r.valorMax ?: "N/A"} ${parametro.unidad}"
        } ?: "Rango: N/A"

        Text(rangoText, color = Color.Gray, fontSize = 11.sp)
    }
}