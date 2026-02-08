package com.alius.gmrstockplus.ui.components


import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.alius.gmrstockplus.ui.theme.PrimaryColor

@Composable
fun MySegmentedButton(
    options: List<String>,
    selectedIndex: Int,
    onSelect: (Int) -> Unit
) {
    Row(
        modifier = Modifier
            .background(Color(0xFFE0E0E0), RoundedCornerShape(12.dp))
            .padding(2.dp)
    ) {
        options.forEachIndexed { index, option ->
            val isSelected = index == selectedIndex

            Box(
                modifier = Modifier
                    .width(70.dp)  // ANCHO FIJO
                    .height(36.dp) // ALTO FIJO
                    .background(
                        color = if (isSelected) PrimaryColor else Color.Transparent,
                        shape = RoundedCornerShape(12.dp)
                    )
                    .clickable { onSelect(index) }
                    .padding(horizontal = 12.dp)
                    .then(
                        if (isSelected) Modifier.shadow(
                            elevation = 4.dp,
                            shape = RoundedCornerShape(12.dp)
                        ) else Modifier
                    ),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = option,
                    fontSize = 14.sp,
                    color = if (isSelected) Color.White else Color.Black,
                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                )
            }

            if (index != options.lastIndex) {
                Spacer(modifier = Modifier.width(4.dp))
            }
        }
    }
}