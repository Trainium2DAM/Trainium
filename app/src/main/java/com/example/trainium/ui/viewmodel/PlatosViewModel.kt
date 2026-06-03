package com.example.trainium.ui.viewmodel

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.trainium.di.ServiceLocator
import com.example.trainium.models.Plato
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class PlatosViewModel : ViewModel() {
    var listaPlatosAceptados by mutableStateOf<List<Plato>>(emptyList())
    var indicePlatoActual by mutableStateOf(0)
    var sugerenciasPendientes by mutableStateOf<List<Plato>>(emptyList())
    var isLoading by mutableStateOf(false)
    var error by mutableStateOf<String?>(null)
    var showingDialogoSugerencia by mutableStateOf(false)
    var nuevoTitulo by mutableStateOf("")
    var nuevaReceta by mutableStateOf("")
    var nuevoTiempo by mutableStateOf("")
    var caloriasTexto by mutableStateOf("")
    var fotoBase64 by mutableStateOf<String?>(null)

    fun loadPlatos(isAdmin: Boolean) {
        viewModelScope.launch {
            isLoading = true
            error = null
            try {
                val (aceptados, pendientes) = withContext(Dispatchers.IO) {
                    val a = ServiceLocator.platoRepository.getApproved()
                    val p = if (isAdmin) ServiceLocator.platoRepository.getPending() else emptyList()
                    Pair(a, p)
                }
                listaPlatosAceptados = aceptados
                sugerenciasPendientes = pendientes
                indicePlatoActual = 0
            } catch (e: Exception) {
                error = "Error: ${e.message}"
            } finally {
                isLoading = false
            }
        }
    }

    fun enviarSugerencia(userId: Int, onSent: () -> Unit) {
        viewModelScope.launch {
            isLoading = true
            error = null
            try {
                val plato = Plato(
                    idUsuario = userId,
                    nombre = nuevoTitulo,
                    descripcion = nuevaReceta,
                    calorias = caloriasTexto.toDoubleOrNull(),
                    tiempo = nuevoTiempo,
                    imagenUrl = fotoBase64,
                    visibilidad = true,
                    aceptado = false
                )
                withContext(Dispatchers.IO) {
                    ServiceLocator.platoRepository.insert(plato)
                }
                clearSuggestionDialog()
                onSent()
            } catch (e: Exception) {
                error = "Error: ${e.message}"
            } finally {
                isLoading = false
            }
        }
    }

    fun aprobarSugerencia(id: Int) {
        viewModelScope.launch {
            try {
                withContext(Dispatchers.IO) {
                    ServiceLocator.platoRepository.approve(id)
                }
                sugerenciasPendientes = sugerenciasPendientes.filter { it.id != id }
            } catch (_: Exception) {
            }
        }
    }

    fun rechazarSugerencia(id: Int) {
        viewModelScope.launch {
            try {
                withContext(Dispatchers.IO) {
                    ServiceLocator.platoRepository.reject(id)
                }
                sugerenciasPendientes = sugerenciasPendientes.filter { it.id != id }
            } catch (_: Exception) {
            }
        }
    }

    fun nextDish() {
        if (listaPlatosAceptados.isNotEmpty()) {
            indicePlatoActual = (indicePlatoActual + 1) % listaPlatosAceptados.size
        }
    }

    fun clearSuggestionDialog() {
        nuevoTitulo = ""
        nuevaReceta = ""
        nuevoTiempo = ""
        caloriasTexto = ""
        fotoBase64 = null
        showingDialogoSugerencia = false
    }

    fun clearError() { error = null }
}
