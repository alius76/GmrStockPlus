package com.alius.gmrstockplus.ui.components

import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.alius.gmrstockplus.domain.model.Comanda
import kotlinx.datetime.*
import com.alius.gmrstockplus.ui.theme.PrimaryColor
import com.alius.gmrstockplus.ui.theme.ReservedColor
import com.alius.gmrstockplus.ui.theme.WarningColor

@Composable
fun InlineCalendarSelector(
    selectedDate: LocalDate,
    allComandas: List<Comanda>,
    onDateSelected: (LocalDate) -> Unit,
    primaryColor: Color = PrimaryColor
) {
    var isExpanded by remember { mutableStateOf(false) }
    var viewMonth by remember { mutableStateOf(selectedDate.monthNumber) }
    var viewYear by remember { mutableStateOf(selectedDate.year) }

    LaunchedEffect(selectedDate) {
        viewMonth = selectedDate.monthNumber
        viewYear = selectedDate.year
    }

    val monthNames = listOf(
        "Enero", "Febrero", "Marzo", "Abril", "Mayo", "Junio",
        "Julio", "Agosto", "Septiembre", "Octubre", "Noviembre", "Diciembre"
    )

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .animateContentSize(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
        border = BorderStroke(1.dp, Color.LightGray.copy(alpha = 0.4f))
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(16.dp))
        ) {
            // --- HEADER ---
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(primaryColor)
                    .padding(vertical = 4.dp, horizontal = 8.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        IconButton(onClick = {
                            if (isExpanded) {
                                val prevMonth = if (viewMonth == 1) LocalDate(viewYear - 1, 12, 1)
                                else LocalDate(viewYear, viewMonth - 1, 1)
                                onDateSelected(prevMonth)
                            } else {
                                onDateSelected(selectedDate.minus(7, DateTimeUnit.DAY))
                            }
                        }) {
                            Text("<", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 20.sp)
                        }

                        Text(
                            text = "${monthNames[viewMonth - 1]} $viewYear",
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.Bold,
                                fontSize = 18.sp
                            ),
                            color = Color.White,
                            modifier = Modifier
                                .clickable { isExpanded = !isExpanded }
                                .padding(horizontal = 8.dp)
                        )

                        IconButton(onClick = {
                            if (isExpanded) {
                                val nextMonth = if (viewMonth == 12) LocalDate(viewYear + 1, 1, 1)
                                else LocalDate(viewYear, viewMonth + 1, 1)
                                onDateSelected(nextMonth)
                            } else {
                                onDateSelected(selectedDate.plus(7, DateTimeUnit.DAY))
                            }
                        }) {
                            Text(">", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 20.sp)
                        }
                    }

                    IconButton(onClick = { isExpanded = !isExpanded }) {
                        Icon(
                            imageVector = if (isExpanded) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                            contentDescription = null,
                            tint = Color.White
                        )
                    }
                }
            }

            // --- CUERPO (Días) ---
            Column(modifier = Modifier.padding(12.dp)) {
                Row(modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp)) {
                    val daysOfWeek = listOf("L", "M", "X", "J", "V", "S", "D")
                    daysOfWeek.forEach { day ->
                        Box(modifier = Modifier.weight(1f), contentAlignment = Alignment.Center) {
                            Text(
                                text = day,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = primaryColor.copy(alpha = 0.6f)
                            )
                        }
                    }
                }

                if (isExpanded) {
                    MonthViewGrid(viewMonth, viewYear, selectedDate, allComandas, primaryColor) {
                        onDateSelected(it)
                        isExpanded = false
                    }
                } else {
                    WeekViewRow(selectedDate, allComandas, primaryColor, onDateSelected)
                }
            }
        }
    }
}

@Composable
fun WeekViewRow(
    selectedDate: LocalDate,
    allComandas: List<Comanda>,
    primaryColor: Color,
    onDateSelected: (LocalDate) -> Unit
) {
    val dayOfWeek = selectedDate.dayOfWeek.isoDayNumber
    val startOfWeek = selectedDate.minus(dayOfWeek - 1, DateTimeUnit.DAY)

    Row(modifier = Modifier.fillMaxWidth()) {
        (0..6).forEach { i ->
            val date = startOfWeek.plus(i, DateTimeUnit.DAY)
            DayItem(date, selectedDate, allComandas, primaryColor, Modifier.weight(1f), onDateSelected)
        }
    }
}

