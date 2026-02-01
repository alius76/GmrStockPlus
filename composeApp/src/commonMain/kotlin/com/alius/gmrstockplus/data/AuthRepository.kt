package com.alius.gmrstockplus.data

import com.alius.gmrstockplus.domain.model.User

interface AuthRepository {
    suspend fun login(email: String, password: String): User?
    suspend fun register(email: String, password: String): User?
    suspend fun getCurrentUser(): User?
    suspend fun logout()
    fun isFullyAuthenticated(): Boolean
}

// 🔑 AÑADE ESTO AQUÍ:
expect fun getAuthRepository(): AuthRepository