package com.alius.gmrstockplus.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Scale
import androidx.compose.material.icons.outlined.ShoppingBag
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.alius.gmrstockplus.domain.model.ClientGroupSell
import com.alius.gmrstockplus.core.utils.formatWeight
import com.alius.gmrstockplus.ui.theme.PrimaryColor
import com.alius.gmrstockplus.ui.theme.SecondaryColor
import com.alius.gmrstockplus.ui.theme.TextSecondary

@Composable
fun ClientGroupSellCard(
    group: ClientGroupSell,
    onClick: (ClientGroupSell) -> Unit
) {
    // ⬇️ Ya no necesitamos try-catch: el dato es un Int!
    val totalKilosNumber = group.totalKilosVendidos

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp, horizontal = 12.dp)
            .clickable { onClick(group) },
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 6.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            // --- 1. Título principal: Cliente ---
            Text(
                text = group.cliente.cliNombre,
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.ExtraBold,
                color = PrimaryColor,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )

            // --- Separador visual ---
            Spacer(modifier = Modifier.height(8.dp))
            Divider(color = TextSecondary.copy(alpha = 0.2f), thickness = 1.dp)
            Spacer(modifier = Modifier.height(12.dp))

            // --- 2. Fila de Métricas (Kilos, BigBags y Total Ventas en Badge) ---
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Métrica 1: Kilos Vendidos
                MetricItem(
                    icon = Icons.Default.Scale,
                    label = "Kilos Vendidos",
                    // ⬇️ Usamos el Int directamente
                    value = "${formatWeight(totalKilosNumber)} Kg",
                    iconColor = SecondaryColor
                )

                // Métrica 2: BigBags Vendidos ⬅️ USANDO EL DATO REAL
                MetricItem(
                    icon = Icons.Outlined.ShoppingBag,
                    label = "BigBags",
                    value = group.totalBigBags.toString(), // ⬅️ ¡Dato real!
                    iconColor = Color(0xFF00BFA5)
                )

                // Métrica 3: Ventas Totales (Badge grande)
                Box(
                    modifier = Modifier
                        .background(PrimaryColor.copy(alpha = 0.15f), RoundedCornerShape(10.dp))
                        .padding(horizontal = 10.dp, vertical = 8.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = group.totalVentasMes.toString(),
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            color = PrimaryColor,
                            fontSize = 24.sp
                        )
                        Text(
                            text = "Ventas",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Light,
                            color = TextSecondary
                        )
                    }
                }
            }
        }
    }
}

