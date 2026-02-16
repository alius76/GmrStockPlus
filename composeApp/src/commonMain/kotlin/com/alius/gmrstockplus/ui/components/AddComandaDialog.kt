package com.alius.gmrstockplus.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.alius.gmrstockplus.domain.model.Cliente
import com.alius.gmrstockplus.domain.model.Material
import com.alius.gmrstockplus.ui.theme.PrimaryColor

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddComandaDialog(
    selectedCliente: Cliente?,
    selectedMaterialsList: List<Material>,
    totalWeight: String,
    remark: String,
    errorCliente: Boolean,
    errorDescripcion: Boolean,
    errorPeso: Boolean,
    onDismiss: () -> Unit,
    onSelectCliente: () -> Unit,
    onAddMaterial: () -> Unit,
    onRemoveMaterial: (Material) -> Unit,
    onWeightChange: (String) -> Unit,
    onRemarkChange: (String) -> Unit,
    onSave: () -> Unit
) {
    Dialog(onDismissRequest = onDismiss) {
        Card(
            shape = RoundedCornerShape(16.dp),
            modifier = Modifier
                .fillMaxWidth(0.95f)
                .padding(vertical = 16.dp)
        ) {
            Column(
                modifier = Modifier
                    .padding(horizontal = 16.dp, vertical = 12.dp)
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(12.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "Agregar comanda",
                    style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                    modifier = Modifier.fillMaxWidth(),
                    textAlign = TextAlign.Center
                )

                // Selector de Cliente
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp)
                        .border(
                            width = 1.dp,
                            color = if (selectedCliente != null) PrimaryColor else Color.DarkGray.copy(alpha = 0.8f),
                            shape = RoundedCornerShape(8.dp)
                        )
                        .clickable { onSelectCliente() }
                        .padding(horizontal = 16.dp),
                    contentAlignment = Alignment.CenterStart
                ) {
                    Text(
                        text = selectedCliente?.cliNombre ?: "Seleccione cliente",
                        color = if (selectedCliente != null) PrimaryColor else Color.DarkGray.copy(alpha = 0.8f)
                    )
                }
                if (errorCliente) Text("Seleccione un cliente", color = Color.Red, fontSize = 12.sp)

                // LISTA DE MATERIALES ELEGIDOS
                Column(modifier = Modifier.fillMaxWidth()) {
                    selectedMaterialsList.forEach { mat ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 2.dp)
                                .background(PrimaryColor.copy(alpha = 0.05f), RoundedCornerShape(4.dp))
                                .padding(8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(mat.materialNombre, modifier = Modifier.weight(1f), fontSize = 14.sp)
                            Icon(
                                imageVector = Icons.Default.Close,
                                contentDescription = "Eliminar",
                                modifier = Modifier
                                    .size(18.dp)
                                    .clickable { onRemoveMaterial(mat) },
                                tint = Color.Red
                            )
                        }
                    }
                    // Botón para añadir a la lista
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp)
                            .border(1.dp, Color.DarkGray.copy(alpha = 0.8f), RoundedCornerShape(8.dp))
                            .clickable { onAddMaterial() }
                            .padding(horizontal = 16.dp),
                        contentAlignment = Alignment.CenterStart
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Add, null, tint = PrimaryColor, modifier = Modifier.size(20.dp))
                            Text(" Añadir Material", color = Color.DarkGray.copy(alpha = 0.8f))
                        }
                    }
                }
                if (errorDescripcion) Text("Añada al menos un material", color = Color.Red, fontSize = 12.sp)

                // Campo Peso
                OutlinedTextField(
                    value = totalWeight,
                    onValueChange = onWeightChange,
                    label = { Text("Peso total (Kg)") },
                    isError = errorPeso,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(8.dp),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    colors = TextFieldDefaults.outlinedTextFieldColors(
                        focusedBorderColor = PrimaryColor,
                        focusedLabelColor = PrimaryColor
                    )
                )

                // Campo Observaciones
                OutlinedTextField(
                    value = remark,
                    onValueChange = onRemarkChange,
                    label = { Text("Observaciones") },
                    modifier = Modifier.fillMaxWidth().heightIn(min = 100.dp),
                    shape = RoundedCornerShape(8.dp),
                    colors = TextFieldDefaults.outlinedTextFieldColors(
                        focusedBorderColor = PrimaryColor,
                        focusedLabelColor = PrimaryColor
                    )
                )

                // Botones de acción
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End // Alinea todo el contenido a la derecha
                ) {
                    TextButton(onClick = onDismiss) {
                        Text("Cancelar", color = PrimaryColor)
                    }

                    Spacer(modifier = Modifier.width(8.dp)) // Espacio entre botones

                    TextButton(onClick = onSave) {
                        Text("Guardar", color = PrimaryColor, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}

