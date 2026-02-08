package com.alius.gmrstockplus.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.grid.rememberLazyGridState
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import kotlinx.datetime.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun UniversalDatePickerDialog(
    initialDate: LocalDate,
    onDateSelected: (LocalDate) -> Unit,
    onDismiss: () -> Unit,
    primaryColor: Color = Color(0xFF029083)
) {
    var currentMonth by remember { mutableStateOf(initialDate.monthNumber) }
    var currentYear by remember { mutableStateOf(initialDate.year) }
    var selectedDate by remember { mutableStateOf(initialDate) }

    val monthNames = listOf(
        "Enero", "Febrero", "Marzo", "Abril", "Mayo", "Junio",
        "Julio", "Agosto", "Septiembre", "Octubre", "Noviembre", "Diciembre"
    )

    fun daysInMonth(year: Int, month: Int): Int {
        val thisMonth = LocalDate(year, month, 1)
        val nextMonth = if (month == 12) LocalDate(year + 1, 1, 1)
        else LocalDate(year, month + 1, 1)
        return (nextMonth.toEpochDays() - thisMonth.toEpochDays()).toInt()
    }

    val firstDayOfMonth = remember(currentMonth, currentYear) {
        LocalDate(currentYear, currentMonth, 1)
    }

    val dayOfWeekOffset = remember(firstDayOfMonth) {
        // Kotlinx.datetime usa Monday=1 ... Sunday=7
        firstDayOfMonth.dayOfWeek.isoDayNumber - 1 // 0=Lunes ... 6=Domingo
    }

    val dayList = remember(currentMonth, currentYear) {
        val days = daysInMonth(currentYear, currentMonth)
        List(days) { index -> LocalDate(currentYear, currentMonth, index + 1) }
    }

    val gridState = rememberLazyGridState()

    AlertDialog(
        onDismissRequest = onDismiss,
        confirmButton = {
            TextButton(
                onClick = {
                    onDateSelected(selectedDate)
                    onDismiss()
                },
                colors = ButtonDefaults.textButtonColors(contentColor = primaryColor)
            ) { Text("Aceptar") }
        },
        dismissButton = {
            TextButton(
                onClick = onDismiss,
                colors = ButtonDefaults.textButtonColors(contentColor = primaryColor)
            ) { Text("Cancelar") }
        },
        text = {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {

                // Encabezado mes / año
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(onClick = {
                        if (currentMonth == 1) {
                            currentMonth = 12
                            currentYear -= 1
                        } else currentMonth--
                    }) { Text("<", color = primaryColor, fontWeight = FontWeight.Bold) }

                    Text(
                        "${monthNames[currentMonth - 1]} $currentYear",
                        style = MaterialTheme.typography.titleMedium,
                        color = primaryColor
                    )

                    IconButton(onClick = {
                        if (currentMonth == 12) {
                            currentMonth = 1
                            currentYear += 1
                        } else currentMonth++
                    }) { Text(">", color = primaryColor, fontWeight = FontWeight.Bold) }
                }

                Spacer(Modifier.height(8.dp))

                // Días de la semana
                val daysOfWeek = listOf("L", "M", "X", "J", "V", "S", "D")
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 4.dp),
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    daysOfWeek.forEach { day ->
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .aspectRatio(1f),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = day,
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = FontWeight.Bold,
                                color = primaryColor
                            )
                        }
                    }
                }

                Spacer(Modifier.height(4.dp))

                // Cuadrícula de días
                LazyVerticalGrid(
                    columns = GridCells.Fixed(7),
                    state = gridState,
                    // ✅ CORRECCIÓN: Eliminar la altura fija (280.dp) y permitir que se ajuste al contenido.
                    // Usamos .fillMaxWidth().wrapContentHeight() para asegurar que el AlertDialog no lo recorte
                    modifier = Modifier
                        .fillMaxWidth()
                        .wrapContentHeight(),
                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                    verticalArrangement = Arrangement.spacedBy(4.dp),
                    contentPadding = PaddingValues(4.dp)
                ) {
                    // Espacios vacíos antes del primer día
                    if (dayOfWeekOffset > 0) {
                        items(dayOfWeekOffset) {
                            Box(
                                modifier = Modifier
                                    .aspectRatio(1f)
                            )
                        }
                    }

                    // Días reales
                    items(dayList) { date ->
                        val isSelected = date == selectedDate
                        val isToday =
                            date == Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault()).date

                        val dayBackground = when {
                            isSelected -> primaryColor
                            isToday -> primaryColor.copy(alpha = 0.15f)
                            else -> MaterialTheme.colorScheme.surfaceVariant
                        }

                        val dayTextColor = when {
                            isSelected -> Color.White
                            isToday -> primaryColor
                            else -> MaterialTheme.colorScheme.onSurface
                        }

                        Box(
                            modifier = Modifier
                                .aspectRatio(1f)
                                .background(dayBackground, shape = MaterialTheme.shapes.small)
                                .clickable { selectedDate = date },
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = date.dayOfMonth.toString(),
                                color = dayTextColor,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                            )
                        }
                    }
                }
            }
        }
    )
}