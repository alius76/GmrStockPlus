package com.alius.gmrstockplus.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AssignmentReturn
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.alius.gmrstockplus.domain.model.BigBags
import com.alius.gmrstockplus.ui.theme.PrimaryColor


@Composable
fun BigBagSeleccionableItem(
    bigBag: BigBags,
    isSelected: Boolean,
    onToggleSelect: () -> Unit
) {
    // Definición de contornos mejorados
    val selectedBorder = BorderStroke(width = 2.5.dp, color = PrimaryColor)
    val unselectedBorder = BorderStroke(width = 1.dp, color = Color.LightGray.copy(alpha = 0.5f))

    Card(
        onClick = onToggleSelect,
        modifier = Modifier.fillMaxWidth(),
        // MEJORA 1: Mayor redondez consistente
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            // MEJORA 2: Fondo de selección más sutil
            containerColor = if (isSelected) PrimaryColor.copy(alpha = 0.15f) else Color.White
        ),
        // MEJORA 3: Contorno más visible al seleccionar
        border = if (isSelected) selectedBorder else unselectedBorder,
        // Pequeña elevación
        elevation = CardDefaults.cardElevation(
            // Si está seleccionado, ponemos elevación 0 para que la transparencia sea real.
            // Si no, dejamos los 2.dp para que se vea la sombra normal.
            defaultElevation = if (isSelected) 0.dp else 2.dp,
            pressedElevation = 0.dp
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {

            // 1. Checkbox
            Checkbox(
                checked = isSelected,
                onCheckedChange = { onToggleSelect() },
                colors = CheckboxDefaults.colors(
                    checkedColor = PrimaryColor,
                    uncheckedColor = Color.Gray
                )
            )

            Spacer(modifier = Modifier.width(16.dp))


             Icon(
                 Icons.Default.AssignmentReturn,
                contentDescription = "Big Bag",
                tint = PrimaryColor.copy(alpha = 0.7f),
                modifier = Modifier.width(28.dp).height(28.dp)
            )
            Spacer(modifier = Modifier.width(12.dp))

            // 3. Contenido (Número y Peso)
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.Center
            ) {
                // MEJORA 4: Número de BB más destacado
                Text(
                    text = "Big Bag: ${bigBag.bbNumber}",
                    fontWeight = FontWeight.ExtraBold,
                    color = PrimaryColor, // Color primario para el título
                    fontSize = 15.sp
                )
                Spacer(modifier = Modifier.height(2.dp))
                // MEJORA 5: Peso en color gris oscuro para jerarquía
                Text(
                    text = "${bigBag.bbWeight} Kg",
                    color = Color.DarkGray,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Medium
                )
            }
        }
    }
}