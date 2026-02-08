package com.alius.gmrstockplus.ui.components

import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Divider
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
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


@Composable
fun SalidasProgramadasComandaCard(
    comanda: Comanda,
    onClick: () -> Unit,
    isSelected: Boolean,
    modifier: Modifier = Modifier
) {
    // Determinar si fue vendida/cargada
    val isCargada = comanda.fueVendidoComanda

    // Color de fondo (Tu lógica original, se mantiene)
    val backgroundColor = if (isCargada) Color.White else Color.White

    // --- Colores Definidos ---
    // 1. Color para el número de comanda (siempre PrimaryColor)
    val comandaNumberColor = PrimaryColor

    // 2. Color para el ElevatedCard de estado (CARGADO vs PENDIENTE)
    val statusCardColor = if (isCargada) PrimaryColor else MaterialTheme.colorScheme.secondary

    // 3. Color para el borde de selección (Usando PrimaryColor para consistencia en la selección)
    val borderColor = if (isSelected) PrimaryColor else Color.LightGray.copy(alpha = 0.5f)
    val borderWidth = if (isSelected) 3.dp else 1.dp

    Card(
        onClick = onClick, // Habilita el click y el ripple effect
        modifier = modifier
            .fillMaxWidth()
            .border(borderWidth, borderColor, RoundedCornerShape(16.dp)), // Aplicar borde condicional
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = backgroundColor),
        elevation = CardDefaults.cardElevation(defaultElevation = 6.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {

            // FILA 1: ESTADO Y N° COMANDA
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Título de la Comanda formateado
                val formattedComandaNumber = comanda.numeroDeComanda.toString().padStart(6, '0')
                Text(
                    text = "#$formattedComandaNumber",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = comandaNumberColor // 1. Siempre PrimaryColor
                )

                // Estado (Cargado / Pendiente)
                ElevatedCard(
                    colors = CardDefaults.cardColors(
                        // 2. Color definido por el estado
                        containerColor = statusCardColor.copy(alpha = 0.8f)
                    ),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                ) {
                    Text(
                        text = if (isCargada) "CARGADO" else "PENDIENTE",
                        color = Color.White,
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 12.sp,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))
            // 🔑 MODIFICACIÓN: Primer Divider siempre de PrimaryColor con transparencia
            Divider(color = PrimaryColor.copy(alpha = 0.3f))
            Spacer(modifier = Modifier.height(12.dp))

            // FILA 2: Cliente
            Text(
                text = comanda.bookedClientComanda?.cliNombre ?: "Cliente no especificado",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = Color.DarkGray
            )
            Spacer(modifier = Modifier.height(4.dp))

            // FILA 3: Material
            Text(
                text = "Material: ${comanda.descriptionLoteComanda ?: "N/A"}",
                fontSize = 16.sp,
                fontWeight = FontWeight.Medium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.fillMaxWidth()
            )

            // FILA 3.1: Peso (totalWeightComanda)
            Spacer(modifier = Modifier.height(4.dp))
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

            // FILA 4: Lote Asignado
            if (comanda.numberLoteComanda?.isNotBlank() == true) {
                Text(
                    text = "Lote: ${comanda.numberLoteComanda}",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.SemiBold,
                    // Lote Asignado -> MaterialTheme.colorScheme.onSurfaceVariant
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            } else if (!isCargada) {
                Text(
                    text = "Lote: SIN ASIGNAR",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    // Lote No Asignado -> ReservedColor
                    color = ReservedColor
                )
            }

            // FILA 5: Observaciones
            comanda.remarkComanda?.let { remark ->
                if (remark.isNotBlank()) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Divider(color = Color.LightGray.copy(alpha = 0.5f))
                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        text = "Observaciones:",
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = remark,
                        fontSize = 14.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}