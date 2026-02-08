package com.alius.gmrstockplus.bottombar

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.ShoppingBag
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.graphics.vector.VectorPainter
import androidx.compose.ui.graphics.vector.rememberVectorPainter
import cafe.adriel.voyager.navigator.tab.Tab
import cafe.adriel.voyager.navigator.tab.TabOptions
import com.alius.gmrstockplus.domain.model.User
import com.alius.gmrstockplus.presentation.screens.BatchScreenContent

class BatchTab(
    private val user: User,
    private val plantId: String
) : Tab {

    override val key: String = "BatchTab_${user.id}_$plantId"

    override val options: TabOptions
        @Composable
        get() {
            val icon: VectorPainter = rememberVectorPainter(Icons.Outlined.ShoppingBag)
            return remember {
                TabOptions(
                    index = 3u,
                    title = "Lotes",
                    icon = icon
                )
            }
        }

    @Composable
    override fun Content() {

        BatchScreenContent(user = user, plantId = plantId)
    }
}