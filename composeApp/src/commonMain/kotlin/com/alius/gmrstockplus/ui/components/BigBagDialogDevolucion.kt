package com.alius.gmrstockplus.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import com.alius.gmrstockplus.ui.theme.PrimaryColor

@Composable
fun BigBagDialogDevolucion(bigBags: List<com.alius.gmrstockplus.domain.model.DevolucionBigbag>) {
    val primaryColor = PrimaryColor

    Column(modifier = Modifier.fillMaxWidth()) {
        if (bigBags.isEmpty()) {
            Text(
                text = "No hay BigBags detallados para esta devolución.",
                modifier = Modifier.padding(16.dp),
                fontWeight = FontWeight.Medium,
                color = Color.Gray,
                fontSize = 16.sp
            )
        } else {
            // Header de la tabla
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = primaryColor.copy(alpha = 0.1f)),
                shape = RoundedCornerShape(topStart = 12.dp, topEnd = 12.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 12.dp, vertical = 10.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Número",
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp,
                        color = primaryColor,
                        modifier = Modifier.weight(0.3f) // Peso ajustado
                    )
                    Text(
                        text = "Peso (Kg)",
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp,
                        color = primaryColor,
                        modifier = Modifier.weight(0.35f), // Peso ajustado
                        textAlign = TextAlign.End
                    )
                    // 🆕 NUEVA COLUMNA: Estado
                    Text(
                        text = "Estado",
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp,
                        color = primaryColor,
                        modifier = Modifier.weight(0.35f), // Peso ajustado
                        textAlign = TextAlign.End
                    )
                }
            }

            Spacer(modifier = Modifier.height(4.dp))

            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(max = 300.dp),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                items(bigBags) { bigBag ->
                    DevolucionBigBagItem(bigBag, primaryColor)
                }
            }
        }
    }
}

@Composable
fun DevolucionBigBagItem(bigBag: com.alius.gmrstockplus.domain.model.DevolucionBigbag, primaryColor: Color) {
    val formattedWeight = com.alius.gmrstockplus.core.utils.formatWeight(
        bigBag.devolucionBbWeight.toDoubleOrNull() ?: 0.0
    )

    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        shape = RoundedCornerShape(8.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Número del BigBag
            Text(
                text = bigBag.devolucionBbNumber,
                fontWeight = FontWeight.Bold,
                fontSize = 16.sp,
                color = Color.DarkGray,
                modifier = Modifier.weight(0.3f), // Peso ajustado
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )

            // Peso
            Text(
                text = formattedWeight, // Ya tiene " Kg" en el formato, o lo puedes añadir aquí: "$formattedWeight Kg"
                fontSize = 16.sp,
                color = primaryColor,
                modifier = Modifier.weight(0.35f), // Peso ajustado
                textAlign = TextAlign.End,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )

            // 🆕 NUEVA COLUMNA: Estado (Fijo)
            Text(
                text = "Devuelto",
                fontSize = 16.sp,
                // Un color verde para el estado "Devuelto"
                color = Color(0xFF388E3C),
                modifier = Modifier.weight(0.35f), // Peso ajustado
                textAlign = TextAlign.End,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}
