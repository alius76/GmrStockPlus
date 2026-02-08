package com.alius.gmrstockplus.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.alius.gmrstockplus.ui.theme.PrimaryColor
import kotlinx.datetime.LocalDate

@Composable
fun DateRangeFilter(
    startDate: LocalDate,
    endDate: LocalDate,
    onSelectStartDate: () -> Unit,
    onSelectEndDate: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .height(60.dp)
            .background(Color.White, RoundedCornerShape(12.dp))
            .border(1.dp, Color.LightGray.copy(alpha = 0.5f), RoundedCornerShape(12.dp))
            .padding(horizontal = 4.dp, vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Fecha Inicio
        Box(
            modifier = Modifier
                .weight(1f)
                .fillMaxHeight()
                .clip(RoundedCornerShape(8.dp))
                .clickable { onSelectStartDate() },
            contentAlignment = Alignment.Center
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text("DESDE", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = Color.Gray)
                // Formato DD/MM/AAAA con padStart
                val day = startDate.dayOfMonth.toString().padStart(2, '0')
                val month = startDate.monthNumber.toString().padStart(2, '0')
                Text(
                    text = "$day/$month/${startDate.year}",
                    fontSize = 15.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = PrimaryColor
                )
            }
        }

        // Separador visual (Flecha)
        Icon(
            imageVector = androidx.compose.material.icons.Icons.Default.ArrowForward,
            contentDescription = null,
            tint = Color.LightGray,
            modifier = Modifier.size(16.dp)
        )

        // Fecha Fin
        Box(
            modifier = Modifier
                .weight(1f)
                .fillMaxHeight()
                .clip(RoundedCornerShape(8.dp))
                .clickable { onSelectEndDate() },
            contentAlignment = Alignment.Center
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text("HASTA", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = Color.Gray)
                // Formato DD/MM/AAAA con padStart
                val day = endDate.dayOfMonth.toString().padStart(2, '0')
                val month = endDate.monthNumber.toString().padStart(2, '0')
                Text(
                    text = "$day/$month/${endDate.year}",
                    fontSize = 15.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = PrimaryColor
                )
            }
        }
    }
}