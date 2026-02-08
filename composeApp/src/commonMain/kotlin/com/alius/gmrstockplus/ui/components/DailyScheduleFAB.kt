package com.alius.gmrstockplus.ui.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.LocalShipping
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.FloatingActionButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.alius.gmrstockplus.ui.theme.PrimaryColor

@Composable
fun DailyScheduleFAB(onClick: () -> Unit) {
    // 🆕 Cambiamos a FloatingActionButton para un tamaño compacto
    FloatingActionButton(
        onClick = onClick,
        modifier = Modifier.padding(16.dp),
        // Usamos el color principal de tu aplicación
        containerColor = PrimaryColor,
        // Añadimos una elevación moderna
        elevation = FloatingActionButtonDefaults.elevation(
            defaultElevation = 8.dp,
            pressedElevation = 12.dp
        ),
        // Forma un poco más cuadrada y moderna
        shape = RoundedCornerShape(12.dp)
    ) {
        // 🆕 Usamos Column para colocar el Icono arriba y el Texto abajo, centrado
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.padding(vertical = 4.dp) // Añadir un poco de espacio vertical interno
        ) {
            Icon(
                Icons.Default.LocalShipping,
                contentDescription = "Salidas",
                tint = Color.White,
                modifier = Modifier.size(24.dp) // Ajustamos el tamaño del icono
            )
            // Texto descriptivo abajo del icono
            Text(
                text = "Salidas",
                fontWeight = FontWeight.Bold,
                fontSize = 10.sp, // Fuente pequeña para que encaje bien
                color = Color.White
            )
        }
    }
}