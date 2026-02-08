package com.alius.gmrstockplus.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.alius.gmrstockplus.domain.model.VentaBigbag

@Composable
fun VentaBigBagsDialogContent(bigBags: List<VentaBigbag>) {
    val primaryColor = Color(0xFF029083)

    Column(modifier = Modifier.fillMaxWidth()) {
        if (bigBags.isEmpty()) {
            Text(
                text = "No hay BigBags para esta venta.",
                modifier = Modifier.padding(16.dp),
                fontWeight = FontWeight.Medium,
                color = Color.Gray,
                fontSize = 16.sp
            )
        } else {
            // --- Header ---
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
                        modifier = Modifier.weight(0.3f)
                    )
                    Text(
                        text = "Peso (Kg)",
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp,
                        color = primaryColor,
                        modifier = Modifier.weight(0.3f),
                        textAlign = androidx.compose.ui.text.style.TextAlign.End
                    )
                    Text(
                        text = "Estado",
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp,
                        color = primaryColor,
                        modifier = Modifier.weight(0.4f),
                        textAlign = androidx.compose.ui.text.style.TextAlign.Center
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
                    VentaBigBagListItem(bigBag, primaryColor)
                }
            }
        }
    }
}

@Composable
fun VentaBigBagListItem(bigBag: VentaBigbag, primaryColor: Color) {
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
            // Número
            Text(
                text = bigBag.ventaBbNumber,
                fontWeight = FontWeight.Bold,
                fontSize = 16.sp,
                color = primaryColor,
                modifier = Modifier.weight(0.3f),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )

            // Peso
            Box(modifier = Modifier.weight(0.3f), contentAlignment = Alignment.CenterEnd) {
                Text(
                    text = "${bigBag.ventaBbWeight} Kg",
                    fontSize = 14.sp,
                    color = Color.DarkGray,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }

            // Estado con ícono
            Box(
                modifier = Modifier.weight(0.4f),
                contentAlignment = Alignment.Center
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    val statusText = "Entregado"
                    val statusColor = Color(0xFF388E3C) // verde
                    val statusIcon = Icons.Default.CheckCircle

                    Icon(
                        imageVector = statusIcon,
                        contentDescription = statusText,
                        tint = statusColor,
                        modifier = Modifier.size(18.dp)
                    )
                    Text(
                        text = statusText,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = statusColor,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }
        }
    }
}
