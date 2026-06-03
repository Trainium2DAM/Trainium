package com.example.trainium.ui.viewmodel

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.trainium.SecureSessionManager
import com.example.trainium.di.ServiceLocator
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class LoginViewModel : ViewModel() {
    var dni by mutableStateOf("")
    var pass by mutableStateOf("")
    var isLoading by mutableStateOf(false)
    var errorMessage by mutableStateOf<String?>(null)

    fun login(onSuccess: (nombre: String, isAdmin: Int, id: Int, isPremium: Int) -> Unit) {
        viewModelScope.launch {
            isLoading = true
            errorMessage = null
            try {
                val user = withContext(Dispatchers.IO) {
                    ServiceLocator.authRepository.getUserByDni(dni)
                }
                if (user != null) {
                    if (user.contraseniaHash == pass) {
                        SecureSessionManager.iniciarSesion(user.id, user.nombre, user.admin == 1, user.premium)
                        onSuccess(user.nombre, user.admin, user.id, if (user.premium) 1 else 0)
                    } else {
                        errorMessage = "Contraseña incorrecta"
                    }
                } else {
                    errorMessage = "Usuario no encontrado"
                }
            } catch (e: Exception) {
                errorMessage = "Error: ${e.message}"
            } finally {
                isLoading = false
            }
        }
    }

    fun clearError() { errorMessage = null }
}
