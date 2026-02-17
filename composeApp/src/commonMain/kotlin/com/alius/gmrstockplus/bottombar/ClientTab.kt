package com.alius.gmrstockplus.bottombar

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.graphics.vector.rememberVectorPainter
import cafe.adriel.voyager.navigator.Navigator
import cafe.adriel.voyager.navigator.tab.Tab
import cafe.adriel.voyager.navigator.tab.TabOptions
import com.alius.gmrstockplus.domain.model.User
import com.alius.gmrstockplus.presentation.screens.ClientScreenContent
import com.alius.gmrstockplus.presentation.screens.ProcessScreenContent

class ClientTab(
    private val user: User,
    private val plantId: String // 🔑 Cambiado: databaseUrl -> plantId
) : Tab {

    override val key: String = "ClientTab_${user.id}_$plantId"

    override val options: TabOptions
        @Composable
        get() {
            val icon = rememberVectorPainter(Icons.Filled.BarChart)
            return remember {
                TabOptions(
                    index = 2u,
                    title = "Ranking",
                    icon = icon
                )
            }
        }

    @Composable
    override fun Content() {
        ClientScreenContent(user = user, plantId = plantId)
    }
}