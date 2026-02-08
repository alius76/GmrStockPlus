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
// USAMOS ALIAS PARA EVITAR EL ERROR DE "INT" O "REFERENCE"
import com.alius.gmrstockplus.domain.model.Material as MaterialModel
import com.alius.gmrstockplus.ui.theme.PrimaryColor

@Composable
fun MaterialSelectedDialogContent(
    materials: List<MaterialModel>,
    currentSelectedMaterial: MaterialModel?,
    onDismiss: () -> Unit,
    onConfirm: (MaterialModel) -> Unit
) {
    var tempSelected by remember { mutableStateOf<MaterialModel?>(currentSelectedMaterial) }
    var searchQuery by remember { mutableStateOf("") }
    val focusManager = LocalFocusManager.current

    val filteredMaterials = remember(searchQuery, materials) {
        if (searchQuery.isEmpty()) materials else materials.filter {
            it.materialNombre.contains(searchQuery, ignoreCase = true)
        }
    }

    Dialog(onDismissRequest = { focusManager.clearFocus(); onDismiss() }) {
        Card(
            shape = RoundedCornerShape(16.dp),
            modifier = Modifier.fillMaxWidth(0.95f).heightIn(max = 550.dp),
            elevation = CardDefaults.cardElevation(8.dp)
        ) {
            Column(modifier = Modifier.background(MaterialTheme.colorScheme.surface).padding(16.dp).fillMaxHeight()) {
                Text(
                    "Seleccione un material",
                    fontWeight = FontWeight.Bold, fontSize = 20.sp, color = PrimaryColor,
                    modifier = Modifier.fillMaxWidth(), textAlign = TextAlign.Center
                )

                Spacer(Modifier.height(16.dp))

                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    modifier = Modifier.fillMaxWidth(),
                    placeholder = { Text("Buscar material...", fontSize = 14.sp) },
                    leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, tint = PrimaryColor) },
                    singleLine = true,
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = PrimaryColor,
                        cursorColor = PrimaryColor,
                        focusedLeadingIconColor = PrimaryColor
                    )
                )

                Spacer(Modifier.height(12.dp))

                Box(modifier = Modifier.weight(1f).fillMaxWidth()) {
                    LazyColumn {
                        items(filteredMaterials) { materialItem ->
                            Row(
                                modifier = Modifier.fillMaxWidth().clickable { tempSelected = materialItem }
                                    .padding(vertical = 8.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                RadioButton(
                                    selected = tempSelected == materialItem,
                                    onClick = { tempSelected = materialItem },
                                    colors = RadioButtonDefaults.colors(selectedColor = PrimaryColor)
                                )
                                Text(
                                    text = materialItem.materialNombre,
                                    color = if (tempSelected == materialItem) PrimaryColor else Color.Unspecified,
                                    fontWeight = if (tempSelected == materialItem) FontWeight.Bold else FontWeight.Normal
                                )
                            }
                        }
                    }
                }

                Row(Modifier.fillMaxWidth(), Arrangement.End) {
                    TextButton(onClick = onDismiss) { Text("Cancelar", color = PrimaryColor) }
                    TextButton(
                        onClick = { tempSelected?.let { onConfirm(it) } },
                        enabled = tempSelected != null
                    ) {
                        Text("Aceptar", color = PrimaryColor)
                    }
                }
            }
        }
    }
}