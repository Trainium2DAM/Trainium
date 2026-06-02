package com.example.trainium2.ui.viewmodel

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.trainium2.AppConfig
import com.example.trainium2.di.ServiceLocator
import com.example.trainium2.models.PesoUsuario
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.Date

class RegistroViewModel : ViewModel() {
    var registros by mutableStateOf<List<PesoUsuario>>(emptyList())
    var pesoCampo by mutableStateOf("")
    var editandoId by mutableStateOf<Int?>(null)
    var editandoPeso by mutableStateOf("")
    var isLoading by mutableStateOf(false)

    val yaExisteHoy: Boolean
        get() {
            val hoy = AppConfig.FORMAT_ISO_DATE.format(Date())
            return registros.any { it.fecha == hoy }
        }

    fun loadRegistros(userId: Int) {
        viewModelScope.launch {
            isLoading = true
            try {
                registros = withContext(Dispatchers.IO) {
                    ServiceLocator.pesoRepository.getByUser(userId)
                }
            } catch (_: Exception) {
            } finally {
                isLoading = false
            }
        }
    }

    fun addRegistro(userId: Int, onAdded: () -> Unit) {
        viewModelScope.launch {
            isLoading = true
            try {
                val peso = pesoCampo.toDoubleOrNull() ?: return@launch
                val hoy = AppConfig.FORMAT_ISO_DATE.format(Date())
                withContext(Dispatchers.IO) {
                    ServiceLocator.pesoRepository.insert(userId, peso, hoy)
                }
                pesoCampo = ""
                loadRegistros(userId)
                onAdded()
            } catch (_: Exception) {
            } finally {
                isLoading = false
            }
        }
    }

    fun startEdit(id: Int, peso: Double) {
        editandoId = id
        editandoPeso = peso.toString()
    }

    fun saveEdit() {
        val id = editandoId ?: return
        val peso = editandoPeso.toDoubleOrNull() ?: return
        viewModelScope.launch {
            try {
                withContext(Dispatchers.IO) {
                    ServiceLocator.pesoRepository.update(id, peso)
                }
                editandoId = null
                editandoPeso = ""
            } catch (_: Exception) {
            }
        }
    }

    fun deleteRegistro(id: Int) {
        viewModelScope.launch {
            try {
                withContext(Dispatchers.IO) {
                    ServiceLocator.pesoRepository.delete(id)
                }
                registros = registros.filter { it.id != id }
            } catch (_: Exception) {
            }
        }
    }
}
