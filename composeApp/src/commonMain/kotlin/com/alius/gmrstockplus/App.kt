package com.alius.gmrstockplus

import androidx.compose.material.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import cafe.adriel.voyager.navigator.CurrentScreen
import cafe.adriel.voyager.navigator.Navigator
import cafe.adriel.voyager.navigator.NavigatorDisposeBehavior
import com.alius.gmrstockplus.presentation.screens.RootScreen
import io.github.aakira.napier.DebugAntilog
import io.github.aakira.napier.Napier

@Composable
fun App() {
    // Inicializar Napier una sola vez fuera del ciclo de recomposición
    remember {
        Napier.base(DebugAntilog())
        true
    }

    androidx.compose.material.MaterialTheme {
        Surface {
            Navigator(
                screen = RootScreen(),
                // 🛡️ Esto asegura que Voyager no intente serializar
                // el stack de navegación de forma persistente.
                disposeBehavior = NavigatorDisposeBehavior(
                    disposeNestedNavigators = false,
                    disposeSteps = true
                )
            ) { navigator ->
                CurrentScreen()
            }
        }
    }
}