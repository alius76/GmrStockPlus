package com.alius.gmrstockplus.presentation.screens

import androidx.compose.runtime.*
import cafe.adriel.voyager.core.screen.Screen
import com.alius.gmrstockplus.bottombar.BottomBarColors
import com.alius.gmrstockplus.bottombar.BottomBarScreen
import com.alius.gmrstockplus.data.getAuthRepository
import com.alius.gmrstockplus.domain.model.User
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class RootScreen : Screen {

    @Composable
    override fun Content() {
        // Obtenemos el repositorio de autenticación
        val authRepository = remember { getAuthRepository() }
        val scope = rememberCoroutineScope()

        var isInitialized by remember { mutableStateOf(false) }
        var user by remember { mutableStateOf<User?>(null) }
        var selectedPlantId by remember { mutableStateOf<String?>(null) }

        // 1. CHEQUEO INICIAL: Verificamos si hay sesión al arrancar
        LaunchedEffect(Unit) {
            val currentUser = authRepository.getCurrentUser()
            delay(1500) // Tiempo para mostrar el Splash
            user = currentUser
            isInitialized = true
        }

        if (!isInitialized) {
            SplashScreen()
        } else {
            when {
                // Caso A: No hay usuario -> Mostramos Login
                user == null -> {
                    LoginScreen(
                        // 🔑 Eliminamos authRepository de aquí, ya no es necesario
                        onLoginSuccess = { loggedInUser ->
                            user = loggedInUser
                        }
                    ).Content()
                }

                // Caso B: Hay usuario pero no ha elegido Planta (P07/P08)
                selectedPlantId == null -> {
                    DatabaseSelectionScreen { plantId ->
                        selectedPlantId = plantId
                    }.Content()
                }

                // Caso C: Todo listo -> Entramos al Home
                else -> {
                    BottomBarScreen(
                        user = user!!,
                        plantId = selectedPlantId!!,
                        authRepository = authRepository,
                        colors = BottomBarColors(),
                        onChangeDatabase = { selectedPlantId = null },
                        onLogout = {
                            scope.launch {
                                authRepository.logout()
                                user = null
                                selectedPlantId = null
                            }
                        }
                    ).Content()
                }
            }
        }
    }
}