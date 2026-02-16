package com.alius.gmrstockplus.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.alius.gmrstockplus.domain.model.Comanda
import com.alius.gmrstockplus.ui.theme.PrimaryColor

@Composable
fun SelectMaterialAssignmentDialog(
    comanda: Comanda,
    onDismiss: () -> Unit,
    onMaterialSelected: (String) -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = "Asignar lote",
                style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                modifier = Modifier.fillMaxWidth(),
                textAlign = TextAlign.Center
            )
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(
                    text = "Seleccione el material a gestionar:",
                    fontSize = 14.sp,
                    modifier = Modifier.fillMaxWidth(),
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(4.dp))

                // Obtenemos los nombres de materiales únicos de las asignaciones
                comanda.listaAsignaciones.map { it.materialNombre }.distinct().forEach { mat ->
                    Button(
                        onClick = { onMaterialSelected(mat) },
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = PrimaryColor.copy(alpha = 0.1f),
                            contentColor = PrimaryColor
                        )
                    ) {
                        Text(mat)
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text(
                    text = "Cerrar",
                    color = PrimaryColor,
                    fontWeight = FontWeight.Bold
                )
            }
        },
        shape = RoundedCornerShape(16.dp),
        containerColor = Color.White
    )
}

