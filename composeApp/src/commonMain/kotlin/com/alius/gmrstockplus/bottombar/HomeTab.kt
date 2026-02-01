package com.alius.gmrstockplus.bottombar

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Dashboard
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.graphics.vector.rememberVectorPainter
import cafe.adriel.voyager.navigator.Navigator
import cafe.adriel.voyager.navigator.tab.Tab
import cafe.adriel.voyager.navigator.tab.TabOptions
import com.alius.gmrstockplus.domain.model.User
import com.alius.gmrstockplus.presentation.screens.HomeScreenContent

class HomeTab(
    private val user: User,
    private val plantId: String, // 🔑 Recibimos el identificador de planta
    private val onChangeDatabase: () -> Unit,
    private val onLogoutClick: () -> Unit = {}
) : Tab {

    override val options: TabOptions
        @Composable
        get() {
            val icon = rememberVectorPainter(Icons.Filled.Dashboard)
            return remember {
                TabOptions(
                    index = 1u,
                    title = "Inicio",
                    icon = icon
                )
            }
        }

    @Composable
    override fun Content() {
        // Envolvemos el contenido en un Navigator propio de la pestaña
        Navigator(
            screen = HomeScreenContent(
                user = user,
                plantId = plantId, // 🔑 Pasamos el plantId al contenido
                onChangeDatabase = onChangeDatabase,
                onLogoutClick = onLogoutClick
            )
        )
    }
}