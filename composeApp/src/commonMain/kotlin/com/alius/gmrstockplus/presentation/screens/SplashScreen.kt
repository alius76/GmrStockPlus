package com.alius.gmrstockplus.presentation.screens

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay

private val PrimaryColor = Color(0xFF029083)
private val SecondaryColor = Color(0xFF00BFA5)
private val AccentColor = Color(0xFFFFFFFF)

@Composable
fun SplashScreen(durationMillis: Int = 4000) {
    // Animaciones de fade-in y slide-up
    val alphaAnim = remember { Animatable(0f) }
    val offsetY = remember { Animatable(30f) }

    LaunchedEffect(Unit) {
        alphaAnim.animateTo(
            targetValue = 1f,
            animationSpec = tween(durationMillis = 1000)
        )
        offsetY.animateTo(
            targetValue = 0f,
            animationSpec = tween(durationMillis = 1000)
        )
        delay(durationMillis.toLong())
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(PrimaryColor),
        contentAlignment = Alignment.Center
    ) {
        // Contenido central
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier
                .offset(y = offsetY.value.dp)
                .padding(horizontal = 24.dp)
        ) {
            // Nombre de la app
            Text(
                text = "GMR Stock +",
                fontSize = 52.sp,
                fontWeight = FontWeight.ExtraBold,
                color = AccentColor,
                modifier = Modifier.alpha(alphaAnim.value)
            )
            Spacer(modifier = Modifier.height(12.dp))
            // Lema
            Text(
                text = "La inteligencia de tu almacén",
                fontSize = 20.sp,
                fontWeight = FontWeight.Medium,
                color = SecondaryColor,
                modifier = Modifier.alpha(alphaAnim.value)
            )
            Spacer(modifier = Modifier.height(40.dp))
            // Barra de progreso
            CircularProgressIndicator(
                color = AccentColor,
                strokeWidth = 5.dp,
                modifier = Modifier
                    .size(48.dp)
                    .alpha(alphaAnim.value)
            )
        }

        // Pie de página discreto
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(bottom = 20.dp),
            verticalArrangement = Arrangement.Bottom,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "Desarrollado por Alejandro II",
                fontSize = 14.sp,
                fontWeight = FontWeight.Normal,
                color = AccentColor.copy(alpha = 0.7f),
                modifier = Modifier.alpha(alphaAnim.value)
            )
        }
    }
}
