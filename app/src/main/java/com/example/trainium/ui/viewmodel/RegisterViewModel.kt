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

class RegisterViewModel : ViewModel() {
    var nombre by mutableStateOf("")
    var dni by mutableStateOf("")
    var tipoDocumento by mutableStateOf("DNI")
    var tipoDocDesplegado by mutableStateOf(false)
    var email by mutableStateOf("")
    var pass by mutableStateOf("")
    var prefijo by mutableStateOf("+34")
    var numeroTelf by mutableStateOf("")
    var prefijoDesplegado by mutableStateOf(false)
    var isLoading by mutableStateOf(false)
    var errorMessage by mutableStateOf<String?>(null)
    var successMessage by mutableStateOf<String?>(null)

    fun validateDocumento(): Boolean {
        val dniRegex = Regex("^[0-9]{8}[A-Z]$")
        val nieRegex = Regex("^[A-Z][0-9]{8}$")
        val passportRegex = Regex("^[A-Z]{3}[0-9]{6}$")
        return when (tipoDocumento) {
            "DNI" -> dniRegex.matches(dni)
            "NIE" -> nieRegex.matches(dni)
            else -> passportRegex.matches(dni)
        }
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
                if (!validateDocumento()) {
                    errorMessage = when (tipoDocumento) {
                        "DNI" -> "invalid_dni"
                        "NIE" -> "invalid_nie"
                        else -> "invalid_passport"
                    }
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
                nombre = ""; dni = ""; tipoDocumento = "DNI"; email = ""; pass = ""; numeroTelf = ""
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
