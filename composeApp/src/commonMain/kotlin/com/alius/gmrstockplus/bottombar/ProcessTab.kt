package com.alius.gmrstockplus.bottombar

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Autorenew
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.graphics.vector.rememberVectorPainter
import cafe.adriel.voyager.navigator.Navigator
import cafe.adriel.voyager.navigator.tab.Tab
import cafe.adriel.voyager.navigator.tab.TabOptions
import com.alius.gmrstockplus.domain.model.User
import com.alius.gmrstockplus.presentation.screens.ProcessScreenContent

class ProcessTab(
    private val user: User,
    private val plantId: String // 🔑 Cambiado: databaseUrl -> plantId
) : Tab {

    // Añadimos plantId a la key para que la pestaña se refresque al cambiar de fábrica
    override val key: String = "ProcessTab_${user.id}_$plantId"

    override val options: TabOptions
        @Composable
        get() {
            val icon = rememberVectorPainter(Icons.Default.Autorenew)
            return remember {
                TabOptions(
                    index = 4u,
                    title = "WIP",
                    icon = icon
                )
            }
        }

    @Composable
    override fun Content() {
        Navigator(
            screen = ProcessScreenContent(user = user, plantId = plantId)
        )
    }
}