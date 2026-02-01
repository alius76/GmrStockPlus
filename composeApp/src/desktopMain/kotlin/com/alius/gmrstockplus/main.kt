package com.alius.gmrstockplus

import android.app.Application
import androidx.compose.ui.Alignment
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.WindowPosition
import androidx.compose.ui.window.WindowState
import androidx.compose.ui.window.application
import com.google.firebase.FirebasePlatform
import dev.gitlive.firebase.Firebase
import dev.gitlive.firebase.FirebaseOptions
import dev.gitlive.firebase.initialize

fun main() = application {
    // 1. Inicializamos la plataforma (Storage interno)
    FirebasePlatform.initializeFirebasePlatform(object : FirebasePlatform() {
        val storage = mutableMapOf<String, String>()
        override fun clear(key: String) { storage.remove(key) }
        override fun log(msg: String) = println(msg)
        override fun retrieve(key: String) = storage[key]
        override fun store(key: String, value: String) { storage[key] = value }
    })

    // 2. Configuración de tus dos bases de datos (IDs de tipo Web para Desktop)
    val options07 = FirebaseOptions(
        applicationId = "1:983598044129:web:700bb94f6bcad486ca38a0",
        apiKey = "AIzaSyAi8EU5z7TdQXczA_y5R5kH_1abxIoohLo",
        projectId = "firestoreavanzadocompose-62d26"
    )

    val options08 = FirebaseOptions(
        applicationId = "1:729925233716:web:4fe4b6484ac7c2bfe2ee61",
        apiKey = "AIzaSyDuwTitw5QIsWZWmRQSaSIhuAYMggjT8Gw",
        projectId = "cepexproduccion-2d770"
    )

    // 3. Inicializamos usando Application() para evitar el error de Context
    try {
        // Inicializa la base de datos por defecto (P07)
        Firebase.initialize(Application(), options07)

        // Inicializa la base de datos secundaria con nombre (P08)
        Firebase.initialize(Application(), options08, name = "P08")

        println("✅ Firebase Desktop: P07 y P08 inicializadas con éxito")
    } catch (e: Exception) {
        println("⚠️ Error inicializando Firebase en Desktop: ${e.message}")
    }

    Window(
        onCloseRequest = ::exitApplication,
        title = "GmrStockPlus v1.0.0",
        state = WindowState(
            position = WindowPosition.Aligned(Alignment.Center),
            width = 800.dp,
            height = 900.dp
        )
    ) {
        window.minimumSize = java.awt.Dimension(500, 700)
        App()
    }
}