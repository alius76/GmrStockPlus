package com.alius.gmrstockplus.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.alius.gmrstockplus.ui.theme.PrimaryColor

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditRemarkDialog(
    remark: String,
    onRemarkChange: (String) -> Unit,
    onDismiss: () -> Unit,
    onSave: () -> Unit
) {
    Dialog(onDismissRequest = onDismiss) {
        Card(
            shape = RoundedCornerShape(16.dp),
            modifier = Modifier.fillMaxWidth(0.9f).padding(vertical = 16.dp)
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text("Editar observaciones", fontSize = 20.sp, modifier = Modifier.fillMaxWidth(), textAlign = TextAlign.Center)

                OutlinedTextField(
                    value = remark,
                    onValueChange = onRemarkChange,
                    label = { Text("Observaciones") },
                    modifier = Modifier.fillMaxWidth().heightIn(min = 120.dp, max = 150.dp),
                    shape = RoundedCornerShape(8.dp),
                    colors = TextFieldDefaults.outlinedTextFieldColors(
                        focusedBorderColor = PrimaryColor,
                        focusedLabelColor = PrimaryColor
                    )
                )

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                    TextButton(onClick = onDismiss) { Text("Cancelar", color = PrimaryColor) }
                    Spacer(modifier = Modifier.width(8.dp))
                    TextButton(onClick = onSave) { Text("Guardar", color = PrimaryColor, fontWeight = FontWeight.Bold) }
                }
            }
        }
    }
}

