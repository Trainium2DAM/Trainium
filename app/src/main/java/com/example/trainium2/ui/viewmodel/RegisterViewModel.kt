package com.example.trainium2.ui.viewmodel

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.trainium2.di.ServiceLocator
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class RegisterViewModel : ViewModel() {
    var nombre by mutableStateOf("")
    var dni by mutableStateOf("")
    var email by mutableStateOf("")
    var pass by mutableStateOf("")
    var prefijo by mutableStateOf("+34")
    var numeroTelf by mutableStateOf("")
    var prefijoDesplegado by mutableStateOf(false)
    var isLoading by mutableStateOf(false)
    var errorMessage by mutableStateOf<String?>(null)
    var successMessage by mutableStateOf<String?>(null)

    fun validateDni(): Boolean {
        val dniRegex = Regex("^[0-9]{8}[A-Z]$")
        val nieRegex = Regex("^[XYZ][0-9]{7}[A-Z]$")
        val extranjeroRegex = Regex("^[A-Z0-9]{5,20}$")
        return dniRegex.matches(dni) || nieRegex.matches(dni) || extranjeroRegex.matches(dni)
    }

    fun register(onBack: () -> Unit) {
        viewModelScope.launch {
            isLoading = true
            errorMessage = null
            successMessage = null
            try {
                if (nombre.isBlank() || dni.isBlank() || email.isBlank() || pass.isBlank() || numeroTelf.isBlank()) {
                    errorMessage = "Todos los campos son obligatorios"
                    return@launch
                }
                if (!validateDni()) {
                    errorMessage = "Formato de DNI/NIE inválido"
                    return@launch
                }
                val digitos = numeroTelf.filter { it.isDigit() }
                if (digitos.length < 7) {
                    errorMessage = "El teléfono debe tener al menos 7 d\u00edgitos"
                    return@launch
                }
                val existe = withContext(Dispatchers.IO) {
                    ServiceLocator.authRepository.userExistsByDni(dni)
                }
                if (existe) {
                    errorMessage = "El DNI ya está registrado"
                    return@launch
                }
                val telefono = "$prefijo$digitos"
                withContext(Dispatchers.IO) {
                    ServiceLocator.authRepository.insertUser(nombre, dni, email, pass, telefono)
                }
                successMessage = "Usuario registrado correctamente"
                nombre = ""; dni = ""; email = ""; pass = ""; numeroTelf = ""
                onBack()
            } catch (e: Exception) {
                errorMessage = "Error: ${e.message}"
            } finally {
                isLoading = false
            }
        }
    }

    fun clearError() { errorMessage = null }
    fun clearSuccess() { successMessage = null }
}
