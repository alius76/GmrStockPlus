package com.alius.gmrstockplus.ui.components

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.unit.dp
import com.alius.gmrstockplus.ui.theme.PrimaryColor

@Composable
fun ConfirmReassignDateDialog(
    clienteNombre: String,
    oldDate: String,
    newDate: String,
    onDismiss: () -> Unit,
    onConfirm: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Confirmar nueva fecha") },
        text = { Text("¿Desea mover la comanda de $clienteNombre del $oldDate al $newDate?") },
        confirmButton = {
            TextButton(onClick = onConfirm) { Text("Confirmar", color = PrimaryColor) }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancelar", color = PrimaryColor) }
        },
        shape = RoundedCornerShape(16.dp)
    )
}