@Composable
fun MonthViewGrid(
    month: Int,
    year: Int,
    selectedDate: LocalDate,
    allComandas: List<Comanda>,
    primaryColor: Color,
    onDateSelected: (LocalDate) -> Unit
) {
    val firstDayOfMonth = LocalDate(year, month, 1)
    val dayOfWeekOffset = firstDayOfMonth.dayOfWeek.isoDayNumber - 1
    val daysInMonth = if (month == 12) LocalDate(year + 1, 1, 1).toEpochDays() - firstDayOfMonth.toEpochDays()
    else LocalDate(year, month + 1, 1).toEpochDays() - firstDayOfMonth.toEpochDays()

    Column {
        for (row in 0..5) {
            Row(modifier = Modifier.fillMaxWidth()) {
                for (col in 0..6) {
                    val dayIndex = row * 7 + col - dayOfWeekOffset
                    if (dayIndex in 0 until daysInMonth) {
                        val date = LocalDate(year, month, dayIndex.toInt() + 1)
                        DayItem(date, selectedDate, allComandas, primaryColor, Modifier.weight(1f), onDateSelected)
                    } else {
                        Spacer(modifier = Modifier.weight(1f))
                    }
                }
            }
            if ((row + 1) * 7 - dayOfWeekOffset >= daysInMonth) break
        }
    }
}

@Composable
fun DayItem(
    date: LocalDate,
    selectedDate: LocalDate,
    allComandas: List<Comanda>,
    primaryColor: Color,
    modifier: Modifier,
    onDateSelected: (LocalDate) -> Unit
) {
    val isSelected = date == selectedDate
    val hoy = Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault()).date
    val isToday = date == hoy
    val statusColor = obtenerColorDia(date, allComandas)

    Box(
        modifier = modifier
            .aspectRatio(1f)
            .padding(2.dp)
            .background(
                color = when {
                    statusColor != Color.Transparent -> statusColor.copy(alpha = 0.15f)
                    isToday -> Color.LightGray.copy(alpha = 0.3f)
                    else -> Color.Transparent
                },
                shape = RoundedCornerShape(8.dp)
            )
            .border(
                width = if (isSelected) 2.dp else if (statusColor != Color.Transparent || isToday) 1.dp else 0.dp,
                color = when {
                    isSelected -> PrimaryColor // Borde Verde para selección
                    statusColor != Color.Transparent -> statusColor
                    isToday -> Color.Gray.copy(alpha = 0.5f)
                    else -> Color.Transparent
                },
                shape = RoundedCornerShape(8.dp)
            )
            .clickable { onDateSelected(date) },
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = date.dayOfMonth.toString(),
            color = if (isSelected) Color.Black else Color.DarkGray,
            fontWeight = if (isSelected || isToday) FontWeight.ExtraBold else FontWeight.Bold,
            fontSize = 14.sp
        )
    }
}

private fun obtenerColorDia(
    date: LocalDate,
    allComandas: List<Comanda>
): Color {
    val hoy = Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault()).date

    val comandasDelDia = allComandas.filter {
        it.dateBookedComanda?.toLocalDateTime(TimeZone.currentSystemDefault())?.date == date
    }

    if (comandasDelDia.isEmpty()) return Color.Transparent

    // 1. PRIORIDAD ROJA: Retraso
    if (comandasDelDia.any { !it.fueVendidoComanda && date < hoy }) return ReservedColor

    // 2. COMANDAS ACTIVAS
    val activas = comandasDelDia.filter { !it.fueVendidoComanda }
    if (activas.isNotEmpty()) {
        // PRIORIDAD ÁMBAR: Si alguna activa no tiene lote
        return if (activas.any { it.numberLoteComanda.isBlank() }) {
            WarningColor
        } else {
            // TODO OK: Verde
            PrimaryColor
        }
    }

    return Color.Transparent
}