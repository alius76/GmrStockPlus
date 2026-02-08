package com.alius.gmrstockplus.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.ripple.rememberRipple
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.alius.gmrstockplus.core.utils.formatInstant
import com.alius.gmrstockplus.core.utils.formatWeight
import com.alius.gmrstockplus.domain.model.Comanda
import com.alius.gmrstockplus.ui.theme.PrimaryColor
import com.alius.gmrstockplus.ui.theme.TextSecondary
import kotlinx.datetime.Instant

@Composable
fun ComandaLoteCard(
    comanda: Comanda,
    isSelected: Boolean,
    onClick: (Comanda) -> Unit
) {
    val badgeColor = PrimaryColor
    val cardColor = Color.White
    val rippleColor = PrimaryColor.copy(alpha = 0.2f)

    val topPaddingForBadge = 12.dp

    fun formatComandaNumber(number: String): String {
        return ("#" + number.toIntOrNull()?.toString()?.padStart(6, '0')) ?: "#ERROR"
    }

    val formattedComandaNumber = formatComandaNumber(comanda.numeroDeComanda.toString())
    val selectionBorderColor = if (isSelected) PrimaryColor else Color.LightGray.copy(alpha = 0.3f)

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = topPaddingForBadge)
            .clickable(
                onClick = { onClick(comanda) },
                indication = rememberRipple(bounded = true, color = rippleColor),
                interactionSource = remember { MutableInteractionSource() }
            )
    ) {
        // --- Card principal (Estética de ComandaCard) ---
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
            colors = CardDefaults.cardColors(containerColor = cardColor),
            border = BorderStroke(if (isSelected) 2.dp else 1.dp, selectionBorderColor)
        ) {
            Column(
                modifier = Modifier
                    .padding(12.dp)
                    .fillMaxWidth()
            ) {
                // --- FILA 1: NÚMERO DE COMANDA Y FECHA (CABECERA) ---
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    // 1. Espacio para el badge.
                    Spacer(modifier = Modifier.width(60.dp))

                    // 2. Fecha (Alineada a la derecha)
                    Text(
                        text = formatInstant(comanda.dateBookedComanda ?: Instant.DISTANT_FUTURE),
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = TextSecondary,
                        modifier = Modifier.align(Alignment.CenterVertically)
                    )
                }

                // LÍNEA DE DIVISIÓN
                Spacer(modifier = Modifier.height(6.dp))
                Divider(
                    modifier = Modifier.fillMaxWidth(),
                    thickness = 0.5.dp,
                    color = MaterialTheme.colorScheme.outlineVariant
                )
                Spacer(modifier = Modifier.height(8.dp))

                // --- 3. Descripción del Material (Base para el estilo) ---
                Text(
                    text = comanda.descriptionLoteComanda,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.fillMaxWidth(),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )

                Spacer(modifier = Modifier.height(4.dp))

                // --- 4. Peso (Kilos) (ETIQUETA COMPACTA con estilo normal) ---
                Text(
                    buildAnnotatedString {
                        // Etiqueta (Estilo normal: BodyMedium, sin SemiBold/Bold)
                        withStyle(style = SpanStyle(
                            fontWeight = FontWeight.Normal,
                            color = MaterialTheme.colorScheme.onSurface
                        )) {
                            append("Peso total: ")
                        }
                        // Valor (Estilo normal: BodyMedium, sin Bold)
                        withStyle(style = SpanStyle(
                            fontWeight = FontWeight.Normal,
                            color = MaterialTheme.colorScheme.onSurface
                        )) {
                            append("${formatWeight(comanda.totalWeightComanda.toDoubleOrNull() ?: 0.0)} Kg")
                        }
                    },
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier.fillMaxWidth(),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )

                // --- 5. Observación (si existe) (Estilo BodySmall) ---
                comanda.remarkComanda.takeIf { it.isNotBlank() }?.let { remark ->
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        buildAnnotatedString {
                            withStyle(style = SpanStyle( color = Color.Gray)) {
                                append("Obs: ")
                            }
                            withStyle(style = SpanStyle(fontWeight = FontWeight.Normal, color = Color.Gray)) {
                                append(remark)
                            }
                        },
                        style = MaterialTheme.typography.bodySmall,
                        modifier = Modifier.fillMaxWidth(),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            } // Fin del Column principal
        }

        // --- Badge superior izquierdo ---
        Box(
            modifier = Modifier
                .align(Alignment.TopStart)
                .offset(x = 8.dp, y = (-topPaddingForBadge))
                .background(color = badgeColor, shape = CircleShape)
                .padding(horizontal = 8.dp, vertical = 4.dp)
        ) {
            Text(
                text = formattedComandaNumber,
                color = Color.White,
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold
            )
        }

        // --- Overlay de selección ---
        if (isSelected) {
            Box(
                modifier = Modifier
                    .matchParentSize()
                    .background(PrimaryColor.copy(alpha = 0.1f))
            )
        }
    }
}