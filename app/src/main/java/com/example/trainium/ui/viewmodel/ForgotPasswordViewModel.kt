package com.example.trainium.ui.viewmodel

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.trainium.di.ServiceLocator
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class ForgotPasswordViewModel : ViewModel() {
    var step by mutableStateOf(1)
    var dni by mutableStateOf("")
    var email by mutableStateOf("")
    var newPass by mutableStateOf("")
    var confirmPass by mutableStateOf("")
    var idUsuario by mutableStateOf(0)
    var isLoading by mutableStateOf(false)
    var errorMessage by mutableStateOf<String?>(null)

    fun verifyUser() {
        viewModelScope.launch {
            isLoading = true
            errorMessage = null
            try {
                if (dni.isBlank() || email.isBlank()) {
                    errorMessage = "Todos los campos son obligatorios"
                    return@launch
                }
                val user = withContext(Dispatchers.IO) {
                    ServiceLocator.authRepository.verifyCredentials(dni, email)
                }
                if (user != null) {
                    idUsuario = user.id
                    step = 2
                } else {
                    errorMessage = "DNI y email no coinciden"
                }
            } catch (e: Exception) {
                errorMessage = "Error: ${e.message}"
            } finally {
                isLoading = false
            }
        }
    }

    fun updatePassword(onBack: () -> Unit) {
        viewModelScope.launch {
            isLoading = true
            errorMessage = null
            try {
                if (newPass.length < 4) {
                    errorMessage = "La contraseña debe tener al menos 4 caracteres"
                    return@launch
                }
                if (newPass != confirmPass) {
                    errorMessage = "Las contraseñas no coinciden"
                    return@launch
                }
                withContext(Dispatchers.IO) {
                    ServiceLocator.authRepository.updatePassword(idUsuario, newPass)
                }
                onBack()
            } catch (e: Exception) {
                errorMessage = "Error: ${e.message}"
            } finally {
                isLoading = false
            }
        }
    }

    fun clearError() { errorMessage = null }
}
