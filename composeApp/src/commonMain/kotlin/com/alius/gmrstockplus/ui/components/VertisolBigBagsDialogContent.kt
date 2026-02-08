package com.alius.gmrstockplus.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
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
import com.alius.gmrstockplus.core.utils.formatInstant
import com.alius.gmrstockplus.domain.model.VertisolBigBag
import com.alius.gmrstockplus.ui.theme.PrimaryColor

@Composable
fun VertisolBigBagsDialogContent(vertisolBigBags: List<VertisolBigBag>) {
    val primaryColor = PrimaryColor

    Column(modifier = Modifier.fillMaxWidth()) {
        if (vertisolBigBags.isEmpty()) {
            Text(
                text = "No hay BigBags asociados a este lote.",
                modifier = Modifier.padding(16.dp),
                fontWeight = FontWeight.Medium,
                color = Color.Gray,
                fontSize = 16.sp
            )
        } else {
            // --- Encabezado ---
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
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Número",
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp,
                        color = primaryColor,
                        modifier = Modifier.weight(0.35f)
                    )
                    Text(
                        text = "Peso (Kg)",
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp,
                        color = primaryColor,
                        modifier = Modifier.weight(0.35f),
                        textAlign = TextAlign.Center
                    )
                    Text(
                        text = "Fecha",
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp,
                        color = primaryColor,
                        modifier = Modifier.weight(0.3f),
                        textAlign = TextAlign.End
                    )
                }
            }

            Spacer(modifier = Modifier.height(4.dp))

            // --- Lista de BigBags ---
            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(max = 300.dp),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                items(vertisolBigBags) { bigBag ->
                    VertisolBigBagListItem(bigBag, primaryColor)
                }
            }
        }
    }
}

@Composable
fun VertisolBigBagListItem(bigBag: VertisolBigBag, primaryColor: Color) {
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
                text = bigBag.bbNumber,
                fontWeight = FontWeight.Bold,
                fontSize = 16.sp,
                color = primaryColor,
                modifier = Modifier.weight(0.35f),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )

            // Peso (centrado)
            Box(
                modifier = Modifier.weight(0.35f),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "${bigBag.bbWeight} Kg",
                    fontSize = 14.sp,
                    color = Color.DarkGray,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }

            // Fecha (alineada a la derecha)
            Box(
                modifier = Modifier.weight(0.3f),
                contentAlignment = Alignment.CenterEnd
            ) {
                val fechaTexto = bigBag.bbTrasvaseDate?.let { formatInstant(it) } ?: "Desconocido"
                Text(
                    text = fechaTexto,
                    fontSize = 14.sp,
                    color = if (bigBag.bbTrasvaseDate != null) Color(0xFF388E3C) else Color.Gray,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
    }
}
