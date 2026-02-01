package com.alius.gmrstockplus.data

import com.alius.gmrstockplus.domain.model.User
import dev.gitlive.firebase.Firebase
import dev.gitlive.firebase.auth.auth
import dev.gitlive.firebase.app

class AuthRepositoryImpl : AuthRepository {

    // Referencias a los servicios de Auth para cada proyecto mediante lazy
    private val auth07 by lazy { Firebase.auth }
    private val auth08 by lazy { Firebase.auth(Firebase.app("P08")) }

    override suspend fun login(email: String, password: String): User? {
        return try {
            val result07 = auth07.signInWithEmailAndPassword(email, password)
            val result08 = auth08.signInWithEmailAndPassword(email, password)

            val firebaseUser = result07.user
            if (firebaseUser != null && result08.user != null) {
                println("✅ Login exitoso en P07 y P08 mediante SDK")

                // 🛠️ SOLUCIÓN PARA DESKTOP:
                // En lugar de llamar a firebaseUser.email (que lanza NotImplementedError),
                // usamos un bloque try-catch o simplemente usamos el 'email' que recibimos por parámetro.
                val userEmail = try {
                    firebaseUser.email ?: email
                } catch (e: NotImplementedError) {
                    email
                }

                User(id = firebaseUser.uid, email = userEmail)
            } else null
        } catch (e: Exception) {
            println("❌ Error en Login SDK: ${e.message}")
            null
        }
    }

    override suspend fun getCurrentUser(): User? {
        val user07 = auth07.currentUser
        val user08 = auth08.currentUser

        return if (user07 != null && user08 != null) {
            val safeEmail = try { user07.email ?: "" } catch (e: NotImplementedError) { "" }
            User(id = user07.uid, email = safeEmail)
        } else null
    }

    override fun isFullyAuthenticated(): Boolean {
        // Método rápido síncrono para comprobaciones de UI
        return auth07.currentUser != null && auth08.currentUser != null
    }

    override suspend fun logout() {
        try {
            auth07.signOut()
            auth08.signOut()
            println("🚪 Sesión cerrada en ambos proyectos (SDK)")
        } catch (e: Exception) {
            println("⚠️ Error al cerrar sesión: ${e.message}")
        }
    }

    override suspend fun register(email: String, password: String): User? {
        return try {
            // Creamos el usuario en ambos proyectos para mantener la paridad
            val result07 = auth07.createUserWithEmailAndPassword(email, password)
            val result08 = auth08.createUserWithEmailAndPassword(email, password)

            result07.user?.let {
                User(id = it.uid, email = it.email ?: email)
            }
        } catch (e: Exception) {
            println("❌ Error en Registro: ${e.message}")
            null
        }
    }
}

