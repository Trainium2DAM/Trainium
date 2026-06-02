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

class EditProfileViewModel : ViewModel() {
    var nombre by mutableStateOf("")
    var email by mutableStateOf("")
    var telefono by mutableStateOf("")
    var password by mutableStateOf("")
    var isPremium by mutableStateOf(false)
    var fotoBase64 by mutableStateOf<String?>(null)
    var isLoading by mutableStateOf(false)
    var avatarMenuExpanded by mutableStateOf(false)

    fun loadUser(id: Int) {
        viewModelScope.launch {
            isLoading = true
            try {
                val user = withContext(Dispatchers.IO) {
                    ServiceLocator.usuarioRepository.getUserById(id)
                }
                user?.let {
                    nombre = it.nombre
                    email = it.email ?: ""
                    telefono = it.telefono ?: ""
                    isPremium = it.premium
                    fotoBase64 = it.foto
                }
            } catch (_: Exception) {
            } finally {
                isLoading = false
            }
        }
    }

    fun saveUser(id: Int, onSaved: () -> Unit) {
        viewModelScope.launch {
            isLoading = true
            try {
                withContext(Dispatchers.IO) {
                    ServiceLocator.usuarioRepository.updateUser(id, nombre, email, telefono, fotoBase64)
                }
                if (password.isNotBlank()) {
                    withContext(Dispatchers.IO) {
                        ServiceLocator.usuarioRepository.updatePassword(id, password)
                    }
                }
                onSaved()
            } catch (_: Exception) {
            } finally {
                isLoading = false
            }
        }
    }

    fun setFoto(value: String?) { fotoBase64 = value }

    fun deleteFoto() { fotoBase64 = null }
}
