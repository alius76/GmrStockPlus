package com.alius.gmrstockplus.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.alius.gmrstockplus.domain.model.TraceEvent
import com.alius.gmrstockplus.domain.model.TraceEventType
import com.alius.gmrstockplus.ui.theme.PrimaryColor
import com.alius.gmrstockplus.core.utils.formatInstant
import com.alius.gmrstockplus.core.utils.formatWeight

@Composable
fun TraceEventCard(event: TraceEvent) {
    var expanded by remember { mutableStateOf(false) }

    // El color del icono sigue dependiendo del tipo para mantener la ayuda visual visual rápida
    val iconColor = when (event.type) {
        TraceEventType.CREACION -> Color(0xFF4CAF50)
        TraceEventType.VENTA -> Color(0xFF2196F3)
        TraceEventType.REPROCESO -> Color(0xFF9C27B0)
        TraceEventType.DEVOLUCION -> Color(0xFFFF9800)
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 4.dp)
            .clickable { expanded = !expanded },
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                // Indicador circular de tipo de evento
                Box(
                    modifier = Modifier
                        .size(10.dp)
                        .background(iconColor, shape = RoundedCornerShape(50))
                )

                Spacer(Modifier.width(12.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = event.title,
                        fontWeight = FontWeight.Bold,
                        fontSize = 17.sp,
                        color = Color.Black
                    )
                    Text(
                        text = event.subtitle,
                        fontSize = 13.sp,
                        color = Color.Gray
                    )
                }

                // 🔹 Badge de Fecha con PrimaryColor transparente
                Surface(
                    color = PrimaryColor.copy(alpha = 0.1f),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text(
                        text = formatInstant(event.date),
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = PrimaryColor
                    )
                }
            }

            Spacer(Modifier.height(16.dp))

            // Fila de Peso Total
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                val totalWeightFormatted = event.totalWeight.toDoubleOrNull() ?: 0.0
                Text(
                    text = "Peso total: ",
                    fontSize = 14.sp,
                    color = Color.Gray
                )
                Text(
                    text = "${formatWeight(totalWeightFormatted)} kg",
                    fontWeight = FontWeight.Bold,
                    color = PrimaryColor,
                    fontSize = 16.sp
                )
            }

            if (expanded) {
                HorizontalDivider(
                    modifier = Modifier.padding(vertical = 12.dp),
                    thickness = 0.5.dp,
                    color = Color.LightGray
                )

                Text(
                    "Lista completa:",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = Color.DarkGray,
                    modifier = Modifier.padding(bottom = 8.dp)
                )

                event.bigBags.forEach { bb ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp)
                            .background(Color(0xFFF9F9F9), RoundedCornerShape(6.dp))
                            .padding(horizontal = 10.dp, vertical = 6.dp),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("BigBag: ${bb.number}", fontSize = 13.sp, color = Color.Black)

                        val bbWeightFormatted = bb.weight.toDoubleOrNull() ?: 0.0
                        Text(
                            text = "${formatWeight(bbWeightFormatted)} kg",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            color = PrimaryColor
                        )
                    }
                }
            }
        }
    }
}