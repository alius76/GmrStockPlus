package com.alius.gmrstockplus.bottombar

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.EuroSymbol
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.graphics.vector.rememberVectorPainter
import cafe.adriel.voyager.navigator.Navigator
import cafe.adriel.voyager.navigator.tab.Tab
import cafe.adriel.voyager.navigator.tab.TabOptions
import com.alius.gmrstockplus.domain.model.User
import com.alius.gmrstockplus.presentation.screens.TransferScreenContent

class TransferTab(
    private val user: User,
    private val plantId: String // 🔑 Cambiado: databaseUrl -> plantId
) : Tab {

    override val key: String = "TransferTab_${user.id}_$plantId"

    override val options: TabOptions
        @Composable
        get() {
            val icon = rememberVectorPainter(Icons.Filled.EuroSymbol)
            return remember {
                TabOptions(
                    index = 5u,
                    title = "Ventas",
                    icon = icon
                )
            }
        }

    @Composable
    override fun Content() {
        // Navigator para permitir el flujo de creación de ventas/facturas
        Navigator(
            screen = TransferScreenContent(user = user, plantId = plantId)
        )
    }
}