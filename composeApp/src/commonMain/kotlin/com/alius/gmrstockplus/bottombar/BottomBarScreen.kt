package com.alius.gmrstockplus.bottombar

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.ShoppingBag
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import cafe.adriel.voyager.core.annotation.InternalVoyagerApi
import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.navigator.tab.CurrentTab
import cafe.adriel.voyager.navigator.tab.TabNavigator
import com.alius.gmrstockplus.data.AuthRepository
import com.alius.gmrstockplus.domain.model.User
import com.alius.gmrstockplus.getPlatform
import kotlinx.serialization.Serializable
import kotlinx.serialization.Transient
import cafe.adriel.voyager.core.lifecycle.JavaSerializable
import cafe.adriel.voyager.navigator.internal.BackHandler

@Serializable
class BottomBarScreen(
    private val user: User,
    private val plantId: String, // 🔑 AHORA RECIBIMOS DIRECTAMENTE "P07" O "P08"
    @Transient private val colors: BottomBarColors = BottomBarColors(),
    @Transient private val authRepository: AuthRepository? = null,
    @Transient private val onChangeDatabase: (() -> Unit)? = null,
    @Transient private val onLogout: (() -> Unit)? = null
) : Screen, JavaSerializable {

    override val key: String = "bottom_bar_main"

    @OptIn(InternalVoyagerApi::class)
    @Composable
    override fun Content() {
        var homeRefreshKey by remember { mutableStateOf(0) }
        val platform = getPlatform()

        // 🛡️ Inicializamos las Tabs pasando el plantId directamente.
        // Ya no dependemos de LocalDatabaseUrl.current
        val homeTab = remember(user.id, plantId, homeRefreshKey) {
            HomeTab(
                user = user,
                plantId = plantId,
                onChangeDatabase = { onChangeDatabase?.invoke() },
                onLogoutClick = { onLogout?.invoke() }
            )
        }

        // Preparamos las otras pestañas (deberás actualizar sus constructores también)
        val clientTab = remember(user.id, plantId) { ClientTab(user, plantId) }
        val batchTab = remember(user.id, plantId) { BatchTab(user, plantId) }
        val processTab = remember(user.id, plantId) { ProcessTab(user, plantId) }
        val transferTab = remember(user.id, plantId) { TransferTab(user, plantId) }

        TabNavigator(homeTab) { tabNavigator ->

            BackHandler(enabled = tabNavigator.current.key != homeTab.key) {
                tabNavigator.current = homeTab
            }

            Scaffold(
                topBar = {
                    TopAppBar(
                        backgroundColor = colors.topBarBackground,
                        contentColor = colors.topBarContent,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(64.dp + if (platform.isMobile) WindowInsets.statusBars.asPaddingValues().calculateTopPadding() else 0.dp),
                        title = {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(top = if (platform.isMobile) 28.dp else 8.dp),
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.Center
                            ) {
                                Text(
                                    text = "GMR Stock - $plantId", // 🔑 Visualización directa del ID
                                    fontSize = 24.sp,
                                    fontWeight = androidx.compose.ui.text.font.FontWeight.Bold,
                                    color = colors.topBarContent
                                )
                                Text(
                                    text = "Gestión de stock en tiempo real",
                                    fontSize = 12.sp,
                                    color = colors.topBarContent.copy(alpha = 0.8f)
                                )
                            }
                        }
                    )
                },
                bottomBar = {
                    BottomNavigation(
                        backgroundColor = colors.bottomBarBackground,
                        contentColor = colors.bottomBarContent,
                        modifier = Modifier.navigationBarsPadding()
                    ) {
                        BottomNavigationItem(
                            selected = tabNavigator.current.key.startsWith(homeTab.key),
                            onClick = {
                                if (tabNavigator.current.key.startsWith(homeTab.key)) {
                                    homeRefreshKey++
                                }
                                tabNavigator.current = homeTab
                            },
                            icon = { Icon(Icons.Filled.Dashboard, null) },
                            label = { Text(homeTab.options.title) }
                        )
                        BottomNavigationItem(
                            selected = tabNavigator.current.key == processTab.key,
                            onClick = { tabNavigator.current = processTab },
                            icon = { Icon(Icons.Filled.Autorenew, null) },
                            label = { Text(processTab.options.title) }
                        )
                        BottomNavigationItem(
                            selected = tabNavigator.current.key == batchTab.key,
                            onClick = { tabNavigator.current = batchTab },
                            icon = { Icon(Icons.Outlined.ShoppingBag, null) },
                            label = { Text(batchTab.options.title) }
                        )
                        BottomNavigationItem(
                            selected = tabNavigator.current.key == transferTab.key,
                            onClick = { tabNavigator.current = transferTab },
                            icon = { Icon(Icons.Default.LocalShipping, null) },
                            label = { Text(transferTab.options.title) }
                        )
                        BottomNavigationItem(
                            selected = tabNavigator.current.key == clientTab.key,
                            onClick = { tabNavigator.current = clientTab },
                            icon = { Icon(Icons.Filled.BarChart, null) },
                            label = { Text(clientTab.options.title) }
                        )
                    }
                }
            ) { innerPadding ->
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(innerPadding)
                        .background(color = colors.bottomBarBackground)
                ) {
                    CurrentTab()
                }
            }
        }
    }
}