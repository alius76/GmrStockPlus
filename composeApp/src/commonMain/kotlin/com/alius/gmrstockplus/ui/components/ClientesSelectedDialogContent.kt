package com.alius.gmrstockplus.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.alius.gmrstockplus.domain.model.Cliente
import com.alius.gmrstockplus.ui.theme.PrimaryColor

@Composable
fun ClientesSelectedDialogContent(
    clients: List<Cliente>,
    currentSelectedClient: Cliente?,
    showAllOption: Boolean = false, // Parámetro opcional
    onDismiss: () -> Unit,
    onConfirm: (Cliente?) -> Unit // Cambiado a Cliente? (null significa TODOS)
) {
    var tempSelected by remember { mutableStateOf(currentSelectedClient) }
    var searchQuery by remember { mutableStateOf("") }
    val focusManager = LocalFocusManager.current

    // Objeto ficticio para representar la opción global
    val todoItem = remember { Cliente(cliNombre = "TODOS LOS CLIENTES") }

    val filteredClients = remember(searchQuery, clients) {
        val base = if (searchQuery.isEmpty()) clients else clients.filter {
            it.cliNombre.contains(searchQuery, ignoreCase = true)
        }
        // Solo inyectamos "TODOS" si no hay búsqueda activa y la opción está habilitada
        if (showAllOption && searchQuery.isEmpty()) listOf(todoItem) + base else base
    }

    Dialog(onDismissRequest = { focusManager.clearFocus(); onDismiss() }) {
        Card(
            shape = RoundedCornerShape(16.dp),
            modifier = Modifier.fillMaxWidth(0.95f).heightIn(max = 550.dp),
            elevation = CardDefaults.cardElevation(8.dp)
        ) {
            Column(modifier = Modifier.background(MaterialTheme.colorScheme.surface).padding(16.dp).fillMaxHeight()) {
                Text(
                    "Seleccione un cliente",
                    fontWeight = FontWeight.Bold, fontSize = 20.sp, color = PrimaryColor,
                    modifier = Modifier.fillMaxWidth(), textAlign = TextAlign.Center
                )

                Spacer(Modifier.height(16.dp))

                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    modifier = Modifier.fillMaxWidth(),
                    placeholder = { Text("Buscar cliente...", fontSize = 14.sp) },
                    leadingIcon = { Icon(Icons.Default.Search, null, tint = PrimaryColor) },
                    singleLine = true,
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = PrimaryColor,    // Color del borde al seleccionar
                        focusedLabelColor = PrimaryColor,     // Color de la etiqueta al seleccionar
                        cursorColor = PrimaryColor,           // Color de la barrita del cursor
                        focusedLeadingIconColor = PrimaryColor // Color del icono cuando está enfocado
                    )
                )

                Spacer(Modifier.height(12.dp))

                Box(modifier = Modifier.weight(1f).fillMaxWidth()) {
                    LazyColumn {
                        items(filteredClients) { clienteItem ->
                            Row(
                                modifier = Modifier.fillMaxWidth().clickable { tempSelected = clienteItem }
                                    .padding(vertical = 8.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                RadioButton(
                                    selected = tempSelected == clienteItem,
                                    onClick = { tempSelected = clienteItem },
                                    colors = RadioButtonDefaults.colors(selectedColor = PrimaryColor)
                                )
                                Text(
                                    text = clienteItem.cliNombre,
                                    color = if (tempSelected == clienteItem) PrimaryColor else Color.Unspecified,
                                    fontWeight = if (tempSelected == clienteItem) FontWeight.Bold else FontWeight.Normal
                                )
                            }
                        }
                    }
                }

                Row(Modifier.fillMaxWidth(), Arrangement.End) {
                    TextButton(onClick = onDismiss) { Text("Cancelar", color = PrimaryColor) }
                    TextButton(
                        onClick = {
                            // Si seleccionó el item especial, devolvemos null
                            if (tempSelected == todoItem) onConfirm(null)
                            else onConfirm(tempSelected)
                        },
                        enabled = tempSelected != null
                    ) {
                        Text("Aceptar", color = PrimaryColor)
                    }
                }
            }
        }
    }
}