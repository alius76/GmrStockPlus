package com.alius.gmrstockplus.presentation.screens.logic

import com.alius.gmrstockplus.data.AuthRepository
import com.alius.gmrstockplus.domain.model.User
import com.russhwolf.settings.Settings
import com.russhwolf.settings.set
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class LoginScreenLogic(
    private val authRepository: AuthRepository
) {
    // 🔑 Inicializamos Settings (usa SharedPreferences, NSUserDefaults o Java Prefs según plataforma)
    private val settings: Settings = Settings()
    private val KEY_EMAIL = "remembered_email"
    private val KEY_REMEMBER = "remember_me_active"

    private val scope = CoroutineScope(Dispatchers.Main)

    private val _email = MutableStateFlow("")
    val email: StateFlow<String> = _email.asStateFlow()

    private val _password = MutableStateFlow("")
    val password: StateFlow<String> = _password.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage.asStateFlow()

    private val _user = MutableStateFlow<User?>(null)
    val user: StateFlow<User?> = _user.asStateFlow()

    // 🔘 Estado para el Checkbox "Recordarme"
    private val _rememberMe = MutableStateFlow(false)
    val rememberMe: StateFlow<Boolean> = _rememberMe.asStateFlow()

    init {
        // Al instanciar la lógica, recuperamos el email si "Recordarme" estaba activo
        val isRememberActive = settings.getBoolean(KEY_REMEMBER, false)
        if (isRememberActive) {
            _email.value = settings.getString(KEY_EMAIL, "")
            _rememberMe.value = true
        }
    }

    fun checkExistingSession() {
        scope.launch {
            val currentUser = authRepository.getCurrentUser()
            if (currentUser != null) {
                _user.value = currentUser
            }
        }
    }

    fun updateEmail(newEmail: String) { _email.value = newEmail }
    fun updatePassword(newPassword: String) { _password.value = newPassword }

    // Función para actualizar el estado del Checkbox
    fun updateRememberMe(value: Boolean) { _rememberMe.value = value }

    fun login() {
        if (email.value.isBlank() || password.value.isBlank()) {
            _errorMessage.value = "Email y contraseña requeridos"
            return
        }

        scope.launch {
            _isLoading.value = true
            _errorMessage.value = null
            try {
                val loggedUser = authRepository.login(email.value, password.value)
                if (loggedUser != null) {

                    // ✅ GESTIÓN DE PERSISTENCIA LOCAL TRAS LOGIN EXITOSO
                    if (_rememberMe.value) {
                        settings[KEY_EMAIL] = email.value
                        settings[KEY_REMEMBER] = true
                    } else {
                        settings.remove(KEY_EMAIL)
                        settings[KEY_REMEMBER] = false
                    }

                    _user.value = loggedUser
                } else {
                    _errorMessage.value = "No se pudo validar el acceso en ambos servidores"
                }
            } catch (e: Exception) {
                _errorMessage.value = e.message ?: "Error de conexión"
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun register() {
        scope.launch {
            _isLoading.value = true
            _errorMessage.value = null
            try {
                val newUser = authRepository.register(email.value, password.value)
                if (newUser != null) {
                    _user.value = newUser
                } else {
                    _errorMessage.value = "Error al crear la cuenta"
                }
            } catch (e: Exception) {
                _errorMessage.value = e.message ?: "Error en el registro"
            } finally {
                _isLoading.value = false
            }
        }
    }
}