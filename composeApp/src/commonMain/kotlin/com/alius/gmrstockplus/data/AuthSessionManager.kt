package com.alius.gmrstockplus.data

object AuthSessionManager {
    var idTokenP07: String? = null
    var idTokenP08: String? = null

    // 🔑 Guardamos temporalmente las credenciales para re-login automático
    var savedEmail: String? = null
    var savedPassword: String? = null

    /**
     * Identifica automáticamente qué token enviar según el proyecto de Firebase.
     */
    fun getTokenForUrl(url: String): String? {
        return when {
            // Proyecto Planta 07
            url.contains("firestoreavanzadocompose-62d26") -> idTokenP07
            // Proyecto Planta 08
            url.contains("cepexproduccion-2d770") -> idTokenP08
            else -> null
        }
    }

    fun clear() {
        idTokenP07 = null
        idTokenP08 = null
        savedEmail = null
        savedPassword = null
    }
}
