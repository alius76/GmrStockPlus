package com.alius.gmrstockplus.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.alius.gmrstockplus.domain.model.Comanda
import com.alius.gmrstockplus.ui.theme.PrimaryColor
import com.alius.gmrstockplus.core.utils.formatWeight
import com.alius.gmrstockplus.ui.theme.ReservedColor
import com.alius.gmrstockplus.ui.theme.WarningColor

@Composable
fun SalidasProgramadasComandaCard(
    comanda: Comanda,
    modifier: Modifier = Modifier
) {
    // --- LÓGICA DE ESTADOS (Mantenemos tu nueva lógica de materiales) ---
    val isCargada = comanda.fueVendidoComanda

    val todoAsignado = remember(comanda.listaAsignaciones) {
        comanda.listaAsignaciones.isNotEmpty() &&
                comanda.listaAsignaciones.all { it.numeroLote.isNotBlank() }
    }

    val totalAsignacionesConLote = comanda.listaAsignaciones.count { it.numeroLote.isNotBlank() }
    val asignacionesVendidas = comanda.listaAsignaciones.count { it.fueVendido }
    val esVentaParcial = asignacionesVendidas > 0 && asignacionesVendidas < totalAsignacionesConLote

    val isLoteAsignado = todoAsignado || (comanda.numberLoteComanda.isNotBlank() && comanda.listaAsignaciones.isEmpty())

    // Definición de colores y etiquetas
    val (statusColor, statusText) = when {
        isCargada -> Color.Gray to "CARGADO"
        esVentaParcial -> PrimaryColor to "PARCIAL"
        isLoteAsignado -> PrimaryColor to "ASIGNADO"
        else -> WarningColor to "PENDIENTE"
    }

    // Card Original: Sin onClick, sin bordes de selección, elevación estándar
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 6.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {

            // FILA 1: ESTADO Y N° COMANDA
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                val formattedComandaNumber = comanda.numeroDeComanda.toString().padStart(6, '0')
                Text(
                    text = "#$formattedComandaNumber",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = if (isCargada) Color.Gray else PrimaryColor
                )

                // Estado (Originalmente usaba ElevatedCard o Surface)
                Surface(
                    color = statusColor.copy(alpha = 0.8f),
                    shape = RoundedCornerShape(8.dp),
                    shadowElevation = 2.dp
                ) {
                    Text(
                        text = statusText,
                        color = Color.White,
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 12.sp,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))
            HorizontalDivider(color = PrimaryColor.copy(alpha = 0.3f))
            Spacer(modifier = Modifier.height(12.dp))

            // FILA 2: Cliente
            Text(
                text = comanda.bookedClientComanda?.cliNombre ?: "Cliente no especificado",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = if (isCargada) Color.Gray else Color.DarkGray
            )

            Spacer(modifier = Modifier.height(6.dp))

            // FILA 3: Listado de Materiales (La gran mejora)
            if (comanda.listaAsignaciones.isNotEmpty()) {
                comanda.listaAsignaciones.forEach { asig ->
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = "• ${asig.materialNombre}",
                            fontSize = 15.sp,
                            color = if (asig.fueVendido) Color.Gray else MaterialTheme.colorScheme.onSurfaceVariant,
                            fontWeight = if (asig.numeroLote.isNotBlank()) FontWeight.Bold else FontWeight.Medium
                        )
                        if (asig.numeroLote.isNotBlank()) {
                            Text(
                                text = " [${asig.numeroLote}]",
                                fontSize = 14.sp,
                                color = if (asig.fueVendido) Color.Gray else PrimaryColor,
                                fontWeight = FontWeight.ExtraBold
                            )
                        }
                    }
                }
            } else {
                Text(
                    text = "Material: ${comanda.descriptionLoteComanda ?: "N/A"}",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Medium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            // FILA 4: Peso
            Row(verticalAlignment = Alignment.CenterVertically) {
                val weight = comanda.totalWeightComanda.toDoubleOrNull() ?: 0.0
                Text(
                    text = "Peso total: ${formatWeight(weight)} Kg",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            // FILA 5: Lote Asignado (Mantenemos tu lógica visual original)
            if (comanda.numberLoteComanda.isNotBlank() && comanda.listaAsignaciones.isEmpty()) {
                Text(
                    text = "Lote: ${comanda.numberLoteComanda}",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            } else if (!isCargada && !isLoteAsignado && comanda.listaAsignaciones.isEmpty()) {
                Text(
                    text = "Lote: SIN ASIGNAR",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = ReservedColor
                )
            }

            // FILA 6: Observaciones
            if (!comanda.remarkComanda.isNullOrBlank()) {
                Spacer(modifier = Modifier.height(8.dp))
                HorizontalDivider(color = Color.LightGray.copy(alpha = 0.5f))
                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = "Observaciones:",
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = comanda.remarkComanda,
                    fontSize = 14.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}