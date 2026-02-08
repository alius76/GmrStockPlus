package com.alius.gmrstockplus.bottombar

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Dashboard
import androidx.compose.runtime.Composable
import androidx.compose.runtime.key // Importante
import androidx.compose.runtime.remember
import androidx.compose.ui.graphics.vector.rememberVectorPainter
import cafe.adriel.voyager.navigator.Navigator
import cafe.adriel.voyager.navigator.tab.Tab
import cafe.adriel.voyager.navigator.tab.TabOptions
import com.alius.gmrstockplus.domain.model.User
import com.alius.gmrstockplus.presentation.screens.HomeScreenContent

class HomeTab(
    private val user: User,
    private val plantId: String,
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
        // 🔑 LA SOLUCIÓN:
        // Si plantId cambia, 'key' destruye este Navigator y crea uno nuevo.
        // Sin esto, Voyager siempre mostrará la HomeScreenContent que se creó la primera vez.
        key(plantId) {
            Navigator(
                screen = HomeScreenContent(
                    user = user,
                    plantId = plantId,
                    onChangeDatabase = onChangeDatabase,
                    onLogoutClick = onLogoutClick
                )
            )
        }
    }
}