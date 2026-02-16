package com.alius.gmrstockplus.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.alius.gmrstockplus.ui.theme.PrimaryColor

@Composable
fun ConfirmDeleteComandaDialog(
    clienteNombre: String,
    isProcessing: Boolean,
    onDismiss: () -> Unit,
    onConfirm: () -> Unit
) {
    AlertDialog(
        onDismissRequest = { if (!isProcessing) onDismiss() },
        title = {
            Text(
                text = "Confirmar anulación",
                fontWeight = FontWeight.Bold,
                color = if (isProcessing) Color.Gray else MaterialTheme.colorScheme.onSurface
            )
        },
        text = {
            if (isProcessing) {
                Column(
                    modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    CircularProgressIndicator(color = PrimaryColor, modifier = Modifier.size(40.dp))
                    Spacer(modifier = Modifier.height(16.dp))
                    Text("Procesando anulación...", color = PrimaryColor)
                }
            } else {
                Text("¿Está seguro de que desea anular la comanda para $clienteNombre?")
            }
        },
        confirmButton = {
            if (!isProcessing) {
                TextButton(onClick = onConfirm) {
                    Text("Anular", color = MaterialTheme.colorScheme.error, fontWeight = FontWeight.Bold)
                }
            }
        },
        dismissButton = {
            if (!isProcessing) {
                TextButton(onClick = onDismiss) { Text("Cancelar", color = PrimaryColor) }
            }
        },
        shape = RoundedCornerShape(16.dp),
        containerColor = Color.White
    )
}

