package com.alius.gmrstockplus.ui.components


import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.alius.gmrstockplus.domain.model.BigBags
import com.alius.gmrstockplus.ui.theme.PrimaryColor

@Composable
fun BigBagsDialogContent(bigBags: List<BigBags>) {
    val primaryColor = PrimaryColor

    Column(modifier = Modifier.fillMaxWidth()) {
        if (bigBags.isEmpty()) {
            Text(
                text = "No hay BigBags para este lote.",
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
                        modifier = Modifier.weight(0.4f)
                    )
                    Text(
                        text = "Peso (Kg)",
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp,
                        color = primaryColor,
                        modifier = Modifier.weight(0.4f)
                    )
                    Text(
                        text = "Estado",
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp,
                        color = primaryColor,
                        modifier = Modifier.weight(0.2f)
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
                    BigBagListItem(bigBag, primaryColor)
                }
            }
        }
    }
}

@Composable
fun BigBagListItem(bigBag: BigBags, primaryColor: Color) {
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
            // Número (alineado a la izquierda)
            Text(
                text = bigBag.bbNumber,
                fontWeight = FontWeight.Bold,
                fontSize = 16.sp,
                color = primaryColor,
                modifier = Modifier.weight(0.2f),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )

            // Peso (alineado a la derecha)
            Box(modifier = Modifier.weight(0.4f), contentAlignment = Alignment.CenterEnd) {
                Text(
                    text = "${bigBag.bbWeight} Kg",
                    fontSize = 14.sp,
                    color = Color.DarkGray,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }

            // Estado con ícono (alineado a la derecha)
            Box(
                modifier = Modifier.weight(0.4f),
                contentAlignment = Alignment.CenterEnd
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    val (statusText, statusColor, statusIcon) = when (bigBag.bbStatus.lowercase()) {
                        "s" -> Triple("En stock", Color(0xFF388E3C), Icons.Default.CheckCircle)
                        "o" -> Triple("Salida", Color(0xFFD32F2F), Icons.Default.Warning)
                        else -> Triple(bigBag.bbStatus, Color.Gray, Icons.Default.Warning)
                    }

                    Icon(
                        imageVector = statusIcon,
                        contentDescription = statusText,
                        tint = statusColor,
                        modifier = Modifier.size(18.dp)
                    )
                    Text(
                        text = statusText,
                        fontSize = 14.sp,
                        color = statusColor,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }
        }
    }
}
